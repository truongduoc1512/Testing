package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.entity.UserAddress;
import com.example.demo.form.UserAddressForm;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserAddressDAOTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private UserAddressDAO dao;

    @BeforeEach
    void setUp() {
        dao = new UserAddressDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void getUserAddresses_returnsEmptyForMissingUsername(String username) {
        assertTrue(dao.getUserAddresses(username).isEmpty());
        verify(session, never()).createQuery(anyString(), any(Class.class));
    }

    @Test
    void getUserAddresses_bindsUsernameAndOrdersDefaultsFirst() {
        Query<UserAddress> query = addressListQuery(Collections.singletonList(address("alice", false)));

        assertEquals(1, dao.getUserAddresses("alice").size());
        verify(query).setParameter("username", "alice");
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("a.isDefault desc"),
                org.mockito.ArgumentMatchers.eq(UserAddress.class));
    }

    @Test
    void getAddressById_returnsNullForNullId() {
        assertNull(dao.getAddressById(null));
        verify(session, never()).find(any(Class.class), any());
    }

    @Test
    void getAddressById_delegatesLookup() {
        UserAddress address = address("alice", false);
        when(session.find(UserAddress.class, 1L)).thenReturn(address);

        assertSame(address, dao.getAddressById(1L));
    }

    @Test
    void unsetPreviousDefault_executesBulkUpdate() {
        @SuppressWarnings("unchecked")
        Query<Object> query = mock(Query.class);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        dao.unsetPreviousDefault("alice");

        verify(query).setParameter("username", "alice");
        verify(query).executeUpdate();
    }

    @Test
    void saveAddress_returnsNullForNullUsername() {
        assertNull(dao.saveAddress(null, validForm()));
        verify(session, never()).saveOrUpdate(any());
    }

    @Test
    void saveAddress_returnsNullForNullForm() {
        assertNull(dao.saveAddress("alice", null));
        verify(session, never()).saveOrUpdate(any());
    }

    @Test
    void saveAddress_makesFirstAddressDefaultAndUnsetsPreviousDefaults() {
        addressListQuery(Collections.emptyList());
        bulkUpdateQuery();

        UserAddress saved = dao.saveAddress("alice", validForm());

        assertTrue(saved.isDefault());
        assertEquals("alice", saved.getUsername());
        verify(session).saveOrUpdate(saved);
        verify(session).createQuery(org.mockito.ArgumentMatchers.contains("Set a.isDefault = false"));
    }

    @Test
    void saveAddress_keepsNonFirstAddressNonDefaultWhenNotRequested() {
        addressListQuery(Collections.singletonList(address("alice", true)));

        UserAddress saved = dao.saveAddress("alice", validForm());

        assertFalse(saved.isDefault());
        verify(session, never()).createQuery(org.mockito.ArgumentMatchers.contains("Set a.isDefault = false"));
    }

    @Test
    void saveAddress_requestedDefaultUnsetsOldDefault() {
        addressListQuery(Collections.singletonList(address("alice", true)));
        bulkUpdateQuery();
        UserAddressForm form = validForm();
        form.setDefault(true);

        UserAddress saved = dao.saveAddress("alice", form);

        assertTrue(saved.isDefault());
        verify(session).saveOrUpdate(saved);
    }

    @Test
    void saveAddress_updatesOwnedAddressAndTrimsFields() {
        UserAddress existing = address("alice", false);
        existing.setId(5L);
        addressListQuery(Collections.singletonList(existing));
        when(session.find(UserAddress.class, 5L)).thenReturn(existing);
        UserAddressForm form = validForm();
        form.setId(5L);
        form.setReceiverName(" Alice ");
        form.setNote(" note ");

        UserAddress saved = dao.saveAddress("alice", form);

        assertSame(existing, saved);
        assertEquals("Alice", saved.getReceiverName());
        assertEquals("note", saved.getNote());
        assertNotNull(saved.getUpdatedAt());
        verify(session).saveOrUpdate(existing);
    }

    @Test
    void saveAddress_rejectsForeignAddress() {
        UserAddress foreign = address("bob", false);
        addressListQuery(Collections.singletonList(address("alice", false)));
        when(session.find(UserAddress.class, 5L)).thenReturn(foreign);
        UserAddressForm form = validForm();
        form.setId(5L);

        assertNull(dao.saveAddress("alice", form));
        verify(session, never()).saveOrUpdate(any());
    }

    @Test
    void saveAddress_currentlyUnsetsDefaultsBeforeRejectingForeignAddress_characterization() {
        UserAddress foreign = address("bob", false);
        addressListQuery(Collections.singletonList(address("alice", true)));
        Query<?> bulk = bulkUpdateQuery();
        when(session.find(UserAddress.class, 5L)).thenReturn(foreign);
        UserAddressForm form = validForm();
        form.setId(5L);
        form.setDefault(true);

        assertNull(dao.saveAddress("alice", form));
        verify(bulk).executeUpdate();
    }

    @Test
    void saveAddress_createsNewAddressWhenRequestedIdDoesNotExist() {
        addressListQuery(Collections.singletonList(address("alice", false)));
        when(session.find(UserAddress.class, 404L)).thenReturn(null);
        UserAddressForm form = validForm();
        form.setId(404L);

        UserAddress saved = dao.saveAddress("alice", form);

        assertEquals("alice", saved.getUsername());
        verify(session).saveOrUpdate(saved);
    }

    @Test
    void saveAddress_currentlyThrowsForNullRequiredField_characterization() {
        addressListQuery(Collections.singletonList(address("alice", false)));
        UserAddressForm form = validForm();
        form.setPhone(null);

        assertThrows(NullPointerException.class, () -> dao.saveAddress("alice", form));
        verify(session, never()).saveOrUpdate(any());
    }

    @Test
    void deleteAddress_returnsFalseWhenMissing() {
        when(session.find(UserAddress.class, 1L)).thenReturn(null);

        assertFalse(dao.deleteAddress("alice", 1L));
        verify(session, never()).delete(any());
    }

    @Test
    void deleteAddress_returnsFalseForForeignOwner() {
        UserAddress foreign = address("bob", false);
        when(session.find(UserAddress.class, 1L)).thenReturn(foreign);

        assertFalse(dao.deleteAddress("alice", 1L));
        verify(session, never()).delete(any());
    }

    @Test
    void deleteAddress_removesNonDefaultWithoutPromotion() {
        UserAddress existing = address("alice", false);
        when(session.find(UserAddress.class, 1L)).thenReturn(existing);

        assertTrue(dao.deleteAddress("alice", 1L));
        verify(session).delete(existing);
        verify(session).flush();
        verify(session, never()).update(any());
    }

    @Test
    void deleteAddress_doesNotPromoteWhenNoAddressRemains() {
        UserAddress existing = address("alice", true);
        when(session.find(UserAddress.class, 1L)).thenReturn(existing);
        addressListQuery(Collections.emptyList());

        assertTrue(dao.deleteAddress("alice", 1L));
        verify(session, never()).update(any());
    }

    @Test
    void deleteAddress_promotesFirstRemainingAddress() {
        UserAddress existing = address("alice", true);
        UserAddress remaining = address("alice", false);
        when(session.find(UserAddress.class, 1L)).thenReturn(existing);
        addressListQuery(Collections.singletonList(remaining));

        assertTrue(dao.deleteAddress("alice", 1L));
        assertTrue(remaining.isDefault());
        verify(session).update(remaining);
    }

    @Test
    void setDefaultAddress_returnsFalseWhenMissing() {
        when(session.find(UserAddress.class, 1L)).thenReturn(null);

        assertFalse(dao.setDefaultAddress("alice", 1L));
    }

    @Test
    void setDefaultAddress_returnsFalseForForeignOwner() {
        when(session.find(UserAddress.class, 1L)).thenReturn(address("bob", false));

        assertFalse(dao.setDefaultAddress("alice", 1L));
    }

    @Test
    void setDefaultAddress_unsetsOldDefaultAndUpdatesTarget() {
        UserAddress target = address("alice", false);
        target.setUpdatedAt(new Date(1));
        when(session.find(UserAddress.class, 1L)).thenReturn(target);
        Query<?> bulk = bulkUpdateQuery();

        assertTrue(dao.setDefaultAddress("alice", 1L));
        assertTrue(target.isDefault());
        assertTrue(target.getUpdatedAt().after(new Date(1)));
        verify(bulk).executeUpdate();
        verify(session).update(target);
    }

    @SuppressWarnings("unchecked")
    private Query<UserAddress> addressListQuery(List<UserAddress> rows) {
        Query<UserAddress> query = mock(Query.class);
        when(session.createQuery(org.mockito.ArgumentMatchers.contains("Select a from"), any(Class.class)))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(rows);
        return query;
    }

    @SuppressWarnings("unchecked")
    private Query<?> bulkUpdateQuery() {
        Query<Object> query = mock(Query.class);
        when(session.createQuery(org.mockito.ArgumentMatchers.contains("Set a.isDefault = false")))
                .thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        return query;
    }

    private UserAddressForm validForm() {
        UserAddressForm form = new UserAddressForm();
        form.setReceiverName(" Receiver ");
        form.setPhone(" 0900 ");
        form.setProvince(" Province ");
        form.setDistrict(" District ");
        form.setWard(" Ward ");
        form.setStreetAddress(" Street ");
        return form;
    }

    private UserAddress address(String username, boolean isDefault) {
        UserAddress address = new UserAddress();
        address.setUsername(username);
        address.setDefault(isDefault);
        return address;
    }
}
