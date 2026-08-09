package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import javax.persistence.LockModeType;

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

import com.example.demo.entity.Voucher;
import com.example.demo.entity.VoucherUsage;
import com.example.demo.form.VoucherForm;
import com.example.demo.model.VoucherApplyResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VoucherDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private VoucherDAO dao;

    @BeforeEach
    void setUp() {
        dao = new VoucherDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void findVoucher_rejectsMissingCode(String code) {
        assertEquals(null, dao.findVoucher(code));
        verify(session, never()).find(any(Class.class), any());
    }

    @Test
    void findVoucher_normalizesCode() {
        Voucher voucher = voucher("SALE10");
        when(session.find(Voucher.class, "SALE10")).thenReturn(voucher);

        assertSame(voucher, dao.findVoucher(" sale10 "));
    }

    @Test
    void listActiveVouchers_appliesActiveExpiryAndUsageFilters() {
        @SuppressWarnings("unchecked")
        Query<Voucher> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(voucher("A")));

        assertEquals(1, dao.listActiveVouchers().size());
        verify(query).setParameter(org.mockito.ArgumentMatchers.eq("now"), any(Date.class));
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("v.usedCount < v.usageLimit"),
                org.mockito.ArgumentMatchers.eq(Voucher.class));
    }

    @Test
    void listAllVouchers_returnsDescendingCreatedRows() {
        @SuppressWarnings("unchecked")
        Query<Voucher> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(voucher("A")));

        assertEquals(1, dao.listAllVouchers().size());
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("Order by v.createdAt desc"),
                org.mockito.ArgumentMatchers.eq(Voucher.class));
    }

    @Test
    void saveVoucher_createsAndCopiesEveryFormField() {
        VoucherForm form = form(" sale10 ");
        Date expiry = new Date(System.currentTimeMillis() + 86_400_000);
        form.setDiscountType(Voucher.TYPE_FIXED);
        form.setDiscountValue(25);
        form.setMaxDiscount(20.0);
        form.setMinOrderValue(100);
        form.setExpiryDate(expiry);
        form.setActive(false);
        form.setUsageLimit(7);
        form.setPerUserLimit(2);
        when(session.find(Voucher.class, "SALE10")).thenReturn(null);

        dao.saveVoucher(form);

        org.mockito.ArgumentCaptor<Voucher> captor = org.mockito.ArgumentCaptor.forClass(Voucher.class);
        verify(session).saveOrUpdate(captor.capture());
        Voucher saved = captor.getValue();
        assertEquals("SALE10", saved.getCode());
        assertEquals(Voucher.TYPE_FIXED, saved.getDiscountType());
        assertEquals(25, saved.getDiscountValue());
        assertEquals(20.0, saved.getMaxDiscount());
        assertEquals(100, saved.getMinOrderValue());
        assertEquals(expiry, saved.getExpiryDate());
        assertFalse(saved.isActive());
        assertEquals(7, saved.getUsageLimit());
        assertEquals(2, saved.getPerUserLimit());
    }

    @Test
    void saveVoucher_updatesExistingEntity() {
        Voucher existing = voucher("SALE10");
        VoucherForm form = form("sale10");
        form.setDiscountValue(15);
        when(session.find(Voucher.class, "SALE10")).thenReturn(existing);

        dao.saveVoucher(form);

        assertEquals(15, existing.getDiscountValue());
        verify(session).saveOrUpdate(existing);
    }

    @Test
    void deleteVoucher_returnsFalseWhenMissing() {
        when(session.find(Voucher.class, "MISSING")).thenReturn(null);

        assertFalse(dao.deleteVoucher("missing"));
        verify(session, never()).update(any());
    }

    @Test
    void deleteVoucher_softDeletesExistingVoucher() {
        Voucher voucher = voucher("SALE10");
        when(session.find(Voucher.class, "SALE10")).thenReturn(voucher);

        assertTrue(dao.deleteVoucher("sale10"));
        assertFalse(voucher.isActive());
        verify(session).update(voucher);
    }

    static Stream<Arguments> nullUsageKeys() {
        return Stream.of(Arguments.of(null, "SALE10"), Arguments.of("alice", null));
    }

    @ParameterizedTest
    @MethodSource("nullUsageKeys")
    void getUserVoucherUsageCount_returnsZeroForNullKey(String username, String code) {
        assertEquals(0, dao.getUserVoucherUsageCount(username, code));
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @ParameterizedTest
    @MethodSource("usageCounts")
    void getUserVoucherUsageCount_mapsNullableAggregate(Long aggregate, int expected) {
        usageCountQuery(aggregate);

        assertEquals(expected, dao.getUserVoucherUsageCount("alice", " sale10 "));
    }

    static Stream<Arguments> usageCounts() {
        return Stream.of(Arguments.of(null, 0), Arguments.of(4L, 4));
    }

    @Test
    void recordVoucherUsage_doesNothingForUnknownVoucher() {
        when(session.find(Voucher.class, "MISSING")).thenReturn(null);

        dao.recordVoucherUsage("missing", "alice", "O1");

        verify(session, never()).update(any());
        verify(session, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("usageUsers")
    void recordVoucherUsage_incrementsVoucherAndPersistsUsage(String username) {
        Voucher voucher = voucher("SALE10");
        voucher.setUsedCount(2);
        when(session.find(Voucher.class, "SALE10")).thenReturn(voucher);

        dao.recordVoucherUsage("sale10", username, "O1");

        assertEquals(3, voucher.getUsedCount());
        verify(session).update(voucher);
        verify(session).save(any(VoucherUsage.class));
    }

    static Stream<String> usageUsers() {
        return Stream.of("alice", null);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void validateAndApplyVoucher_rejectsMissingCode(String code) {
        assertFalse(dao.validateAndApplyVoucher(code, 100, "alice").isSuccess());
        verify(session, never()).find(any(Class.class), any());
    }

    @Test
    void validateAndApplyVoucher_rejectsUnknownVoucher() {
        when(session.find(Voucher.class, "MISSING")).thenReturn(null);

        assertFalse(dao.validateAndApplyVoucher("missing", 100, "alice").isSuccess());
    }

    @Test
    void validateAndApplyVoucher_rejectsInactiveVoucher() {
        Voucher voucher = voucher("SALE10");
        voucher.setActive(false);
        stubVoucher(voucher);

        assertFalse(dao.validateAndApplyVoucher("SALE10", 100, "alice").isSuccess());
    }

    @Test
    void validateAndApplyVoucher_rejectsExpiredVoucher() {
        Voucher voucher = voucher("SALE10");
        voucher.setExpiryDate(new Date(System.currentTimeMillis() - 1_000));
        stubVoucher(voucher);

        assertFalse(dao.validateAndApplyVoucher("SALE10", 100, "alice").isSuccess());
    }

    @Test
    void validateAndApplyVoucher_rejectsAmountOneUnitBelowMinimum() {
        Voucher voucher = voucher("SALE10");
        voucher.setMinOrderValue(100);
        stubVoucher(voucher);

        assertFalse(dao.validateAndApplyVoucher("SALE10", 99, null).isSuccess());
    }

    @Test
    void validateAndApplyVoucher_acceptsAmountAtMinimumBoundary() {
        Voucher voucher = voucher("SALE10");
        voucher.setMinOrderValue(100);
        stubVoucher(voucher);

        assertTrue(dao.validateAndApplyVoucher("SALE10", 100, null).isSuccess());
    }

    @ParameterizedTest
    @MethodSource("globalUsageBoundaries")
    void validateAndApplyVoucher_enforcesGlobalUsageBoundary(int usedCount, boolean expected) {
        Voucher voucher = voucher("SALE10");
        voucher.setUsageLimit(10);
        voucher.setUsedCount(usedCount);
        stubVoucher(voucher);

        assertEquals(expected, dao.validateAndApplyVoucher("SALE10", 100, null).isSuccess());
    }

    static Stream<Arguments> globalUsageBoundaries() {
        return Stream.of(Arguments.of(9, true), Arguments.of(10, false), Arguments.of(11, false));
    }

    @ParameterizedTest
    @MethodSource("perUserUsageBoundaries")
    void validateAndApplyVoucher_enforcesPerUserUsageBoundary(int userUsed, boolean expected) {
        Voucher voucher = voucher("SALE10");
        voucher.setPerUserLimit(2);
        stubVoucher(voucher);
        usageCountQuery((long) userUsed);

        assertEquals(expected, dao.validateAndApplyVoucher("SALE10", 100, "alice").isSuccess());
    }

    static Stream<Arguments> perUserUsageBoundaries() {
        return Stream.of(Arguments.of(1, true), Arguments.of(2, false), Arguments.of(3, false));
    }

    @Test
    void validateAndApplyVoucher_skipsPerUserLimitForGuest() {
        Voucher voucher = voucher("SALE10");
        voucher.setPerUserLimit(0);
        stubVoucher(voucher);

        assertTrue(dao.validateAndApplyVoucher("SALE10", 100, "  ").isSuccess());
    }

    @ParameterizedTest
    @MethodSource("percentDiscounts")
    void validateAndApplyVoucher_calculatesPercentDiscount(Double maxDiscount, double expectedDiscount) {
        Voucher voucher = voucher("SALE10");
        voucher.setDiscountType(Voucher.TYPE_PERCENT);
        voucher.setDiscountValue(20);
        voucher.setMaxDiscount(maxDiscount);
        stubVoucher(voucher);

        VoucherApplyResult result = dao.validateAndApplyVoucher("SALE10", 200, null);

        assertTrue(result.isSuccess());
        assertEquals(expectedDiscount, result.getDiscountAmount(), 0.0001);
        assertEquals(200 - expectedDiscount, result.getFinalAmount(), 0.0001);
    }

    static Stream<Arguments> percentDiscounts() {
        return Stream.of(
                Arguments.of(null, 40.0),
                Arguments.of(0.0, 40.0),
                Arguments.of(50.0, 40.0),
                Arguments.of(30.0, 30.0));
    }

    @ParameterizedTest
    @MethodSource("fixedDiscounts")
    void validateAndApplyVoucher_capsFixedDiscountAtOrderAmount(double discount, double expected) {
        Voucher voucher = voucher("FIXED");
        voucher.setDiscountType(Voucher.TYPE_FIXED);
        voucher.setDiscountValue(discount);
        stubVoucher(voucher);

        VoucherApplyResult result = dao.validateAndApplyVoucher("FIXED", 100, null);

        assertEquals(expected, result.getDiscountAmount(), 0.0001);
        assertEquals(100 - expected, result.getFinalAmount(), 0.0001);
    }

    static Stream<Arguments> fixedDiscounts() {
        return Stream.of(Arguments.of(30.0, 30.0), Arguments.of(100.0, 100.0), Arguments.of(150.0, 100.0));
    }

    @Test
    void validateAndApplyVoucher_unknownDiscountTypeCurrentlySucceedsWithZeroDiscount_characterization() {
        Voucher voucher = voucher("UNKNOWN");
        voucher.setDiscountType("BOGUS");
        voucher.setDiscountValue(50);
        stubVoucher(voucher);

        VoucherApplyResult result = dao.validateAndApplyVoucher("UNKNOWN", 100, null);

        assertTrue(result.isSuccess());
        assertEquals(0, result.getDiscountAmount());
        assertEquals(100, result.getFinalAmount());
    }

    @Test
    void validateAndApplyVoucherForCheckout_usesPessimisticWriteLock() {
        Voucher voucher = voucher("SALE10");
        when(session.find(Voucher.class, "SALE10", LockModeType.PESSIMISTIC_WRITE)).thenReturn(voucher);

        assertTrue(dao.validateAndApplyVoucherForCheckout(" sale10 ", 100, null).isSuccess());
        verify(session).find(Voucher.class, "SALE10", LockModeType.PESSIMISTIC_WRITE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void validateAndApplyVoucherForCheckout_rejectsMissingCodeWithoutDatabaseLookup(String code) {
        VoucherApplyResult result = dao.validateAndApplyVoucherForCheckout(code, 100, null);

        assertFalse(result.isSuccess());
        verify(session, never()).find(any(Class.class), any(), any(LockModeType.class));
    }

    private Voucher voucher(String code) {
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setActive(true);
        voucher.setDiscountType(Voucher.TYPE_PERCENT);
        voucher.setDiscountValue(10);
        voucher.setUsageLimit(10);
        voucher.setPerUserLimit(2);
        return voucher;
    }

    private VoucherForm form(String code) {
        VoucherForm form = new VoucherForm();
        form.setCode(code);
        return form;
    }

    private void stubVoucher(Voucher voucher) {
        when(session.find(Voucher.class, voucher.getCode())).thenReturn(voucher);
    }

    @SuppressWarnings("unchecked")
    private void usageCountQuery(Long count) {
        Query<Long> query = mock(Query.class);
        when(session.createQuery(anyString(), any(Class.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResult()).thenReturn(count);
    }
}
