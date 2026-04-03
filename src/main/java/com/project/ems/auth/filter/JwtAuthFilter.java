package com.project.ems.auth.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.ems.auth.repository.RevokedTokenRepository;
import com.project.ems.auth.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, RevokedTokenRepository revokedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (revokedTokenRepository.existsByToken(token)) {
            sendUnauthorized(response, "Token has been revoked. Please log in again.");
            return;
        }

        if (jwtUtil.isTokenExpired(token)) {
            sendUnauthorized(response, "Token has expired. Please log in again.");
            return;
        }

        if (!jwtUtil.isTokenValid(token)) {
            sendUnauthorized(response, "Invalid or malformed token.");
            return;
        }

        if (!jwtUtil.isAccessToken(token)) {
            sendUnauthorized(response, "Refresh tokens cannot be used for API access.");
            return;
        }

        Long userId = jwtUtil.extractUserId(token);
        String role  = jwtUtil.extractRole(token);

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
            String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message)
        );
    }
}