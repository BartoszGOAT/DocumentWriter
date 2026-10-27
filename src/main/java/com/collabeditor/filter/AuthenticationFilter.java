package com.collabeditor.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthenticationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        HttpSession session = httpRequest.getSession(false);
        
        boolean isLoggedIn = (session != null && session.getAttribute("userId") != null);
        
        // Allow WebSocket connections (they handle their own auth)
        String requestURI = httpRequest.getRequestURI();
        boolean isWebSocket = requestURI.contains("/editor/");
        
        if (isLoggedIn || isWebSocket) {
            // User is logged in, proceed with the request
            chain.doFilter(request, response);
        } else {
            // User is not logged in, redirect to login page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}