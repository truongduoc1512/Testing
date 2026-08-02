package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AccountDAO accountDAO;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_addsRolePrefixForLegacyRoleValue() {
        Account account = account("alice", "USER");
        when(accountDAO.findAccount("alice")).thenReturn(account);

        UserDetails result = userDetailsService.loadUserByUsername("alice");

        assertThat(authorities(result)).containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_preservesAlreadyPrefixedRoleValue() {
        Account account = account("admin", "ROLE_ADMIN");
        when(accountDAO.findAccount("admin")).thenReturn(account);

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(authorities(result)).containsExactly("ROLE_ADMIN");
    }

    private Account account(String username, String role) {
        Account account = new Account();
        account.setUserName(username);
        account.setEncrytedPassword("encoded-password");
        account.setUserRole(role);
        account.setActive(true);
        return account;
    }

    private java.util.List<String> authorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
    }
}
