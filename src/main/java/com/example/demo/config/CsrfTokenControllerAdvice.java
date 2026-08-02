package com.example.demo.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Materializes the lazily-created CSRF token before Thymeleaf starts writing
 * large responses. This prevents a late POST form from trying to create the
 * HTTP session after the response has already been committed.
 */
@ControllerAdvice
public class CsrfTokenControllerAdvice {

    @ModelAttribute
    public void initializeCsrfToken(HttpServletRequest request) {
        Object attribute = request.getAttribute(CsrfToken.class.getName());
        if (attribute instanceof CsrfToken) {
            ((CsrfToken) attribute).getToken();
        }
    }
}
