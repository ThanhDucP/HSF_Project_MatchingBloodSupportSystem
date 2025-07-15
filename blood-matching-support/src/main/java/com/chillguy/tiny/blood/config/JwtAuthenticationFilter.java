package com.chillguy.tiny.blood.config;

import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.service.TokenBlacklistService;
import com.chillguy.tiny.blood.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final AccountRepository accountRepository;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, AccountRepository accountRepository) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.accountRepository = accountRepository;
    }

    private boolean isExcluded(String path) {
        return path.startsWith("/swagger")
                || path.contains("swagger-ui")
                || path.contains("api-docs")
                || path.contains("webjars")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/logout")
                || path.startsWith("/api/auth/validate");
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {


        String path = request.getServletPath();
        if (isExcluded(path)) {
            log.info("Path '{}' is excluded from filtering", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.info("Token extracted: {}", token);

        if (tokenBlacklistService.contains(token)) {
            log.warn("Token is blacklisted");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"status\":\"fail\", \"message\":\"Token đã bị vô hiệu hóa hoặc đăng xuất\"}");
            return;
        }

        try {
            String accountId = jwtUtil.extractAccountId(token);
            String role = jwtUtil.extractAllClaims(token).get("role", String.class);
            Optional<Account> accountInDb =  accountRepository.findByAccountId(accountId);

            log.info("Extracted accountId: {}, role: {}", accountId, role);

            if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Account account = accountInDb.get();

                // Nếu cần quyền thì lấy từ claims
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(account, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            log.error("JWT validation failed: ", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"status\":\"fail\", \"message\":\"Token không hợp lệ hoặc đã hết hạn\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

