package com.rasi.med.security;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class ApiKeyFilter extends OncePerRequestFilter {

    private final String requiredApiKey;

    public ApiKeyFilter(String requiredApiKey) {
        this.requiredApiKey = requiredApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // solo protege los endpoints de sync (ajusta el path si cambias)
        String path = request.getRequestURI();
        if (path.startsWith("/api/sync/")) {
            String header = request.getHeader("X-Api-Key");
            if (header == null || !header.equals(requiredApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("missing/invalid api key");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
