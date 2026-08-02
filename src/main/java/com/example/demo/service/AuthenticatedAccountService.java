package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;

@Service
public class AuthenticatedAccountService {

    @Autowired
    private AccountDAO accountDAO;

    public Account resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        Account account = accountDAO.findAccount(authentication.getName());
        if (account != null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return account;
        }

        String email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        return email == null ? null : accountDAO.findAccountByEmail(email.trim().toLowerCase());
    }
}
