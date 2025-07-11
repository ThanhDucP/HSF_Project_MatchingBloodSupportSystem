package com.chillguy.tiny.blood.config;

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

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    private static final List<String> EXCLUDE_PREFIXES = List.of(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/validate",
            "/swagger", "/v3/api-docs", "/swagger-ui"
    );

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private boolean isExcluded(String path) {
        return EXCLUDE_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (tokenBlacklistService.contains(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"status\":\"fail\", \"message\":\"Token đã bị vô hiệu hóa hoặc đăng xuất\"}");
            return;
        }

        try {
            String accountId = jwtUtil.extractAccountId(token);

            if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Nếu cần quyền thì lấy từ claims
                String role = jwtUtil.extractAllClaims(token).get("role", String.class);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(accountId, null, authorities);
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
