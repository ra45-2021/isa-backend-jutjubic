package com.jutjubic.security;

import com.jutjubic.service.ActiveUserMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ActiveUserMetrics activeUserMetrics;

    public JwtRequestFilter(JwtUtil jwtUtil, ActiveUserMetrics activeUserMetrics) {
        this.jwtUtil = jwtUtil;
        this.activeUserMetrics = activeUserMetrics;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("/api/popular/run-etl".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        if (path.startsWith("/uploads/") || path.startsWith("/media/") || path.startsWith("/ws/")) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.warn("Invalid JWT token: " + e.getMessage());
            }
        }

        if (username != null) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            activeUserMetrics.recordActivity(username);
        }


        chain.doFilter(request, response);
    }
}