package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;

class GlobalControllerAdviceTest {

    private AccountDAO accountDAO;
    private GlobalControllerAdvice advice;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        advice = new GlobalControllerAdvice();
        ReflectionTestUtils.setField(advice, "accountDAO", accountDAO);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void attributes_areAbsentWhenAuthenticationIsMissing() {
        assertNull(advice.getUserAvatarUrl());
        assertNull(advice.getUserDisplayName());
    }

    @Test
    void attributes_areAbsentWhenAuthenticationIsInactive() {
        installAuthentication(false, "buyer", "buyer");

        assertNull(advice.getUserAvatarUrl());
        assertNull(advice.getUserDisplayName());

        verify(accountDAO, never()).findAccount("buyer");
    }

    @Test
    void attributes_areAbsentForAnonymousAuthentication() {
        installAuthentication(true, "anonymousUser", "anonymousUser");

        assertNull(advice.getUserAvatarUrl());
        assertNull(advice.getUserDisplayName());
    }

    @Test
    void oauthNonBlankAttributes_takePriorityOverStoredProfile() {
        OAuth2User principal = oauthPrincipal("https://images.example/avatar.png", "OAuth Name", null);
        installAuthentication(true, principal, "buyer@example.com");

        assertEquals("https://images.example/avatar.png", advice.getUserAvatarUrl());
        assertEquals("OAuth Name", advice.getUserDisplayName());

        verify(accountDAO, never()).findAccount("buyer@example.com");
    }

    @Test
    void oauthBlankAttributes_fallBackToAccountFoundByUsername() {
        OAuth2User principal = oauthPrincipal("   ", "   ", "buyer@example.com");
        Account account = accountWithProfile("stored.png", "Stored Name");
        when(accountDAO.findAccount("buyer")).thenReturn(account);
        installAuthentication(true, principal, "buyer");

        assertEquals("stored.png", advice.getUserAvatarUrl());
        assertEquals("Stored Name", advice.getUserDisplayName());

        verify(accountDAO, never()).findAccountByEmail("buyer@example.com");
    }

    @Test
    void oauthNullAttributes_useNormalizedEmailForAvatarWhenUsernameMisses() {
        OAuth2User principal = oauthPrincipal(null, null, "Buyer@Example.COM");
        Account account = accountWithProfile("stored.png", "Stored Name");
        when(accountDAO.findAccount("oauth-user")).thenReturn(null);
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(account);
        installAuthentication(true, principal, "oauth-user");

        assertEquals("stored.png", advice.getUserAvatarUrl());
        assertEquals("oauth-user", advice.getUserDisplayName());

        verify(accountDAO).findAccountByEmail("buyer@example.com");
    }

    @Test
    void oauthMissingEmailAndAccounts_returnsNoAvatarAndUsernameAsDisplayName() {
        OAuth2User principal = oauthPrincipal(null, null, null);
        when(accountDAO.findAccount("oauth-user")).thenReturn(null);
        installAuthentication(true, principal, "oauth-user");

        assertNull(advice.getUserAvatarUrl());
        assertEquals("oauth-user", advice.getUserDisplayName());
    }

    @Test
    void localAccount_nonBlankProfileValuesAreReturned() {
        Account account = accountWithProfile("local.png", "Local Name");
        when(accountDAO.findAccount("buyer")).thenReturn(account);
        installAuthentication(true, "buyer", "buyer");

        assertEquals("local.png", advice.getUserAvatarUrl());
        assertEquals("Local Name", advice.getUserDisplayName());
    }

    @Test
    void localAccountMissing_returnsNullAvatarAndUsernameAsDisplayName() {
        when(accountDAO.findAccount("buyer")).thenReturn(null);
        installAuthentication(true, "buyer", "buyer");

        assertNull(advice.getUserAvatarUrl());
        assertEquals("buyer", advice.getUserDisplayName());
    }

    @Test
    void nullStoredProfileValues_useFallbacks() {
        Account account = accountWithProfile(null, null);
        when(accountDAO.findAccount("buyer")).thenReturn(account);
        installAuthentication(true, "buyer", "buyer");

        assertNull(advice.getUserAvatarUrl());
        assertEquals("buyer", advice.getUserDisplayName());
    }

    @Test
    void blankStoredProfileValues_useFallbacks() {
        Account account = accountWithProfile("   ", "   ");
        when(accountDAO.findAccount("buyer")).thenReturn(account);
        installAuthentication(true, "buyer", "buyer");

        assertNull(advice.getUserAvatarUrl());
        assertEquals("buyer", advice.getUserDisplayName());
    }

    @Test
    void nullUsername_doesNotQueryAccount() {
        installAuthentication(true, "principal", null);

        assertNull(advice.getUserAvatarUrl());
        assertNull(advice.getUserDisplayName());

        verify(accountDAO, never()).findAccount(null);
    }

    @Test
    void blankUsername_doesNotQueryAccountAndRemainsDisplayFallback() {
        installAuthentication(true, "principal", "   ");

        assertNull(advice.getUserAvatarUrl());
        assertEquals("   ", advice.getUserDisplayName());

        verify(accountDAO, never()).findAccount("   ");
    }

    private Authentication installAuthentication(boolean authenticated, Object principal, String name) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(authenticated);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getName()).thenReturn(name);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private OAuth2User oauthPrincipal(String picture, String name, String email) {
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("picture")).thenReturn(picture);
        when(principal.getAttribute("name")).thenReturn(name);
        when(principal.getAttribute("email")).thenReturn(email);
        return principal;
    }

    private Account accountWithProfile(String avatarUrl, String fullName) {
        Account account = new Account();
        account.setAvatarUrl(avatarUrl);
        account.setFullName(fullName);
        return account;
    }
}
