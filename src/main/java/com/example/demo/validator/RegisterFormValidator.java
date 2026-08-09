package com.example.demo.validator;

import java.util.Locale;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.RegisterForm;

@Component
public class RegisterFormValidator implements Validator {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 128;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72;

    private final AccountDAO accountDAO;

    @Autowired
    public RegisterFormValidator(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == RegisterForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegisterForm form = (RegisterForm) target;

        normalize(form);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "NotEmpty.registerForm.userName", "Tên tài khoản không được để trống");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.registerForm.email", "Email không được để trống");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.registerForm.password", "Mật khẩu không được để trống");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotEmpty.registerForm.confirmPassword", "Xác nhận mật khẩu không được để trống");

        if (errors.hasErrors()) {
            return;
        }

        if (form.getEmail().length() > MAX_EMAIL_LENGTH) {
            errors.rejectValue("email", "Length.registerForm.email", "Email tối đa 128 ký tự");
        } else if (!EmailValidator.getInstance().isValid(form.getEmail())) {
            errors.rejectValue("email", "Pattern.registerForm.email", "Email không hợp lệ");
        }

        // Check password matching
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "Match.registerForm.confirmPassword", "Mật khẩu xác nhận không khớp");
        }
        if (form.getUserName().length() > MAX_USERNAME_LENGTH) {
            errors.rejectValue("userName", "Length.registerForm.userName", "Tên tài khoản tối đa 50 ký tự");
        }
        if (form.getPassword().length() < MIN_PASSWORD_LENGTH
                || form.getPassword().length() > MAX_PASSWORD_LENGTH) {
            errors.rejectValue("password", "Length.registerForm.password", "Mật khẩu phải từ 8 đến 72 ký tự");
        }

        if (errors.hasErrors()) {
            return;
        }

        // Check duplicate username
        Account existingAccount = accountDAO.findAccount(form.getUserName());
        if (existingAccount != null) {
            errors.rejectValue("userName", "Duplicate.registerForm.userName", "Tên tài khoản này đã được sử dụng");
        }

        // Check duplicate email
        Account existingEmailAccount = accountDAO.findAccountByEmail(form.getEmail());
        if (existingEmailAccount != null) {
            errors.rejectValue("email", "Duplicate.registerForm.email", "Email này đã được đăng ký tài khoản khác");
        }
    }

    private void normalize(RegisterForm form) {
        form.setUserName(trim(form.getUserName()));
        form.setEmail(normalizeEmail(form.getEmail()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
