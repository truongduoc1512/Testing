package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import com.example.demo.dao.ProductDAO;
import com.example.demo.dao.ProductReviewDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.service.ProductImageAnalysisService;
import com.example.demo.validator.ProductFormValidator;

class ProductControllerCoverageTest {

    private ProductDAO productDAO;
    private ProductReviewDAO reviewDAO;
    private ProductFormValidator validator;
    private ProductController controller;
    private ProductImageAnalysisService productImageAnalysisService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        productDAO = mock(ProductDAO.class);
        reviewDAO = mock(ProductReviewDAO.class);
        validator = mock(ProductFormValidator.class);
        when(validator.supports(ProductForm.class)).thenReturn(true);
        controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productDAO", productDAO);
        ReflectionTestUtils.setField(controller, "productReviewDAO", reviewDAO);
        ReflectionTestUtils.setField(controller, "productFormValidator", validator);
        productImageAnalysisService = new ProductImageAnalysisService();
        ReflectionTestUtils.setField(productImageAnalysisService, "aiServiceUrl", "http://ai.test");
        ReflectionTestUtils.setField(controller, "productImageAnalysisService", productImageAnalysisService);
        restTemplate = (RestTemplate) ReflectionTestUtils.getField(productImageAnalysisService, "restTemplate");
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initBinder_setsValidatorOnlyForProductFormTargets() {
        WebDataBinder nullBinder = new WebDataBinder(null);
        controller.myInitBinder(nullBinder);
        assertTrue(nullBinder.getValidators().isEmpty());

        WebDataBinder otherBinder = new WebDataBinder("other");
        controller.myInitBinder(otherBinder);
        assertTrue(otherBinder.getValidators().isEmpty());

        WebDataBinder productBinder = new WebDataBinder(new ProductForm());
        controller.myInitBinder(productBinder);
        assertSame(validator, productBinder.getValidator());
    }

    @Test
    void listProduct_propagatesFiltersAndNormalizesGuestPage() {
        @SuppressWarnings("unchecked")
        PaginationResult<ProductInfo> publicPage = mock(PaginationResult.class);
        when(productDAO.queryProducts(1, 12, 10, "shoe", null, "price", 10.0, 20.0,
                "Hanoi", "Demo", true, false, 4, "Sneaker")).thenReturn(publicPage);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("productList", controller.listProductHandler(new MockHttpServletRequest(), model,
                "shoe", 0, "price", 10.0, 20.0, "Hanoi", "Demo", true, false, 4, "Sneaker"));
        assertSame(publicPage, model.get("paginationProducts"));
        assertEquals("shoe", model.get("likeName"));
        assertEquals("price", model.get("sort"));
        assertEquals(10.0, model.get("minPrice"));
        assertEquals(20.0, model.get("maxPrice"));
        assertEquals("Hanoi", model.get("location"));
        assertEquals("Demo", model.get("brand"));
        assertTrue((Boolean) model.get("isMall"));
        assertFalse((Boolean) model.get("isFavored"));
        assertEquals(4, model.get("rating"));
        assertEquals("Sneaker", model.get("category"));
    }

    @Test
    void listProduct_doesNotScopeUnauthenticatedTokenToOwner() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", "n/a"));
        String view = controller.listProductHandler(new MockHttpServletRequest(), new ExtendedModelMap(),
                "", 1, "newest", null, null, null, null, null, null, null, null);

        assertEquals("productList", view);
        verify(productDAO).queryProducts(1, 12, 10, "", null, "newest", null, null,
                null, null, null, null, null, null);
    }

    @Test
    void listProduct_doesNotScopeAnonymousAuthenticationToOwner() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        String view = controller.listProductHandler(new MockHttpServletRequest(), new ExtendedModelMap(),
                "anonymous", 1, "newest", null, null, null, null, null, null, null, null);

        assertEquals("productList", view);
        verify(productDAO).queryProducts(1, 12, 10, "anonymous", null, "newest", null, null,
                null, null, null, null, null, null);
    }

    @Test
    void listProduct_doesNotScopeRegularUserToOwner() {
        authenticate("buyer", "ROLE_USER");
        String view = controller.listProductHandler(new MockHttpServletRequest(), new ExtendedModelMap(),
                "user", 2, "newest", null, null, null, null, null, null, null, null);

        assertEquals("productList", view);
        verify(productDAO).queryProducts(2, 12, 10, "user", null, "newest", null, null,
                null, null, null, null, null, null);
    }

    @Test
    void listProduct_scopesAdminToAuthenticatedUsername() {
        authenticate("admin", "ROLE_ADMIN");
        String view = controller.listProductHandler(new MockHttpServletRequest(), new ExtendedModelMap(),
                "admin", 3, "newest", null, null, null, null, null, null, null, null);

        assertEquals("productList", view);
        verify(productDAO).queryProducts(3, 12, 10, "admin", "admin", "newest", null, null,
                null, null, null, null, null, null);
    }

    @Test
    void productDetail_redirectsMissingProduct() {
        assertEquals("redirect:/productList",
                controller.productDetail(new ExtendedModelMap(), "missing"));
    }

    @Test
    void productDetail_populatesExistingProductAndReviews() {
        ProductInfo info = new ProductInfo();
        when(productDAO.findProductInfo("P1")).thenReturn(info);
        when(reviewDAO.getReviewsByProductCode("P1")).thenReturn(Collections.emptyList());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("productDetail", controller.productDetail(model, "P1"));

        assertSame(info, model.get("productInfo"));
        assertEquals(Collections.emptyList(), model.get("reviewsList"));
        assertEquals("P1", ((com.example.demo.form.ProductReviewForm) model.get("productReviewForm"))
                .getProductCode());
    }

    @Test
    void productImage_writesNoBytesForNullCode() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.productImage(new MockHttpServletRequest(), response, new ExtendedModelMap(), null);

        assertArrayEquals(new byte[0], response.getContentAsByteArray());
    }

    @Test
    void productImage_writesNoBytesForMissingProduct() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.productImage(new MockHttpServletRequest(), response, new ExtendedModelMap(), "missing");

        assertArrayEquals(new byte[0], response.getContentAsByteArray());
    }

    @Test
    void productImage_writesNoBytesForProductWithoutImage() throws IOException {
        Product productWithoutImage = productOwnedBy("P0", "owner");
        when(productDAO.findProduct("P0")).thenReturn(productWithoutImage);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.productImage(new MockHttpServletRequest(), response, new ExtendedModelMap(), "P0");

        assertArrayEquals(new byte[0], response.getContentAsByteArray());
    }

    @Test
    void productImage_writesPngBytesWithDetectedContentType() throws IOException {
        Product png = productOwnedBy("P1", "owner");
        byte[] pngBytes = new byte[] { (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a };
        png.setImage(pngBytes);
        when(productDAO.findProduct("P1")).thenReturn(png);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.productImage(new MockHttpServletRequest(), response, new ExtendedModelMap(), "P1");

        assertArrayEquals(pngBytes, response.getContentAsByteArray());
        assertEquals("image/png", response.getContentType());
    }

    @Test
    void productImage_usesOctetStreamForUnknownImageType() throws IOException {
        Product unknown = productOwnedBy("P2", "owner");
        unknown.setImage(new byte[] { 1, 2, 3, 4 });
        when(productDAO.findProduct("P2")).thenReturn(unknown);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.productImage(new MockHttpServletRequest(), response, new ExtendedModelMap(), "P2");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, response.getContentType());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void productEdit_buildsNewFormForMissingCode(String code) {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("product", controller.product(model, code, new RedirectAttributesModelMap()));

        assertTrue(((ProductForm) model.get("productForm")).isNewProduct());
    }

    @Test
    void productEdit_buildsNewFormWhenProductLookupIsMissing() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("product", controller.product(model, "missing", new RedirectAttributesModelMap()));

        assertTrue(((ProductForm) model.get("productForm")).isNewProduct());
    }

    @Test
    void productEdit_rejectsProductOwnedByAnotherUser() {
        Product product = productOwnedBy("P1", "seller");
        when(productDAO.findProduct("P1")).thenReturn(product);
        authenticate("other", "ROLE_ADMIN");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList", controller.product(new ExtendedModelMap(), "P1", redirect));

        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void productEdit_populatesFormForOwner() {
        Product product = productOwnedBy("P1", "seller");
        when(productDAO.findProduct("P1")).thenReturn(product);
        authenticate("seller", "ROLE_ADMIN");
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("product", controller.product(model, "P1", new RedirectAttributesModelMap()));

        assertFalse(((ProductForm) model.get("productForm")).isNewProduct());
    }

    @Test
    void productSave_returnsFormForBindingErrors() {
        ProductForm invalid = validProductForm(" P1 ", " Name ");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(invalid, "productForm");
        errors.rejectValue("name", "invalid");

        assertEquals("product", controller.productSave(new ExtendedModelMap(), invalid, errors,
                new RedirectAttributesModelMap()));

        verify(productDAO, never()).save(any(ProductForm.class));
    }

    @Test
    void productSave_rejectsForeignOwner() {
        Product foreign = productOwnedBy("P1", "seller");
        when(productDAO.findProduct("P1")).thenReturn(foreign);
        authenticate("other", "ROLE_ADMIN");
        ProductForm valid = validProductForm(" P1 ", " Name ");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/productList", controller.productSave(new ExtendedModelMap(), valid,
                bindingResult(valid), redirect));

        assertEquals("P1", valid.getCode());
        assertEquals("Name", valid.getName());
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void productSave_savesFormWithoutImage() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm(" P1 ", " Name ");
        when(productDAO.findProduct("P1")).thenReturn(productOwnedBy("P1", "seller"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.productSave(new ExtendedModelMap(), form, bindingResult(form), redirect);

        assertEquals("redirect:/productList", view);
        verify(productDAO).save(form);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void productSave_treatsEmptyUploadAsNoImage() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P2", "Empty file");
        form.setFileData(new MockMultipartFile("file", new byte[0]));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.productSave(new ExtendedModelMap(), form, bindingResult(form), redirect);

        assertEquals("redirect:/productList", view);
        verify(productDAO).save(form);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void productSave_returnsFormWhenDaoRejectsInvalidData() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P3", "Illegal");
        doThrow(new IllegalArgumentException("invalid data")).when(productDAO).save(form);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.productSave(model, form, bindingResult(form), new RedirectAttributesModelMap());

        assertEquals("product", view);
        assertEquals("invalid data", model.get("errorMessage"));
    }

    @Test
    void productSave_redirectsForbiddenWhenDaoDeniesAccess() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P4", "Denied");
        doThrow(new AccessDeniedException("denied")).when(productDAO).save(form);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.productSave(new ExtendedModelMap(), form, bindingResult(form), redirect);

        assertEquals("redirect:/403", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void productSave_returnsFormWhenDaoFailsUnexpectedly() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P5", "Failed");
        doThrow(new RuntimeException("database error")).when(productDAO).save(form);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.productSave(model, form, bindingResult(form), new RedirectAttributesModelMap());

        assertEquals("product", view);
        assertTrue(model.containsAttribute("errorMessage"));
    }

    @Test
    void productSave_rejectsImageWhenAiDoesNotApprove() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P1", "AI rejected");
        form.setFileData(new MockMultipartFile("file", "shoe.png", "image/png", new byte[] { 1, 2 }));
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(once(), requestTo("http://ai.test/api/v1/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"approved\":false,\"reason\":\"blurred\",\"metrics\":{\"score\":0.1}}",
                        MediaType.APPLICATION_JSON));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("product", controller.productSave(model, form, bindingResult(form),
                new RedirectAttributesModelMap()));
        assertEquals("blurred", model.get("aiError"));
        assertTrue(model.containsAttribute("aiMetrics"));
        verify(productDAO, never()).save(form);
        server.verify();
    }

    @Test
    void productSave_acceptsApprovedImageAndUsesFallbackFilename() throws Exception {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P1", "AI approved");
        MultipartFile fallbackNameFile = mock(MultipartFile.class);
        when(fallbackNameFile.isEmpty()).thenReturn(false);
        when(fallbackNameFile.getBytes()).thenReturn(new byte[] { 1, 2 });
        when(fallbackNameFile.getOriginalFilename()).thenReturn(null);
        form.setFileData(fallbackNameFile);
        assertFalse(form.getFileData().isEmpty());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(once(), requestTo("http://ai.test/api/v1/analyze"))
                .andRespond(withSuccess("{\"approved\":true}", MediaType.APPLICATION_JSON));

        assertEquals("redirect:/productList", controller.productSave(new ExtendedModelMap(), form,
                bindingResult(form), new RedirectAttributesModelMap()));
        verify(productDAO).save(form);
        server.verify();
    }

    @Test
    void productSave_allowsMissingAiResponseBody() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P1", "No body");
        form.setFileData(new MockMultipartFile("file", "shoe.jpg", "image/jpeg", new byte[] { 1 }));
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(once(), requestTo("http://ai.test/api/v1/analyze"))
                .andRespond(withSuccess());

        String view = controller.productSave(new ExtendedModelMap(), form, bindingResult(form),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/productList", view);
        verify(productDAO).save(form);
        server.verify();
    }

    @Test
    void productSave_allowsUndecidedAiResponse() {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P2", "Undecided");
        form.setFileData(new MockMultipartFile("file", "shoe.jpg", "image/jpeg", new byte[] { 2 }));
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(once(), requestTo("http://ai.test/api/v1/analyze"))
                .andRespond(withSuccess("{\"approved\":null,\"reason\":\"unknown\"}", MediaType.APPLICATION_JSON));

        String view = controller.productSave(new ExtendedModelMap(), form, bindingResult(form),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/productList", view);
        verify(productDAO).save(form);
        server.verify();
    }

    @Test
    void productSave_warnsAndContinuesWhenAiRequestFails() throws Exception {
        authenticate("seller", "ROLE_ADMIN");
        ProductForm form = validProductForm("P3", "Unavailable");
        MultipartFile failingFile = mock(MultipartFile.class);
        when(failingFile.isEmpty()).thenReturn(false);
        when(failingFile.getBytes()).thenThrow(new IOException("read failed"));
        form.setFileData(failingFile);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.productSave(model, form, bindingResult(form), new RedirectAttributesModelMap());

        assertEquals("redirect:/productList", view);
        assertTrue(model.containsAttribute("aiWarning"));
        verify(productDAO).save(form);
    }

    @Test
    void deleteProduct_ignoresNullCode() {
        String view = controller.deleteProduct(new ExtendedModelMap(), null,
                new RedirectAttributesModelMap());

        assertEquals("redirect:/productList", view);
        verify(productDAO, never()).deleteProduct(any());
    }

    @Test
    void deleteProduct_ignoresEmptyCode() {
        String view = controller.deleteProduct(new ExtendedModelMap(), "",
                new RedirectAttributesModelMap());

        assertEquals("redirect:/productList", view);
        verify(productDAO, never()).deleteProduct(any());
    }

    @Test
    void deleteProduct_rejectsForeignOwner() {
        authenticate("other", "ROLE_ADMIN");
        Product foreign = productOwnedBy("P1", "seller");
        when(productDAO.findProduct("P1")).thenReturn(foreign);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteProduct(new ExtendedModelMap(), "P1", redirect);

        assertEquals("redirect:/productList", view);
        verify(productDAO, never()).deleteProduct("P1");
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void deleteProduct_deletesProductOwnedByCurrentAdmin() {
        authenticate("seller", "ROLE_ADMIN");
        when(productDAO.findProduct("P1")).thenReturn(productOwnedBy("P1", "seller"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteProduct(new ExtendedModelMap(), "P1", redirect);

        assertEquals("redirect:/productList", view);
        verify(productDAO).deleteProduct("P1");
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void deleteProduct_deletesCodeWhenProductLookupIsMissing() {
        authenticate("seller", "ROLE_ADMIN");
        when(productDAO.findProduct("missing")).thenReturn(null);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteProduct(new ExtendedModelMap(), "missing", redirect);

        assertEquals("redirect:/productList", view);
        verify(productDAO).deleteProduct("missing");
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void deleteProduct_reportsDaoFailure() {
        authenticate("seller", "ROLE_ADMIN");
        doThrow(new RuntimeException("delete failed")).when(productDAO).deleteProduct("bad");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteProduct(new ExtendedModelMap(), "bad", redirect);

        assertEquals("redirect:/productList", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    private Product productOwnedBy(String code, String owner) {
        Product product = new Product();
        product.setCode(code);
        product.setName(code);
        product.setOwnerUsername(owner);
        return product;
    }

    private ProductForm validProductForm(String code, String name) {
        ProductForm form = new ProductForm();
        form.setCode(code);
        form.setName(name);
        form.setPrice(100.0);
        form.setStockQuantity(10);
        return form;
    }

    private BeanPropertyBindingResult bindingResult(ProductForm form) {
        return new BeanPropertyBindingResult(form, "productForm");
    }

    private void authenticate(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "n/a", Collections.singletonList(new SimpleGrantedAuthority(role))));
    }
}
