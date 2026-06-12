package com.example.webapp.security;

import com.example.webapp.service.LicenseService;
import com.example.webapp.service.LicenseStatus;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LicenseFilter implements Filter {

    @Autowired
    private LicenseService licenseService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // 1. Always allow license status check and activation endpoint
        if (path.startsWith("/api/license/status") || path.startsWith("/api/license/activate")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Always allow static web resources so the login/unlock page can be loaded
        if (path.equals("/") || path.equals("/index.html") || path.equals("/favicon.ico")
                || path.startsWith("/assets/") || path.startsWith("/css/") 
                || path.startsWith("/js/") || path.startsWith("/images/") 
                || path.startsWith("/static/")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Block all other requests if license status is EXPIRED or TAMPERED
        LicenseStatus status = licenseService.getLicenseStatus();
        if (status == LicenseStatus.EXPIRED || status == LicenseStatus.TAMPERED) {
            httpResponse.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED); // 402 Payment Required
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(
                "{\"error\":\"TRIAL_EXPIRED\"," +
                "\"status\":\"" + status.name() + "\"," +
                "\"message\":\"Your trial has expired. Please reach out to the system administrator to get the full version.\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }
}
