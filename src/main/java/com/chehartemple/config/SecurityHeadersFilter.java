package com.chehartemple.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Prevent MIME type sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // XSS protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Prevent caching of sensitive data
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Strict Transport Security (browser will only use HTTPS for 1 year)
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Prevent referrer leakage
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        chain.doFilter(request, response);
    }
}
