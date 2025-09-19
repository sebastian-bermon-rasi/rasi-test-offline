package com.rasi.med.sync;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {
    private final String expected;

    public ApiKeyFilter(){
        this.expected = System.getenv().getOrDefault("CENTRAL_API_KEY", "dev-key");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String key = req.getHeader("X-Api-Key");
        if (key == null || !key.equals(expected)) {
            ((HttpServletResponse)response).sendError(401, "Invalid API key");
            return;
        }
        chain.doFilter(request, response);
    }
}
