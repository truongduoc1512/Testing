package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.stream.Stream;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.Account;
import com.example.demo.pagination.PaginationResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private AccountDAO dao;

    @BeforeEach
    void setUp() {
        dao = new AccountDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void findAccount_delegatesMissingUsernameWithoutGuard_characterization(String username) {
        Account expected = new Account();
        when(session.find(Account.class, username)).thenReturn(expected);

        assertSame(expected, dao.findAccount(username));
        verify(session).find(Account.class, username);
    }

    @Test
    void findAccount_preservesUsernameWithoutNormalization_characterization() {
        Account expected = new Account();
        when(session.find(Account.class, "  alice  ")).thenReturn(expected);

        assertSame(expected, dao.findAccount("  alice  "));
        verify(session).find(Account.class, "  alice  ");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void findAccountByEmail_rejectsMissingEmail(String email) {
        assertNull(dao.findAccountByEmail(email));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findAccountByEmail_preservesNonBlankEmailWithoutNormalization_characterization() {
        @SuppressWarnings("unchecked")
        Query<Account> query = mock(Query.class);
        Account expected = new Account();
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter("email", " Alice@Example.COM ")).thenReturn(query);
        when(query.uniqueResult()).thenReturn(expected);

        assertSame(expected, dao.findAccountByEmail(" Alice@Example.COM "));
        verify(query).setParameter("email", " Alice@Example.COM ");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void findAccountByResetToken_rejectsMissingToken(String token) {
        assertNull(dao.findAccountByResetToken(token));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void findAccountByResetToken_hashesTrimmedTokenAndUsesCurrentTime() {
        @SuppressWarnings("unchecked")
        Query<Account> query = mock(Query.class);
        Account expected = new Account();
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResult()).thenReturn(expected);

        assertSame(expected, dao.findAccountByResetToken(" raw-token "));
        verify(query).setParameter("token", DaoTestSupport.sha256("raw-token"));
        verify(query).setParameter(org.mockito.ArgumentMatchers.eq("now"), any(Date.class));
    }

    static Stream<Arguments> invalidResetTokenInputs() {
        Account account = new Account();
        Date expiry = new Date(System.currentTimeMillis() + 60_000);
        return Stream.of(
                Arguments.of(null, "token", expiry),
                Arguments.of(account, null, expiry),
                Arguments.of(account, "   ", expiry),
                Arguments.of(account, "token", null));
    }

    @ParameterizedTest
    @MethodSource("invalidResetTokenInputs")
    void savePasswordResetToken_rejectsIncompleteInput(Account account, String token, Date expiry) {
        assertThrows(IllegalArgumentException.class,
                () -> dao.savePasswordResetToken(account, token, expiry));
        verify(session, never()).saveOrUpdate(any());
    }

    @Test
    void savePasswordResetToken_storesHashAndExpiry() {
        Account account = new Account();
        Date expiry = new Date(System.currentTimeMillis() + 60_000);

        dao.savePasswordResetToken(account, " token ", expiry);

        assertEquals(DaoTestSupport.sha256("token"), account.getResetToken());
        assertEquals(expiry, account.getResetTokenExpiresAt());
        verify(session).saveOrUpdate(account);
    }

    static Stream<Arguments> invalidPasswordResetInputs() {
        return Stream.of(
                Arguments.of(null, "encoded"),
                Arguments.of("", "encoded"),
                Arguments.of("   ", "encoded"),
                Arguments.of("token", null),
                Arguments.of("token", ""));
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordResetInputs")
    void resetPassword_returnsFalseForInvalidInput(String token, String password) {
        assertFalse(dao.resetPassword(token, password));
        verify(session, never()).createQuery(anyString());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2 })
    void resetPassword_returnsFalseUnlessExactlyOneTokenIsConsumed(int updatedRows) {
        @SuppressWarnings("unchecked")
        Query<Object> query = mock(Query.class);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(updatedRows);

        assertFalse(dao.resetPassword(" token ", "encoded"));
        verify(query).setParameter("token", DaoTestSupport.sha256("token"));
    }

    @Test
    void resetPassword_returnsTrueAndSetsAtomicUpdateParameters() {
        @SuppressWarnings("unchecked")
        Query<Object> query = mock(Query.class);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        assertTrue(dao.resetPassword(" token ", "encoded"));
        verify(query).setParameter("password", "encoded");
        verify(query).setParameter("token", DaoTestSupport.sha256("token"));
        verify(query).setParameter(org.mockito.ArgumentMatchers.eq("now"), any(Date.class));
    }

    @Test
    void saveAccount_refreshesUpdatedAtAndDelegatesPersistence() {
        Account account = new Account();
        account.setUpdatedAt(new Date(1));

        dao.saveAccount(account);

        assertTrue(account.getUpdatedAt().after(new Date(1)));
        verify(session).saveOrUpdate(account);
    }

    @Test
    void listAccounts_buildsPaginationFromDescendingCreatedDateQuery() {
        @SuppressWarnings("unchecked")
        Query<Account> query = mock(Query.class);
        ScrollableResults scroll = mock(ScrollableResults.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.scroll(ScrollMode.SCROLL_INSENSITIVE)).thenReturn(scroll);
        when(scroll.first()).thenReturn(false);
        when(scroll.getRowNumber()).thenReturn(-1);

        PaginationResult<Account> result = dao.listAccounts(1, 10, 5);

        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getList().isEmpty());
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("Order by a.createdAt desc"),
                org.mockito.ArgumentMatchers.eq(Account.class));
    }

    @Test
    void countActiveAdmins_returnsTypedAggregate() {
        @SuppressWarnings("unchecked")
        Query<Long> query = mock(Query.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(Long.class))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(3L);

        assertEquals(3L, dao.countActiveAdmins());
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("accountNonLocked = true"),
                org.mockito.ArgumentMatchers.eq(Long.class));
    }
}
