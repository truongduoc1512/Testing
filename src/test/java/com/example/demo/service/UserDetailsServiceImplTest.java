package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.dao.AccountDAO;
import com.example.demo.entity.Account;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AccountDAO accountDAO;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_throwsForUnknownAccount() {
        when(accountDAO.findAccount("missing")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing"));

        assertThat(exception.getMessage()).contains("missing");
    }

    @Test
    void loadUserByUsername_addsRolePrefixForLegacyRoleValue() {
        Account account = activeAccountWithRole("alice", "USER");
        when(accountDAO.findAccount("alice")).thenReturn(account);

        UserDetails result = userDetailsService.loadUserByUsername("alice");

        assertThat(authorityNames(result)).containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_preservesAlreadyPrefixedRoleValue() {
        Account account = activeAccountWithRole("admin", "ROLE_ADMIN");
        when(accountDAO.findAccount("admin")).thenReturn(account);

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(authorityNames(result)).containsExactly("ROLE_ADMIN");
    }

    private Account activeAccountWithRole(String username, String role) {
        Account account = new Account();
        account.setUserName(username);
        account.setEncrytedPassword("encoded-password");
        account.setUserRole(role);
        account.setActive(true);
        return account;
    }

    private List<String> authorityNames(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());
    }
}
