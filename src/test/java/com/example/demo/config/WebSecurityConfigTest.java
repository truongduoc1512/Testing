package com.example.demo.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.AccountDAO;
import com.example.demo.service.CustomOAuth2UserService;
import com.example.demo.service.UserDetailsServiceImpl;

@WebMvcTest(controllers = WebSecurityConfigTest.SecurityRouteTestController.class)
@ContextConfiguration(classes = {
        WebSecurityConfig.class,
        WebSecurityConfigTest.SecurityRouteTestController.class
})
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean(name = "userDetailsService")
    private UserDetailsService defaultUserDetailsService;

    @MockBean
    private AccountDAO accountDAO;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void productRead_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/products/P001"))
                .andExpect(status().isOk());
    }

    @Test
    void reviewRead_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/product/P001"))
                .andExpect(status().isOk());
    }

    @Test
    void registration_isPublic() throws Exception {
        mockMvc.perform(post("/api/v1/users/register"))
                .andExpect(status().isOk());
    }

    @Test
    void productMutation_requiresAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/products/P001")
                .with(csrf())
                .with(user("seller").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void userManagement_requiresAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users/alice")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void cart_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/cart")
                .with(user("customer").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void profileAndAddress_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users/addresses")
                .with(user("customer").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void orderRead_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/orders/O001"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/orders/O001")
                .with(user("customer").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void orderStatusMutation_requiresAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/orders/O001/status")
                .with(csrf())
                .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/orders/O001/status")
                .with(csrf())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void reviewMutation_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/reviews").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/reviews/1")
                .with(csrf())
                .with(user("customer").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void returnWorkflow_requiresExpectedRoles() throws Exception {
        mockMvc.perform(post("/api/v1/orders/O001/return").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/orders/O001/return")
                .with(csrf())
                .with(user("customer").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/orders/O001/return-status")
                .with(csrf())
                .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/admin/orders/O001/return-status")
                .with(csrf())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void existingMvcAccessRules_arePreserved() throws Exception {
        mockMvc.perform(get("/productList"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/orderList")
                .with(user("customer").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/product")
                .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/product")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @RestController
    static class SecurityRouteTestController {

        @GetMapping({
                "/api/v1/products/P001", "/api/v1/reviews/product/P001",
                "/api/v1/users", "/api/v1/users/alice", "/api/v1/cart",
                "/api/v1/users/profile", "/api/v1/users/addresses",
                "/api/v1/orders/O001", "/productList", "/admin/orderList", "/admin/product"
        })
        ResponseEntity<Void> getRoute() {
            return ResponseEntity.ok().build();
        }

        @PostMapping({
                "/api/v1/users/register", "/api/v1/products", "/api/v1/reviews",
                "/api/v1/orders/O001/return"
        })
        ResponseEntity<Void> postRoute() {
            return ResponseEntity.ok().build();
        }

        @PutMapping({
                "/api/v1/orders/O001/status", "/api/v1/reviews/1",
                "/api/v1/admin/orders/O001/return-status"
        })
        ResponseEntity<Void> putRoute() {
            return ResponseEntity.ok().build();
        }

        @DeleteMapping("/api/v1/products/P001")
        ResponseEntity<Void> deleteRoute() {
            return ResponseEntity.ok().build();
        }
    }
}
