package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestOperations;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;

class CustomOAuth2UserServiceTest {

    private static final Instant ACCESS_TOKEN_ISSUED_AT = Instant.parse("2026-01-01T00:00:00Z");

    private AccountDAO accountDAO;
    private RestOperations restOperations;
    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        restOperations = mock(RestOperations.class);
        service = new CustomOAuth2UserService();
        ReflectionTestUtils.setField(service, "accountDAO", accountDAO);
        service.setRestOperations(restOperations);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonPersistedGoogleProfiles")
    void loadUser_skipsPersistenceWhenEmailIsMissing(
            String caseName, Map<String, Object> attributes, String expectedName) {
        OAuth2User result = loadUserWithAttributes(attributes);

        assertEquals(expectedName, result.getName());

        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    static Stream<Arguments> nonPersistedGoogleProfiles() {
        Map<String, Object> missingNameKey = googleAttributes(" ", null, null, "sub-3");
        missingNameKey.remove("name");
        return Stream.of(
                Arguments.of("null email uses Google name",
                        googleAttributes(null, " Google User ", "pic", "sub-1"), " Google User "),
                Arguments.of("blank email falls back to email attribute",
                        googleAttributes("   ", null, null, "sub-2"), "   "),
                Arguments.of("absent name key falls back to email attribute",
                        missingNameKey, " "));
    }

    @Test
    void loadUser_createsNewGoogleAccountUsingEmailPrefixAndTrimmedName() {
        Map<String, Object> attributes = googleAttributes("Buyer@Example.COM", " Buyer Name ",
                "avatar.png", "google-sub");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(null);
        when(accountDAO.findAccount("Buyer")).thenReturn(null);

        OAuth2User result = loadUserWithAttributes(attributes);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountDAO).saveAccount(accountCaptor.capture());
        Account created = accountCaptor.getValue();
        assertEquals("Buyer", created.getUserName());
        assertEquals("Buyer Name", created.getFullName());
        assertEquals("buyer@example.com", created.getEmail());
        assertEquals("avatar.png", created.getAvatarUrl());
        assertEquals("google-sub", created.getProviderId());
        assertEquals("", created.getEncrytedPassword());
        assertTrue(created.isActive());
        assertTrue(created.isAccountNonLocked());
        assertEquals(Account.ROLE_USER, created.getUserRole());
        assertEquals("GOOGLE", created.getProvider());
        assertNotNull(created.getLastLogin());
        assertEquals(" Buyer Name ", result.getName());
    }

    @Test
    void loadUser_resolvesUsernameCollisionAndFallsBackToEmailPrefixForNullName() {
        Map<String, Object> attributes = googleAttributes("buyer@example.com", null, null, null);
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(null);
        when(accountDAO.findAccount("buyer")).thenReturn(new Account());

        OAuth2User result = loadUserWithAttributes(attributes);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountDAO).saveAccount(accountCaptor.capture());
        Account created = accountCaptor.getValue();
        assertTrue(created.getUserName().matches("buyer_[0-9]{1,4}"));
        assertEquals(created.getUserName(), created.getFullName());
        assertEquals("buyer@example.com", result.getName());
    }

    @Test
    void loadUser_updatesExistingAccountWithLatestNonNullGoogleFields() {
        Account account = accountWithUsername("buyer");
        account.setFullName("Old Name");
        account.setAvatarUrl("old.png");
        account.setProviderId("old-sub");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(account);
        Map<String, Object> attributes = googleAttributes("buyer@example.com", " New Name ",
                "new.png", "new-sub");

        OAuth2User result = loadUserWithAttributes(attributes);

        assertEquals("New Name", account.getFullName());
        assertEquals("new.png", account.getAvatarUrl());
        assertEquals("new-sub", account.getProviderId());
        assertNotNull(account.getLastLogin());
        verify(accountDAO).saveAccount(account);
        assertEquals(" New Name ", result.getName());
    }

    @Test
    void loadUser_preservesOptionalExistingFieldsAndUsesUsernameWhenNameIsNull() {
        Account account = accountWithUsername("buyer");
        account.setFullName("Old Name");
        account.setAvatarUrl("old.png");
        account.setProviderId("old-sub");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(account);
        Map<String, Object> attributes = googleAttributes("buyer@example.com", null, null, null);

        OAuth2User result = loadUserWithAttributes(attributes);

        assertEquals("buyer", account.getFullName());
        assertEquals("old.png", account.getAvatarUrl());
        assertEquals("old-sub", account.getProviderId());
        verify(accountDAO).saveAccount(account);
        assertEquals("buyer@example.com", result.getName());
    }

    private OAuth2User loadUserWithAttributes(Map<String, Object> attributes) {
        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(attributes, HttpStatus.OK);
        when(restOperations.exchange(any(RequestEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                        .thenReturn(response);
        return service.loadUser(googleUserRequest());
    }

    private OAuth2UserRequest googleUserRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("client")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://oauth.test/authorize")
                .tokenUri("https://oauth.test/token")
                .userInfoUri("https://oauth.test/userinfo")
                .userNameAttributeName("id")
                .clientName("Google")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "token", ACCESS_TOKEN_ISSUED_AT, ACCESS_TOKEN_ISSUED_AT.plusSeconds(60));
        return new OAuth2UserRequest(registration, accessToken);
    }

    private static Map<String, Object> googleAttributes(
            String email, String name, String picture, String providerId) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "oauth-user-id");
        attributes.put("sub", providerId);
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("picture", picture);
        return attributes;
    }

    private Account accountWithUsername(String username) {
        Account account = new Account();
        account.setUserName(username);
        return account;
    }
}
