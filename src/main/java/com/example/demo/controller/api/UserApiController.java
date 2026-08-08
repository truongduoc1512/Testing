package com.example.demo.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BeanPropertyBindingResult;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.RegisterForm;
import com.example.demo.form.UserProfileForm;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.service.AccountProfileService;
import com.example.demo.service.AuthenticatedAccountService;
import com.example.demo.validator.RegisterFormValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "4. User REST API", description = "RESTful APIs dành cho quản lý người dùng, đăng ký và thông tin tài khoản (JSON output)")
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticatedAccountService authenticatedAccountService;

    @Autowired
    private AccountProfileService accountProfileService;

    @Autowired
    private RegisterFormValidator registerFormValidator;

    @Operation(summary = "Lấy danh sách người dùng có phân trang (Admin)")
    @GetMapping
    public ResponseEntity<PaginationResult<Account>> getUsers(
            @RequestParam(value = "page", defaultValue = "1") int page) {
        int maxResult = 10;
        int maxNavigationPage = 10;
        PaginationResult<Account> result = accountDAO.listAccounts(Math.max(page, 1), maxResult, maxNavigationPage);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy thông tin tài khoản người dùng theo username")
    @GetMapping("/{userName}")
    public ResponseEntity<?> getUserByUsername(@PathVariable("userName") String userName) {
        Account account = accountDAO.findAccount(userName);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy tài khoản với username: " + userName));
        }
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Đăng ký tài khoản mới (JSON Payload)")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterForm registerForm) {
        if (registerForm == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu đăng ký không được để trống."));
        }

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(registerForm, "registerForm");
        registerFormValidator.validate(registerForm, errors);
        if (errors.hasErrors()) {
            HttpStatus status = ApiResponse.containsErrorCodePrefix(errors, "Duplicate.")
                    ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(ApiResponse.error(errors));
        }

        try {
            Account account = accountDAO.createLocalAccount(registerForm,
                    passwordEncoder.encode(registerForm.getPassword()));
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (DataIntegrityViolationException e) {
            LOGGER.warn("Dữ liệu đăng ký REST xung đột: {}", registerForm.getUserName());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Tên tài khoản hoặc email đã được sử dụng."));
        } catch (Exception e) {
            LOGGER.error("Không thể đăng ký tài khoản {} qua REST", registerForm.getUserName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể tạo tài khoản."));
        }
    }

    @Operation(summary = "Lấy hồ sơ cá nhân của tài khoản đang đăng nhập")
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem thông tin cá nhân!"));
        }

        Account account = authenticatedAccountService.resolve(auth);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy thông tin tài khoản trong hệ thống!"));
        }
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Cập nhật hồ sơ cá nhân (FullName, Email, Phone, AvatarUrl)")
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileForm profileForm) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để cập nhật hồ sơ!"));
        }

        Account account = authenticatedAccountService.resolve(auth);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy tài khoản cần cập nhật!"));
        }

        try {
            String validationError = accountProfileService.validate(account, profileForm);
            if (validationError != null) {
                return ResponseEntity.badRequest().body(ApiResponse.error(validationError));
            }

            accountProfileService.apply(account, profileForm);

            accountDAO.saveAccount(account);
            Account updatedAccount = accountDAO.findAccount(account.getUserName());
            return ResponseEntity.ok(updatedAccount);
        } catch (Exception e) {
            LOGGER.error("Không thể cập nhật hồ sơ {} qua REST", account.getUserName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể cập nhật hồ sơ."));
        }
    }

    @Operation(summary = "Đổi mật khẩu tài khoản")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody java.util.Map<String, String> payload) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để đổi mật khẩu!"));
        }

        String oldPassword = payload != null ? payload.get("oldPassword") : null;
        String newPassword = payload != null ? payload.get("newPassword") : null;
        String confirmPassword = payload != null ? payload.get("confirmPassword") : null;

        if (oldPassword == null || newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu mới phải từ 8 ký tự trở lên!"));
        }

        if (confirmPassword != null && !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu xác nhận không khớp!"));
        }

        Account account = authenticatedAccountService.resolve(auth);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy tài khoản!"));
        }

        if (!passwordEncoder.matches(oldPassword, account.getEncrytedPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu cũ không chính xác!"));
        }

        account.setEncrytedPassword(passwordEncoder.encode(newPassword));
        accountDAO.saveAccount(account);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công!"));
    }
}
