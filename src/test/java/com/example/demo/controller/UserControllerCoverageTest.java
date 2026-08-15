package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.demo.dao.AccountDAO;
import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.WishlistDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.RegisterForm;
import com.example.demo.form.UserProfileForm;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.service.AccountProfileService;
import com.example.demo.service.AuthenticatedAccountService;
import com.example.demo.validator.RegisterFormValidator;

class UserControllerCoverageTest {

    private AccountDAO accountDAO;
    private OrderDAO orderDAO;
    private WishlistDAO wishlistDAO;
    private RegisterFormValidator validator;
    private BCryptPasswordEncoder encoder;
    private AuthenticatedAccountService authenticatedAccountService;
    private AccountProfileService accountProfileService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        accountDAO = mock(AccountDAO.class);
        orderDAO = mock(OrderDAO.class);
        wishlistDAO = mock(WishlistDAO.class);
        validator = mock(RegisterFormValidator.class);
        encoder = mock(BCryptPasswordEncoder.class);
        authenticatedAccountService = mock(AuthenticatedAccountService.class);
        accountProfileService = mock(AccountProfileService.class);
        when(validator.supports(RegisterForm.class)).thenReturn(true);

        controller = new UserController();
        ReflectionTestUtils.setField(controller, "accountDAO", accountDAO);
        ReflectionTestUtils.setField(controller, "orderDAO", orderDAO);
        ReflectionTestUtils.setField(controller, "wishlistDAO", wishlistDAO);
        ReflectionTestUtils.setField(controller, "registerFormValidator", validator);
        ReflectionTestUtils.setField(controller, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(controller, "authenticatedAccountService", authenticatedAccountService);
        ReflectionTestUtils.setField(controller, "accountProfileService", accountProfileService);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initBinder_setsValidatorOnlyForRegisterForm() {
        WebDataBinder nullBinder = new WebDataBinder(null);
        controller.myInitBinder(nullBinder);
        assertTrue(nullBinder.getValidators().isEmpty());

        WebDataBinder otherBinder = new WebDataBinder("other");
        controller.myInitBinder(otherBinder);
        assertTrue(otherBinder.getValidators().isEmpty());

        WebDataBinder registerBinder = new WebDataBinder(new RegisterForm());
        controller.myInitBinder(registerBinder);
        assertSame(validator, registerBinder.getValidator());
    }

    @Test
    void login_returnsLoginView() {
        assertEquals("login", controller.login(new ExtendedModelMap()));
    }

    @Test
    void registerPage_addsEmptyFormAndReturnsRegisterView() {
        ExtendedModelMap registerModel = new ExtendedModelMap();

        assertEquals("register", controller.registerPage(registerModel));

        assertNotNull(registerModel.get("registerForm"));
    }

    @Test
    void forgotPasswordPage_returnsDedicatedView() {
        assertEquals("forgotPassword", controller.forgotPasswordPage());
    }

    @Test
    void registerSave_returnsFormForBindingErrors() {
        RegisterForm invalid = registerForm("user", "user@example.com", "password");
        BeanPropertyBindingResult errors = bindingResult(invalid);
        errors.rejectValue("userName", "invalid");

        assertEquals("register", controller.registerSave(new ExtendedModelMap(), invalid, errors,
                new RedirectAttributesModelMap()));

        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void registerSave_normalizesAndPersistsValidAccount() {
        RegisterForm valid = registerForm("  Buyer  ", "  Buyer@Example.COM  ", "password");
        when(encoder.encode("password")).thenReturn("encoded");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/login", controller.registerSave(new ExtendedModelMap(), valid,
                bindingResult(valid), redirect));

        verify(accountDAO).saveAccount(any(Account.class));
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void registerSave_returnsFormWithErrorWhenAccountSaveFails() {
        RegisterForm form = registerForm("Buyer", "buyer@example.com", "password");
        when(encoder.encode("password")).thenReturn("encoded");
        doThrow(new RuntimeException("duplicate")).when(accountDAO).saveAccount(any(Account.class));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.registerSave(model, form, bindingResult(form),
                new RedirectAttributesModelMap());

        assertEquals("register", view);
        verify(accountDAO).saveAccount(any(Account.class));
        assertTrue(model.containsAttribute("errorMessage"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void forgotPassword_rejectsMissingEmail(String email) {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("forgotPassword", controller.processForgotPassword(model, email,
                new RedirectAttributesModelMap()));

        assertTrue(model.containsAttribute("errorMessage"));
        verify(accountDAO, never()).findAccountByEmail(anyString());
    }

    @Test
    void forgotPassword_rejectsBlankEmail() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("forgotPassword", controller.processForgotPassword(model, "   ",
                new RedirectAttributesModelMap()));

        assertTrue(model.containsAttribute("errorMessage"));
    }

    @Test
    void forgotPassword_reportsUnknownAccount() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("forgotPassword", controller.processForgotPassword(model, " missing@example.com ",
                new RedirectAttributesModelMap()));

        verify(accountDAO).findAccountByEmail("missing@example.com");
        assertTrue(model.containsAttribute("errorMessage"));
    }

    @Test
    void forgotPassword_persistsResetTokenForKnownAccount() {
        Account account = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        when(accountDAO.findAccountByEmail("buyer@example.com")).thenReturn(account);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("forgotPassword", controller.processForgotPassword(model, " Buyer@Example.com ",
                new RedirectAttributesModelMap()));

        verify(accountDAO).savePasswordResetToken(eq(account), anyString(), any(Date.class));
        assertTrue(model.containsAttribute("successMessage"));
        assertNotNull(model.get("resetToken"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void resetPasswordPage_showsFormWithoutToken(String token) {
        assertEquals("resetPassword", controller.resetPasswordPage(new ExtendedModelMap(), token));
    }

    @Test
    void resetPasswordPage_showsFormForBlankToken() {
        assertEquals("resetPassword", controller.resetPasswordPage(new ExtendedModelMap(), "   "));
    }

    @Test
    void resetPasswordPage_redirectsInvalidToken() {
        ExtendedModelMap invalidModel = new ExtendedModelMap();

        assertEquals("forgotPassword", controller.resetPasswordPage(invalidModel, " invalid "));

        verify(accountDAO).findAccountByResetToken("invalid");
        assertTrue(invalidModel.containsAttribute("errorMessage"));
    }

    @Test
    void resetPasswordPage_addsNormalizedValidToken() {
        when(accountDAO.findAccountByResetToken("valid"))
                .thenReturn(accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL"));
        ExtendedModelMap validModel = new ExtendedModelMap();

        assertEquals("resetPassword", controller.resetPasswordPage(validModel, " valid "));

        assertEquals("valid", validModel.get("token"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidResetPasswordRequests")
    void processResetPassword_rejectsInvalidRequest(
            String requestCase, String token, String password, String confirmation) {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.processResetPassword(model, token, password, confirmation,
                new RedirectAttributesModelMap());

        assertEquals("resetPassword", view, requestCase);
        assertTrue(model.containsAttribute("errorMessage"), requestCase);
        verify(accountDAO, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void processResetPassword_reportsInvalidToken() {
        when(encoder.encode("password")).thenReturn("encoded");
        when(accountDAO.resetPassword("invalid", "encoded")).thenReturn(false);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("forgotPassword", controller.processResetPassword(model, " invalid ", "password",
                "password", new RedirectAttributesModelMap()));

        assertTrue(model.containsAttribute("errorMessage"));
    }

    @Test
    void processResetPassword_redirectsAfterSuccessfulReset() {
        when(encoder.encode("password")).thenReturn("encoded");
        when(accountDAO.resetPassword("valid", "encoded")).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/login", controller.processResetPassword(new ExtendedModelMap(),
                " valid ", "password", "password", redirect));

        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void accountInfo_usesEmptyIdentityWithoutAuthentication() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("accountInfo", controller.accountInfo(model));

        assertEquals("", model.get("userName"));
        assertEquals("", model.get("userRole"));
    }

    @Test
    void accountInfo_fallsBackToAuthenticationNameForUnresolvedAccount() {
        authenticate("fallback", "ROLE_OTHER");
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("accountInfo", controller.accountInfo(model));

        assertEquals("fallback", model.get("userName"));
        assertEquals("", model.get("userRole"));
    }

    @Test
    void accountInfo_usesResolvedAccountAndUserStatistics() {
        Authentication auth = authenticate("principal", "ROLE_USER");
        Account resolved = accountWithRoleAndProvider("resolved", "ROLE_USER", "LOCAL");
        when(authenticatedAccountService.resolve(auth)).thenReturn(resolved);
        when(orderDAO.getTotalOrdersCount("resolved", "ROLE_USER")).thenReturn(3L);
        when(orderDAO.getTotalRevenue("resolved", "ROLE_USER")).thenReturn(125.0);
        when(wishlistDAO.getWishlistCount("resolved")).thenReturn(4);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("accountInfo", controller.accountInfo(model));

        assertEquals("resolved", model.get("userName"));
        assertEquals("ROLE_USER", model.get("userRole"));
        assertEquals(3L, model.get("totalOrders"));
        assertEquals(125.0, model.get("totalRevenue"));
        assertEquals(4, model.get("wishlistCount"));
    }

    @Test
    void accountInfo_recognizesAdminRole() {
        authenticate("admin", "ROLE_ADMIN");
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("accountInfo", controller.accountInfo(model));

        assertEquals("admin", model.get("userName"));
        assertEquals("ROLE_ADMIN", model.get("userRole"));
    }

    @Test
    void userProfile_redirectsWithoutAuthentication() {
        assertEquals("redirect:/admin/login", controller.userProfile(new ExtendedModelMap()));
    }

    @Test
    void userProfile_redirectsUnauthenticatedToken() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("buyer", "n/a"));

        assertEquals("redirect:/admin/login", controller.userProfile(new ExtendedModelMap()));
    }

    @Test
    void userProfile_redirectsWhenAccountCannotBeResolved() {
        Authentication auth = authenticate("buyer", "ROLE_USER");

        assertEquals("redirect:/", controller.userProfile(new ExtendedModelMap()));
        verify(authenticatedAccountService).resolve(auth);
    }

    @Test
    void userProfile_usesUsernameWhenFullNameIsMissing() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account account = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        account.setFullName(null);
        account.setEmail("buyer@example.com");
        account.setPhoneNumber("0900");
        account.setAvatarUrl("avatar.png");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("userProfile", controller.userProfile(model));
        UserProfileForm form = (UserProfileForm) model.get("profileForm");
        assertEquals("buyer", form.getFullName());
        assertSame(account, model.get("account"));
    }

    @Test
    void userProfile_mapsExistingFullName() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account account = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        account.setFullName("Buyer Name");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("userProfile", controller.userProfile(model));

        assertEquals("Buyer Name", ((UserProfileForm) model.get("profileForm")).getFullName());
    }

    @Test
    void profileSave_redirectsWithoutAuthentication() {
        UserProfileForm form = validProfileForm("buyer");

        assertEquals("redirect:/admin/login", controller.userProfileSave(new ExtendedModelMap(), form,
                new RedirectAttributesModelMap()));
    }

    @Test
    void profileSave_redirectsUnauthenticatedToken() {
        UserProfileForm form = validProfileForm("buyer");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("buyer", "n/a"));

        assertEquals("redirect:/admin/login", controller.userProfileSave(new ExtendedModelMap(), form,
                new RedirectAttributesModelMap()));
    }

    @Test
    void profileSave_reportsUnresolvedAccount() {
        UserProfileForm form = validProfileForm("buyer");
        Authentication auth = authenticate("buyer", "ROLE_USER");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/user/profile",
                controller.userProfileSave(new ExtendedModelMap(), form, redirect));

        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(authenticatedAccountService).resolve(auth);
    }

    @Test
    void profileSave_reportsServiceValidationError() {
        UserProfileForm form = validProfileForm("buyer");
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account account = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        when(authenticatedAccountService.resolve(auth)).thenReturn(account);
        when(accountProfileService.validate(account, form)).thenReturn("invalid profile");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/user/profile",
                controller.userProfileSave(new ExtendedModelMap(), form, redirect));

        assertEquals("invalid profile", redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void profileSave_rejectsMissingOldPasswordForLocalAccount() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account local = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        local.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(local);
        UserProfileForm form = validProfileForm("buyer");
        form.setNewPassword("new-password");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(accountProfileService, never()).apply(any(Account.class), any(UserProfileForm.class));
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void profileSave_rejectsWrongOldPasswordForLocalAccount() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account local = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        local.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(local);
        UserProfileForm form = validProfileForm("buyer");
        form.setOldPassword("wrong");
        form.setNewPassword("new-password");
        when(encoder.matches("wrong", "old-hash")).thenReturn(false);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(encoder).matches("wrong", "old-hash");
        verify(accountProfileService, never()).apply(any(Account.class), any(UserProfileForm.class));
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void profileSave_rejectsNewPasswordConfirmationMismatch() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account local = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        local.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(local);
        UserProfileForm form = validProfileForm("buyer");
        form.setOldPassword("old");
        form.setNewPassword("new-password");
        form.setConfirmPassword("different");
        when(encoder.matches("old", "old-hash")).thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        verify(encoder, never()).encode(anyString());
        verify(accountProfileService, never()).apply(any(Account.class), any(UserProfileForm.class));
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void profileSave_updatesLocalPasswordAndProfile() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account local = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        local.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(local);
        UserProfileForm form = validProfileForm("buyer");
        form.setOldPassword("old");
        form.setNewPassword("new-password");
        form.setConfirmPassword("new-password");
        when(encoder.matches("old", "old-hash")).thenReturn(true);
        when(encoder.encode("new-password")).thenReturn("new-hash");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertEquals("new-hash", local.getEncrytedPassword());
        verify(accountProfileService).apply(local, form);
        verify(accountDAO).saveAccount(local);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void profileSave_skipsPasswordChangeForGoogleAccount() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account google = accountWithRoleAndProvider("buyer", "ROLE_USER", "GOOGLE");
        google.setEncrytedPassword("google-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(google);
        UserProfileForm form = validProfileForm("buyer");
        form.setNewPassword("ignored");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertEquals("google-hash", google.getEncrytedPassword());
        verify(encoder, never()).matches(any(), anyString());
        verify(encoder, never()).encode(anyString());
        verify(accountProfileService).apply(google, form);
        verify(accountDAO).saveAccount(google);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void profileSave_treatsBlankNewPasswordAsUnchanged() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account local = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        local.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(local);
        UserProfileForm form = validProfileForm("buyer");
        form.setNewPassword("   ");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertEquals("old-hash", local.getEncrytedPassword());
        verify(encoder, never()).matches(any(), anyString());
        verify(encoder, never()).encode(anyString());
        verify(accountProfileService).apply(local, form);
        verify(accountDAO).saveAccount(local);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void profileSave_preservesPasswordWhenNoNewPasswordWasSubmitted() {
        Authentication auth = authenticate("buyer", "ROLE_USER");
        Account local = accountWithRoleAndProvider("buyer", "ROLE_USER", "LOCAL");
        local.setEncrytedPassword("old-hash");
        when(authenticatedAccountService.resolve(auth)).thenReturn(local);
        UserProfileForm form = validProfileForm("buyer");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userProfileSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/profile", view);
        assertEquals("old-hash", local.getEncrytedPassword());
        verify(encoder, never()).matches(any(), anyString());
        verify(encoder, never()).encode(anyString());
        verify(accountProfileService).apply(local, form);
        verify(accountDAO).saveAccount(local);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void userList_redirectsWithoutAuthentication() {
        assertEquals("redirect:/403", controller.userList(new ExtendedModelMap(), "1"));
    }

    @Test
    void userList_redirectsNonAdmin() {
        authenticate("buyer", "ROLE_USER");

        assertEquals("redirect:/403", controller.userList(new ExtendedModelMap(), "1"));
    }

    @Test
    void userList_normalizesInvalidPageForAdmin() {
        authenticate("admin", "ROLE_ADMIN");
        @SuppressWarnings("unchecked")
        PaginationResult<Account> page = mock(PaginationResult.class);
        when(accountDAO.listAccounts(1, 10, 10)).thenReturn(page);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("userList", controller.userList(model, "invalid"));

        assertSame(page, model.get("paginationResult"));
    }

    @Test
    void userEdit_redirectsWithoutAuthentication() {
        assertEquals("redirect:/403", controller.userEdit(new ExtendedModelMap(), "buyer"));
    }

    @Test
    void userEdit_redirectsNonAdmin() {
        authenticate("buyer", "ROLE_USER");

        assertEquals("redirect:/403", controller.userEdit(new ExtendedModelMap(), "buyer"));
    }

    @Test
    void userEdit_redirectsMissingAccount() {
        authenticate("admin", "ROLE_ADMIN");

        assertEquals("redirect:/admin/users", controller.userEdit(new ExtendedModelMap(), "missing"));
    }

    @Test
    void userEdit_usesUsernameWhenFullNameIsMissing() {
        authenticate("admin", "ROLE_ADMIN");
        Account account = accountWithRoleAndProvider("target", "ROLE_USER", "LOCAL");
        account.setFullName(null);
        when(accountDAO.findAccount("target")).thenReturn(account);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("userEdit", controller.userEdit(model, "target"));

        assertEquals("target", ((UserProfileForm) model.get("profileForm")).getFullName());
    }

    @Test
    void userEdit_mapsExistingFullName() {
        authenticate("admin", "ROLE_ADMIN");
        Account account = accountWithRoleAndProvider("target", "ROLE_USER", "LOCAL");
        account.setFullName("Target User");
        when(accountDAO.findAccount("target")).thenReturn(account);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("userEdit", controller.userEdit(model, "target"));

        assertEquals("Target User", ((UserProfileForm) model.get("profileForm")).getFullName());
    }

    @Test
    void userEditSave_redirectsWithoutAuthentication() {
        UserProfileForm form = validProfileForm("target");

        assertEquals("redirect:/403", controller.userEditSave(new ExtendedModelMap(), form,
                new RedirectAttributesModelMap()));
    }

    @Test
    void userEditSave_redirectsNonAdmin() {
        UserProfileForm form = validProfileForm("target");
        authenticate("buyer", "ROLE_USER");

        assertEquals("redirect:/403", controller.userEditSave(new ExtendedModelMap(), form,
                new RedirectAttributesModelMap()));
    }

    @Test
    void userEditSave_reportsMissingAccount() {
        UserProfileForm form = validProfileForm("target");
        authenticate("admin", "ROLE_ADMIN");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/users", controller.userEditSave(new ExtendedModelMap(), form, redirect));

        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void userEditSave_reportsServiceValidationError() {
        UserProfileForm form = validProfileForm("target");
        authenticate("admin", "ROLE_ADMIN");
        Account account = accountWithRoleAndProvider("target", "ROLE_USER", "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.validate(account, form)).thenReturn("invalid profile");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/user/edit?userName=target",
                controller.userEditSave(new ExtendedModelMap(), form, redirect));

        assertEquals("invalid profile", redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void userEditSave_rejectsInvalidRole() {
        UserProfileForm form = validProfileForm("target");
        authenticate("admin", "ROLE_ADMIN");
        Account account = accountWithRoleAndProvider("target", "ROLE_USER", "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(form.getUserRole())).thenReturn(null);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/admin/user/edit?userName=target",
                controller.userEditSave(new ExtendedModelMap(), form, redirect));

        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    @Test
    void userEditSave_blocksLastActiveAdminFromLosingAdminRole() {
        authenticate("admin", "ROLE_ADMIN");
        UserProfileForm form = validProfileForm("target");
        form.setUserRole(Account.ROLE_USER);
        Account account = accountWithRoleAndProvider("target", Account.ROLE_ADMIN, "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(Account.ROLE_USER)).thenReturn(Account.ROLE_USER);
        when(accountProfileService.normalizeRole(Account.ROLE_ADMIN)).thenReturn(Account.ROLE_ADMIN);
        when(accountDAO.countActiveAdmins()).thenReturn(1L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userEditSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/user/edit?userName=target", view);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
        assertEquals(Account.ROLE_ADMIN, account.getUserRole());
        verify(accountProfileService, never()).apply(any(Account.class), any(UserProfileForm.class));
        verify(accountDAO, never()).saveAccount(any(Account.class));
    }

    @Test
    void userEditSave_allowsAdminDowngradeWhenAnotherActiveAdminExists() {
        authenticate("admin", "ROLE_ADMIN");
        UserProfileForm form = validProfileForm("target");
        form.setUserRole(Account.ROLE_USER);
        Account account = accountWithRoleAndProvider("target", Account.ROLE_ADMIN, "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(Account.ROLE_USER)).thenReturn(Account.ROLE_USER);
        when(accountProfileService.normalizeRole(Account.ROLE_ADMIN)).thenReturn(Account.ROLE_ADMIN);
        when(accountDAO.countActiveAdmins()).thenReturn(2L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userEditSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/users", view);
        assertEquals(Account.ROLE_USER, account.getUserRole());
        verify(accountProfileService).apply(account, form);
        verify(accountDAO).saveAccount(account);
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
    }

    @Test
    void userEditSave_deactivatesAdminWhenAnotherActiveAdminExists() {
        authenticate("admin", "ROLE_ADMIN");
        UserProfileForm form = validProfileForm("target");
        form.setUserRole(Account.ROLE_ADMIN);
        form.setActive(false);
        Account account = accountWithRoleAndProvider("target", Account.ROLE_ADMIN, "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(Account.ROLE_ADMIN)).thenReturn(Account.ROLE_ADMIN);
        when(accountDAO.countActiveAdmins()).thenReturn(2L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userEditSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/users", view);
        assertFalse(account.isActive());
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
        verify(accountDAO).countActiveAdmins();
        verify(accountProfileService).apply(account, form);
        verify(accountDAO).saveAccount(account);
    }

    @Test
    void userEditSave_locksAdminWhenAnotherActiveAdminExists() {
        authenticate("admin", "ROLE_ADMIN");
        UserProfileForm form = validProfileForm("target");
        form.setUserRole(Account.ROLE_ADMIN);
        form.setAccountNonLocked(false);
        Account account = accountWithRoleAndProvider("target", Account.ROLE_ADMIN, "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(Account.ROLE_ADMIN)).thenReturn(Account.ROLE_ADMIN);
        when(accountDAO.countActiveAdmins()).thenReturn(2L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userEditSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/users", view);
        assertFalse(account.isAccountNonLocked());
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
        verify(accountDAO).countActiveAdmins();
        verify(accountProfileService).apply(account, form);
        verify(accountDAO).saveAccount(account);
    }

    @Test
    void userEditSave_updatesNormalUserWithoutCountingAdmins() {
        authenticate("admin", "ROLE_ADMIN");
        UserProfileForm form = validProfileForm("target");
        form.setUserRole(Account.ROLE_USER);
        Account account = accountWithRoleAndProvider("target", Account.ROLE_USER, "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(Account.ROLE_USER)).thenReturn(Account.ROLE_USER);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userEditSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/users", view);
        assertEquals(Account.ROLE_USER, account.getUserRole());
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
        verify(accountDAO, never()).countActiveAdmins();
        verify(accountProfileService).apply(account, form);
        verify(accountDAO).saveAccount(account);
    }

    @Test
    void userEditSave_keepsActiveUnlockedAdminWithoutCountingAdmins() {
        authenticate("admin", "ROLE_ADMIN");
        UserProfileForm form = validProfileForm("target");
        form.setUserRole(Account.ROLE_ADMIN);
        Account account = accountWithRoleAndProvider("target", Account.ROLE_ADMIN, "LOCAL");
        when(accountDAO.findAccount("target")).thenReturn(account);
        when(accountProfileService.normalizeRole(Account.ROLE_ADMIN)).thenReturn(Account.ROLE_ADMIN);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.userEditSave(new ExtendedModelMap(), form, redirect);

        assertEquals("redirect:/admin/users", view);
        assertEquals(Account.ROLE_ADMIN, account.getUserRole());
        assertTrue(account.isActive());
        assertTrue(account.isAccountNonLocked());
        assertTrue(redirect.getFlashAttributes().containsKey("message"));
        verify(accountDAO, never()).countActiveAdmins();
        verify(accountProfileService).apply(account, form);
        verify(accountDAO).saveAccount(account);
    }

    private static Stream<Arguments> invalidResetPasswordRequests() {
        String passwordAboveMaximumLength = textOfLength('p', 73);
        return Stream.of(
                Arguments.of("missing token", null, "password", "password"),
                Arguments.of("blank token", "   ", "password", "password"),
                Arguments.of("missing password", "token", null, null),
                Arguments.of("blank password", "token", "   ", "   "),
                Arguments.of("password below minimum length", "token", "short", "short"),
                Arguments.of("password above maximum length", "token",
                        passwordAboveMaximumLength, passwordAboveMaximumLength),
                Arguments.of("confirmation mismatch", "token", "password", "different"));
    }

    private RegisterForm registerForm(String username, String email, String password) {
        return new RegisterForm(username, email, password, password);
    }

    private BeanPropertyBindingResult bindingResult(RegisterForm form) {
        return new BeanPropertyBindingResult(form, "registerForm");
    }

    private Account accountWithRoleAndProvider(String username, String role, String provider) {
        Account account = new Account();
        account.setUserName(username);
        account.setUserRole(role);
        account.setProvider(provider);
        account.setActive(true);
        account.setAccountNonLocked(true);
        return account;
    }

    private UserProfileForm validProfileForm(String username) {
        UserProfileForm form = new UserProfileForm();
        form.setUserName(username);
        form.setFullName("Demo User");
        form.setEmail("user@example.com");
        form.setPhoneNumber("0900");
        form.setUserRole("USER");
        form.setActive(true);
        form.setAccountNonLocked(true);
        return form;
    }

    private Authentication authenticate(String username, String role) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, "n/a",
                Collections.singletonList(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private static String textOfLength(char value, int count) {
        return String.join("", Collections.nCopies(count, String.valueOf(value)));
    }
}
