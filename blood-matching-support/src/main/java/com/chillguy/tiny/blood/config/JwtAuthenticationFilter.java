package com.chillguy.tiny.blood.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chillguy.tiny.blood.service.TokenBlacklistService;
import com.chillguy.tiny.blood.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private boolean isExcluded(String path) {
        // Chỉ exclude các API public và tài nguyên tĩnh.
        // Các trang template sẽ được filter kiểm tra.
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.equals("/favicon.ico")
                || path.startsWith("/swagger") || path.contains("swagger-ui") || path.contains("api-docs")
                || path.equals("/") || path.equals("/home") || path.equals("/index") || path.equals("/about")
                || path.startsWith("/auth/")
                || path.equals("/api/auth/login") || path.equals("/api/auth/register");
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
            // Nếu không có token, và đây là trang cần xác thực, Spring Security sẽ xử lý (trả về 403)
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (tokenBlacklistService.contains(token)) {
            log.warn("Token is blacklisted");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"status\":\"fail\", \"message\":\"Token đã bị vô hiệu hóa hoặc đăng xuất\"}");
            return;
        }

        try {
            String accountId = jwtUtil.extractAccountId(token);
            if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Lấy thông tin user từ token, không cần query lại DB
                String role = jwtUtil.extractAllClaims(token).get("role", String.class);
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                
                // Tạo đối tượng principal là accountId (hoặc một User object đơn giản)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(accountId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token không hợp lệ, không set authentication
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

