package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.csrf.CsrfToken;

class CsrfTokenControllerAdviceTest {

    private final CsrfTokenControllerAdvice advice = new CsrfTokenControllerAdvice();

    @Test
    void initializeCsrfToken_ignoresMissingAttribute() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        assertDoesNotThrow(() -> advice.initializeCsrfToken(request));

        verify(request).getAttribute(CsrfToken.class.getName());
    }

    @Test
    void initializeCsrfToken_ignoresWronglyTypedAttribute() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn("not-a-token");

        assertDoesNotThrow(() -> advice.initializeCsrfToken(request));

        verify(request).getAttribute(CsrfToken.class.getName());
    }

    @Test
    void initializeCsrfToken_materializesTokenAttribute() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        CsrfToken token = mock(CsrfToken.class);
        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(token);

        advice.initializeCsrfToken(request);

        verify(token).getToken();
    }
}
