package com.example.demo.dao;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.LockModeType;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;

@ExtendWith(MockitoExtension.class)
class ProductDAOTest {

    private static final int RANDOM_METADATA_SAMPLE_LIMIT = 1_000;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private ProductDAO dao;

    @BeforeEach
    void setUp() {
        dao = new ProductDAO();
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @AfterEach
    void tearDown() {
        DaoTestSupport.clearAuthentication();
    }

    @Test
    void findProduct_delegatesLookupWithoutNormalization() {
        Product product = product(" P001 ", "ACTIVE");
        when(session.find(Product.class, " P001 ")).thenReturn(product);

        assertSame(product, dao.findProduct(" P001 "));
    }

    @ParameterizedTest
    @MethodSource("inactiveLookups")
    void findActiveProduct_returnsNullForMissingOrNonActiveProduct(Product product) {
        when(session.find(Product.class, "P001")).thenReturn(product);

        assertNull(dao.findActiveProduct("P001"));
    }

    static Stream<Product> inactiveLookups() {
        return Stream.of(null, product("P001", null), product("P001", "DRAFT"), product("P001", "INACTIVE"));
    }

    @Test
    void findActiveProduct_acceptsStatusCaseInsensitively() {
        Product product = product("P001", "active");
        when(session.find(Product.class, "P001")).thenReturn(product);

        assertSame(product, dao.findActiveProduct("P001"));
    }

    @Test
    void findProductForUpdate_usesPessimisticWriteLock() {
        Product product = product("P001", "ACTIVE");
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(product);

        assertSame(product, dao.findProductForUpdate("P001"));
    }

    @Test
    void findProductInfo_returnsNullForUnavailableProduct() {
        when(session.find(Product.class, "P001")).thenReturn(product("P001", "INACTIVE"));

        assertNull(dao.findProductInfo("P001"));
    }

    @Test
    void findProductInfo_mapsActiveProduct() {
        Product product = product("P001", "ACTIVE");
        product.setName("Shoes");
        when(session.find(Product.class, "P001")).thenReturn(product);

        ProductInfo info = dao.findProductInfo("P001");

        assertEquals("P001", info.getCode());
        assertEquals("Shoes", info.getName());
    }

    static Stream<ProductForm> invalidForms() {
        ProductForm nullCode = validForm();
        nullCode.setCode(null);
        ProductForm blankCode = validForm();
        blankCode.setCode(" ");
        ProductForm longCode = validForm();
        longCode.setCode(textOfLength('P', 21));
        ProductForm nullName = validForm();
        nullName.setName(null);
        ProductForm blankName = validForm();
        blankName.setName(" ");
        ProductForm longName = validForm();
        longName.setName(textOfLength('N', 256));
        ProductForm zeroPrice = validForm();
        zeroPrice.setPrice(0);
        ProductForm negativePrice = validForm();
        negativePrice.setPrice(-1);
        ProductForm nanPrice = validForm();
        nanPrice.setPrice(Double.NaN);
        ProductForm infinitePrice = validForm();
        infinitePrice.setPrice(Double.POSITIVE_INFINITY);
        ProductForm lowDiscount = validForm();
        lowDiscount.setDiscountPercent(-1);
        ProductForm highDiscount = validForm();
        highDiscount.setDiscountPercent(101);
        ProductForm negativeStock = validForm();
        negativeStock.setStockQuantity(-1);
        return Stream.of(null, nullCode, blankCode, longCode, nullName, blankName, longName,
                zeroPrice, negativePrice, nanPrice, infinitePrice, lowDiscount, highDiscount, negativeStock);
    }

    @ParameterizedTest
    @MethodSource("invalidForms")
    void save_rejectsEveryInvalidFormBoundary(ProductForm form) {
        assertThrows(IllegalArgumentException.class, () -> dao.save(form));
        verify(session, never()).persist(any());
        verify(session, never()).flush();
    }

    @Test
    void save_rejectsMissingAuthentication() {
        assertThrows(AccessDeniedException.class, () -> dao.save(validForm()));
        verify(session, never()).persist(any());
    }

    @Test
    void save_rejectsUnauthenticatedPrincipal() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(AccessDeniedException.class, () -> dao.save(validForm()));
    }

    @Test
    void save_rejectsAnonymousPrincipal() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        assertThrows(AccessDeniedException.class, () -> dao.save(validForm()));
    }

    @Test
    void save_createsProductWithNormalizedFieldsOwnerAndBoundedMetadata() {
        DaoTestSupport.authenticate("manager1", "ROLE_MANAGER");
        ProductForm form = validForm();
        form.setCode(" P001 ");
        form.setName(" Shoes ");
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        dao.save(form);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(session).persist(captor.capture());
        Product created = captor.getValue();
        assertEquals("P001", created.getCode());
        assertEquals("Shoes", created.getName());
        assertEquals("manager1", created.getOwnerUsername());
        assertEquals("ACTIVE", created.getStatus());
        assertNotNull(created.getCreateDate());
        assertTrue(created.getSalesCount() >= 0 && created.getSalesCount() < 210000);
        assertTrue(created.getRating() >= 3 && created.getRating() <= 5);
        verify(session).flush();
    }

    @Test
    void save_updatesOwnedProductWithoutPersistingAgain() {
        DaoTestSupport.authenticate("manager1", "ROLE_MANAGER");
        Product existing = product("P001", "INACTIVE");
        existing.setOwnerUsername("manager1");
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(existing);
        ProductForm form = validForm();
        form.setPrice(250);
        form.setDiscountPercent(15);
        form.setStockQuantity(8);

        dao.save(form);

        assertEquals(250, existing.getPrice());
        assertEquals(15, existing.getDiscountPercent());
        assertEquals(8, existing.getStockQuantity());
        assertEquals("ACTIVE", existing.getStatus());
        verify(session, never()).persist(any());
        verify(session).flush();
    }

    @Test
    void save_rejectsUpdateByDifferentOwner() {
        DaoTestSupport.authenticate("manager2", "ROLE_MANAGER");
        Product existing = product("P001", "ACTIVE");
        existing.setOwnerUsername("manager1");
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(existing);

        assertThrows(AccessDeniedException.class, () -> dao.save(validForm()));
        verify(session, never()).flush();
    }

    @ParameterizedTest
    @MethodSource("imageBytes")
    void save_setsImageOnlyWhenUploadedBytesAreNonEmpty(byte[] bytes, boolean imageExpected) throws Exception {
        DaoTestSupport.authenticate("manager1", "ROLE_MANAGER");
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(bytes);
        ProductForm form = validForm();
        form.setFileData(file);
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        dao.save(form);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(session).persist(captor.capture());
        if (imageExpected) {
            assertArrayEquals(bytes, captor.getValue().getImage());
        } else {
            assertNull(captor.getValue().getImage());
        }
    }

    static Stream<Arguments> imageBytes() {
        return Stream.of(Arguments.of(null, false), Arguments.of(new byte[0], false),
                Arguments.of(new byte[] { 1, 2 }, true));
    }

    @Test
    void save_wrapsImageReadFailureAsIllegalArgument() throws Exception {
        DaoTestSupport.authenticate("manager1", "ROLE_MANAGER");
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("broken"));
        ProductForm form = validForm();
        form.setFileData(file);
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> dao.save(form));

        assertTrue(error.getCause() instanceof IOException);
        verify(session, never()).persist(any());
    }

    @Test
    void save_samplesBothRandomBooleanMetadataOutcomesWithinSafetyLimit() {
        DaoTestSupport.authenticate("manager1", "ROLE_MANAGER");
        Set<Boolean> mallOutcomes = new HashSet<>();
        Set<Boolean> favoredOutcomes = new HashSet<>();
        doAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            mallOutcomes.add(saved.isMall());
            favoredOutcomes.add(saved.isFavored());
            return null;
        }).when(session).persist(any(Product.class));

        // Production owns ThreadLocalRandom directly, so the sample is bounded to keep
        // branch coverage without introducing a production-only injection seam.
        int attempts = 0;
        while ((mallOutcomes.size() < 2 || favoredOutcomes.size() < 2)
                && attempts < RANDOM_METADATA_SAMPLE_LIMIT) {
            ProductForm form = validForm();
            form.setCode("P" + attempts);
            dao.save(form);
            attempts++;
        }

        assertEquals(2, mallOutcomes.size());
        assertEquals(2, favoredOutcomes.size());
    }

    @Test
    void queryProducts_withoutOwnerRestrictsToActiveAndUsesDefaultSort() {
        Query<ProductInfo> query = emptyProductQuery();

        PaginationResult<ProductInfo> result = dao.queryProducts(1, 10, 5);

        assertEquals(0, result.getTotalRecords());
        verify(query).setParameter("activeStatus", "ACTIVE");
        assertQueryContains("p.status = :activeStatus", "order by p.createDate desc");
    }

    @Test
    void queryProducts_withOwnerIncludesInactiveOwnerInventory() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, "manager1");

        verify(query).setParameter("ownerUsername", "manager1");
        verify(query, never()).setParameter("activeStatus", "ACTIVE");
    }

    @Test
    void queryProducts_likeNameOnlyOverloadDelegatesWithActiveScope() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, "Shoe");

        verify(query).setParameter("likeName", "%shoe%");
        verify(query).setParameter("activeStatus", "ACTIVE");
    }

    @Test
    void queryProducts_withoutCategoryOverloadDelegatesAllFilters() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, "Shoe", "manager1", "priceAsc", 10.0, 100.0,
                "Da Nang", "Originals", true, false, 4);

        verify(query).setParameter("likeName", "%shoe%");
        verify(query).setParameter("ownerUsername", "manager1");
        verify(query).setParameter("minPrice", 10.0);
        verify(query).setParameter("maxPrice", 100.0);
        verify(query).setParameter("loc_0", "%da nang%");
        verify(query).setParameter("brand", "Originals");
        verify(query).setParameter("isMall", true);
        verify(query).setParameter("isFavored", false);
        verify(query).setParameter("rating", 4.0);
    }

    @Test
    void queryProducts_bindsNameCategoryPriceAndBooleanFilters() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, "Shoe", null, null, 10.0, 100.0,
                null, null, true, false, 4, " Sneaker ");

        verify(query).setParameter("likeName", "%shoe%");
        verify(query).setParameter("category", "%sneaker%");
        verify(query).setParameter("minPrice", 10.0);
        verify(query).setParameter("maxPrice", 100.0);
        verify(query).setParameter("isMall", true);
        verify(query).setParameter("isFavored", false);
        verify(query).setParameter("rating", 4.0);
    }

    @Test
    void queryProducts_treatsEmptyOrBlankOptionalTextAsAbsent() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, "", "", null, null, null,
                "   ", "   ", null, null, null, "   ");

        verify(query).setParameter("activeStatus", "ACTIVE");
        verify(query, never()).setParameter("likeName", "%%");
        verify(query, never()).setParameter("ownerUsername", "");
        verify(query, never()).setParameter("category", "%%");
        verify(query, never()).setParameterList(anyString(), any(java.util.Collection.class));
    }

    @Test
    void queryProducts_bindsEachNonEmptyLocationToken() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, null, null, null,
                " Da Nang, ,Nha Trang ", null, null, null, null, null);

        verify(query).setParameter("loc_0", "%da nang%");
        verify(query).setParameter("loc_1", "%nha trang%");
        assertQueryContains("lower(p.location) like :loc_0", "lower(p.location) like :loc_1");
    }

    @Test
    void queryProducts_ignoresLocationContainingOnlySeparators() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, null, null, null,
                ", ,", null, null, null, null, null);

        verify(query, never()).setParameter(org.mockito.ArgumentMatchers.startsWith("loc_"), any());
    }

    @Test
    void queryProducts_normalizesEverySupportedLocationAlias() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, null, null, null,
                "Hồ Chí Minh,HCM,Hà Nội,HN,An Giang,Cà Mau,Đà Nẵng",
                null, null, null, null, null);

        verify(query).setParameter("loc_0", "%hồ chí minh%");
        verify(query).setParameter("loc_1", "%hồ chí minh%");
        verify(query).setParameter("loc_2", "%hà nội%");
        verify(query).setParameter("loc_3", "%hà nội%");
        verify(query).setParameter("loc_4", "%an giang%");
        verify(query).setParameter("loc_5", "%cà mau%");
        verify(query).setParameter("loc_6", "%đà nẵng%");
    }

    @Test
    void queryProducts_bindsSingleBrandAsScalar() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, null, null, null,
                null, "Originals", null, null, null, null);

        verify(query).setParameter("brand", "Originals");
    }

    @Test
    void queryProducts_bindsMultipleBrandsAsList() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, null, null, null,
                null, "Brand A, Brand B", null, null, null, null);

        verify(query).setParameterList("brandList", java.util.Arrays.asList("Brand A", "Brand B"));
    }

    @Test
    void queryProducts_ignoresBrandContainingOnlySeparators() {
        Query<ProductInfo> query = emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, null, null, null,
                null, ", ,", null, null, null, null);

        verify(query, never()).setParameter("brand", "");
        verify(query, never()).setParameterList(anyString(), any(java.util.Collection.class));
    }

    @ParameterizedTest
    @MethodSource("sortClauses")
    void queryProducts_selectsRequestedSort(String sort, String expectedClause) {
        emptyProductQuery();

        dao.queryProducts(1, 10, 5, null, null, sort, null, null,
                null, null, null, null, null, null);

        assertQueryContains(expectedClause);
    }

    static Stream<Arguments> sortClauses() {
        return Stream.of(
                Arguments.of("popular", "order by p.rating desc, p.createDate desc"),
                Arguments.of("sales", "order by p.salesCount desc"),
                Arguments.of("priceAsc", "order by (p.price * (100 - p.discountPercent) / 100.0) asc"),
                Arguments.of("priceDesc", "order by (p.price * (100 - p.discountPercent) / 100.0) desc"),
                Arguments.of("unknown", "order by p.createDate desc"));
    }

    @Test
    void deleteProduct_doesNothingWhenProductMissing() {
        when(session.find(Product.class, "P404", LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);

        dao.deleteProduct("P404");

        verify(session, never()).update(any());
        verify(session, never()).flush();
    }

    @Test
    void deleteProduct_softDeletesUnderWriteLock() {
        Product product = product("P001", "ACTIVE");
        when(session.find(Product.class, "P001", LockModeType.PESSIMISTIC_WRITE)).thenReturn(product);

        dao.deleteProduct("P001");

        assertEquals("INACTIVE", product.getStatus());
        verify(session).update(product);
        verify(session).flush();
    }

    @SuppressWarnings("unchecked")
    private Query<ProductInfo> emptyProductQuery() {
        Query<ProductInfo> query = mock(Query.class);
        ScrollableResults scroll = mock(ScrollableResults.class);
        when(session.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(ProductInfo.class))).thenReturn(query);
        when(query.scroll(ScrollMode.SCROLL_INSENSITIVE)).thenReturn(scroll);
        when(scroll.first()).thenReturn(false);
        when(scroll.getRowNumber()).thenReturn(-1);
        return query;
    }

    private void assertQueryContains(String... fragments) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(session).createQuery(captor.capture(), org.mockito.ArgumentMatchers.eq(ProductInfo.class));
        for (String fragment : fragments) {
            assertTrue(captor.getValue().contains(fragment), captor.getValue());
        }
    }

    private static ProductForm validForm() {
        ProductForm form = new ProductForm();
        form.setCode("P001");
        form.setName("Shoes");
        form.setPrice(100);
        form.setDiscountPercent(10);
        form.setStockQuantity(5);
        return form;
    }

    private static Product product(String code, String status) {
        Product product = new Product();
        product.setCode(code);
        product.setStatus(status);
        return product;
    }

    private static String textOfLength(char value, int count) {
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            text.append(value);
        }
        return text.toString();
    }
}
