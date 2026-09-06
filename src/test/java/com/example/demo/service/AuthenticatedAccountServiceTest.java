package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;

class AuthenticatedAccountServiceTest {

    private AccountDAO accountDAO;
    private AuthenticatedAccountService service;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        service = new AuthenticatedAccountService();
        ReflectionTestUtils.setField(service, "accountDAO", accountDAO);
    }

    @Test
    void resolve_returnsNullForNullAuthentication() {
        assertNull(service.resolve(null));

        verifyNoInteractions(accountDAO);
    }

    @Test
    void resolve_returnsNullForUnauthenticatedAuthentication() {
        assertNull(service.resolve(new UsernamePasswordAuthenticationToken("buyer", "n/a")));

        verifyNoInteractions(accountDAO);
    }

    @Test
    void resolve_returnsNullForAnonymousAuthentication() {
        Authentication anonymous = mock(Authentication.class);
        when(anonymous.isAuthenticated()).thenReturn(true);
        when(anonymous.getName()).thenReturn("anonymousUser");

        assertNull(service.resolve(anonymous));

        verifyNoInteractions(accountDAO);
    }

    @Test
    void resolve_returnsDirectUsernameAccountWithoutInspectingPrincipal() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("buyer");
        Account account = accountWithUsername("buyer");
        when(accountDAO.findAccount("buyer")).thenReturn(account);

        assertSame(account, service.resolve(auth));

        verify(auth, never()).getPrincipal();
        verify(accountDAO, never()).findAccountByEmail(any());
    }

    @Test
    void resolve_returnsNullForMissingNonOAuthPrincipal() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("missing");
        when(auth.getPrincipal()).thenReturn("missing");

        assertNull(service.resolve(auth));

        verify(accountDAO, never()).findAccountByEmail(any());
    }

    @Test
    void resolve_returnsNullForOAuthPrincipalWithoutEmail() {
        Authentication auth = mock(Authentication.class);
        OAuth2User principal = mock(OAuth2User.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("Google User");
        when(auth.getPrincipal()).thenReturn(principal);
        when(principal.getAttribute("email")).thenReturn(null);

        assertNull(service.resolve(auth));

        verify(accountDAO, never()).findAccountByEmail(any());
    }

    @Test
    void resolve_normalizesOAuthEmailBeforeLookup() {
        Authentication auth = mock(Authentication.class);
        OAuth2User principal = mock(OAuth2User.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("Google User");
        when(auth.getPrincipal()).thenReturn(principal);
        when(principal.getAttribute("email")).thenReturn(" Buyer@Example.COM ");
        Account account = accountWithUsername("buyer");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(account);

        assertSame(account, service.resolve(auth));
    }

    private Account accountWithUsername(String username) {
        Account account = new Account();
        account.setUserName(username);
        return account;
    }
}
