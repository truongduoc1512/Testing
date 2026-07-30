package com.example.demo.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.RegisterForm;
import com.example.demo.validator.RegisterFormValidator;

@Controller
@Transactional
public class AuthController {

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private RegisterFormValidator registerFormValidator;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @InitBinder
    public void myInitBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        if (target.getClass() == RegisterForm.class) {
            dataBinder.setValidator(registerFormValidator);
        }
    }

    // GET: Display Registration Form
    @RequestMapping(value = { "/register" }, method = RequestMethod.GET)
    public String registerPage(Model model) {
        RegisterForm form = new RegisterForm();
        model.addAttribute("registerForm", form);
        return "register";
    }

    // POST: Process Registration
    @RequestMapping(value = { "/register" }, method = RequestMethod.POST)
    public String registerSave(Model model,
            @ModelAttribute("registerForm") @Validated RegisterForm registerForm,
            BindingResult result,
            final RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            Account account = new Account();
            account.setUserName(registerForm.getUserName().trim());
            account.setEmail(registerForm.getEmail().trim().toLowerCase());
            account.setEncrytedPassword(passwordEncoder.encode(registerForm.getPassword()));
            account.setActive(true);
            account.setUserRole(Account.ROLE_USER);
            account.setProvider("LOCAL");

            accountDAO.saveAccount(account);
            redirectAttributes.addFlashAttribute("message", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/admin/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi tạo tài khoản: " + e.getMessage());
            return "register";
        }
    }

    // GET: Display Forgot Password Form
    @RequestMapping(value = { "/forgotPassword" }, method = RequestMethod.GET)
    public String forgotPasswordPage() {
        return "forgotPassword";
    }

    // POST: Process Forgot Password Request
    @RequestMapping(value = { "/forgotPassword" }, method = RequestMethod.POST)
    public String processForgotPassword(Model model,
            @RequestParam("email") String email,
            final RedirectAttributes redirectAttributes) {

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập địa chỉ email của bạn.");
            return "forgotPassword";
        }

        Account account = accountDAO.findAccountByEmail(email.trim().toLowerCase());
        if (account == null) {
            model.addAttribute("errorMessage", "Không tìm thấy tài khoản nào kết nối với email này.");
            return "forgotPassword";
        }

        // Generate token and save to DB
        String token = UUID.randomUUID().toString();
        account.setResetToken(token);
        accountDAO.saveAccount(account);

        // Redirect to reset password page directly with token pre-filled for smooth UX demo
        model.addAttribute("successMessage", "Mã đặt lại mật khẩu đã được tạo thành công! (Mã Token Demo: " + token + ")");
        model.addAttribute("resetToken", token);
        return "forgotPassword";
    }

    // GET: Display Reset Password Form
    @RequestMapping(value = { "/resetPassword" }, method = RequestMethod.GET)
    public String resetPasswordPage(Model model, @RequestParam(value = "token", required = false) String token) {
        if (token != null && !token.trim().isEmpty()) {
            Account account = accountDAO.findAccountByResetToken(token.trim());
            if (account == null) {
                model.addAttribute("errorMessage", "Mã xác thực không hợp lệ hoặc đã hết hạn.");
                return "forgotPassword";
            }
            model.addAttribute("token", token.trim());
        }
        return "resetPassword";
    }

    // POST: Save New Password
    @RequestMapping(value = { "/resetPassword" }, method = RequestMethod.POST)
    public String processResetPassword(Model model,
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            final RedirectAttributes redirectAttributes) {

        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Mã xác thực không hợp lệ.");
            return "resetPassword";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập mật khẩu mới.");
            model.addAttribute("token", token);
            return "resetPassword";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("token", token);
            return "resetPassword";
        }

        Account account = accountDAO.findAccountByResetToken(token.trim());
        if (account == null) {
            model.addAttribute("errorMessage", "Mã xác thực không tồn tại hoặc đã bị hủy.");
            return "forgotPassword";
        }

        // Update new encrypted password and clear token
        account.setEncrytedPassword(passwordEncoder.encode(password));
        account.setResetToken(null);
        accountDAO.saveAccount(account);

        redirectAttributes.addFlashAttribute("message", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
        return "redirect:/admin/login";
    }
}
