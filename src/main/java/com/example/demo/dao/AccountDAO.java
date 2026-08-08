package com.example.demo.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Locale;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Account;
import com.example.demo.form.RegisterForm;
import com.example.demo.pagination.PaginationResult;

@Transactional
@Repository
public class AccountDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public Account findAccount(String userName) {
        String normalizedUserName = normalizeUserName(userName);
        if (normalizedUserName == null || normalizedUserName.isEmpty()) {
            return null;
        }
        Session session = this.sessionFactory.getCurrentSession();
        return session.find(Account.class, normalizedUserName);
    }

    public Account findAccountByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return null;
        }
        Session session = this.sessionFactory.getCurrentSession();
        String sql = "Select e from " + Account.class.getName() + " e Where e.email = :email";
        Query<Account> query = session.createQuery(sql, Account.class);
        query.setParameter("email", normalizedEmail);
        return query.uniqueResult();
    }

    public Account createLocalAccount(RegisterForm form, String encodedPassword) {
        if (form == null || encodedPassword == null || encodedPassword.isEmpty()) {
            throw new IllegalArgumentException("Thông tin đăng ký không hợp lệ.");
        }
        Account account = new Account();
        account.setUserName(normalizeUserName(form.getUserName()));
        account.setEmail(normalizeEmail(form.getEmail()));
        account.setEncrytedPassword(encodedPassword);
        account.setActive(true);
        account.setUserRole(Account.ROLE_USER);
        account.setProvider("LOCAL");
        saveAccount(account);
        return account;
    }

    public Account findAccountByResetToken(String resetToken) {
        if (resetToken == null || resetToken.trim().isEmpty()) {
            return null;
        }
        Session session = this.sessionFactory.getCurrentSession();
        String sql = "Select e from " + Account.class.getName()
                + " e Where e.resetToken = :token and e.resetTokenExpiresAt >= :now";
        Query<Account> query = session.createQuery(sql, Account.class);
        query.setParameter("token", hashResetToken(resetToken.trim()));
        query.setParameter("now", new Date());
        return query.uniqueResult();
    }

    public void savePasswordResetToken(Account account, String rawToken, Date expiresAt) {
        if (account == null || rawToken == null || rawToken.trim().isEmpty() || expiresAt == null) {
            throw new IllegalArgumentException("Thông tin mã đặt lại mật khẩu không hợp lệ.");
        }
        account.setResetToken(hashResetToken(rawToken.trim()));
        account.setResetTokenExpiresAt(expiresAt);
        saveAccount(account);
    }

    /** Atomically consumes a valid token so concurrent requests cannot reuse it. */
    public boolean resetPassword(String rawToken, String encodedPassword) {
        if (rawToken == null || rawToken.trim().isEmpty()
                || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        Date now = new Date();
        String hql = "Update " + Account.class.getName()
                + " e Set e.encrytedPassword = :password, e.resetToken = null, "
                + "e.resetTokenExpiresAt = null, e.updatedAt = :now "
                + "Where e.resetToken = :token and e.resetTokenExpiresAt >= :now";
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("password", encodedPassword);
        query.setParameter("token", hashResetToken(rawToken.trim()));
        query.setParameter("now", now);
        return query.executeUpdate() == 1;
    }

    private String hashResetToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte currentByte : bytes) {
            int value = currentByte & 0xff;
            result.append(Character.forDigit(value >>> 4, 16));
            result.append(Character.forDigit(value & 0x0f, 16));
        }
        return result.toString();
    }

    public void saveAccount(Account account) {
        Session session = this.sessionFactory.getCurrentSession();
        account.setUpdatedAt(new Date());
        session.saveOrUpdate(account);
    }

    public PaginationResult<Account> listAccounts(int page, int maxResult, int maxNavigationPage) {
        Session session = this.sessionFactory.getCurrentSession();
        String sql = "Select a from " + Account.class.getName() + " a Order by a.createdAt desc";
        Query<Account> query = session.createQuery(sql, Account.class);
        return new PaginationResult<Account>(query, page, maxResult, maxNavigationPage);
    }

    public long countActiveAdmins() {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "Select count(a.userName) from " + Account.class.getName()
                + " a Where a.active = true and a.accountNonLocked = true"
                + " and upper(a.userRole) in ('ADMIN', 'ROLE_ADMIN', 'MANAGER', 'ROLE_MANAGER')";
        return session.createQuery(hql, Long.class).getSingleResult();
    }

    private String normalizeUserName(String userName) {
        return userName == null ? null : userName.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
