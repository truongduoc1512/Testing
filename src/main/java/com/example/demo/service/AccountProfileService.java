package com.example.demo.service;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;
import com.example.demo.form.UserProfileForm;

@Service
public class AccountProfileService {

    @Autowired
    private AccountDAO accountDAO;

    public String validate(Account account, UserProfileForm form) {
        if (account == null || form == null) {
            return "Thông tin hồ sơ không hợp lệ!";
        }

        String fullName = trimmed(form.getFullName());
        String email = trimmed(form.getEmail()).toLowerCase();
        String phone = trimmed(form.getPhoneNumber());
        String avatar = trimmed(form.getAvatarUrl());
        if (fullName.length() > 100 || phone.length() > 20 || avatar.length() > 255) {
            return "Thông tin hồ sơ vượt quá độ dài cho phép!";
        }
        if (!email.isEmpty() && (email.length() > 100 || !EmailValidator.getInstance().isValid(email))) {
            return "Email không hợp lệ!";
        }

        Account emailOwner = email.isEmpty() ? null : accountDAO.findAccountByEmail(email);
        if (emailOwner != null && !emailOwner.getUserName().equals(account.getUserName())) {
            return "Email đã được sử dụng bởi tài khoản khác!";
        }
        return null;
    }

    public void apply(Account account, UserProfileForm form) {
        account.setFullName(emptyToNull(form.getFullName()));
        account.setEmail(emptyToNull(form.getEmail()) == null ? null : trimmed(form.getEmail()).toLowerCase());
        account.setPhoneNumber(emptyToNull(form.getPhoneNumber()));
        if (!trimmed(form.getAvatarUrl()).isEmpty()) {
            account.setAvatarUrl(trimmed(form.getAvatarUrl()));
        }
    }

    public String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return Account.ROLE_ADMIN.equals(normalized) || Account.ROLE_USER.equals(normalized) ? normalized : null;
    }

    private String emptyToNull(String value) {
        String normalized = trimmed(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }
}
