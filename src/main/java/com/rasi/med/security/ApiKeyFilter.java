package com.rasi.med.security;

import org.springframework.context.annotation.Profile;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Profile("central")
public class ApiKeyFilter implements Filter {

    private final String apiKey;

    public ApiKeyFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();

        // ✅ Aplica seguridad SOLO al namespace de sync
        if (!uri.startsWith("/api/sync/")) {
            chain.doFilter(req, res);
            return;
        }

        String header = request.getHeader("X-Api-Key");
        if (header == null || !header.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing/invalid API key");
            return;
        }

        chain.doFilter(req, res);
    }
}
