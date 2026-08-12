package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.demo.service.CustomOAuth2UserService;
import com.example.demo.service.UserDetailsServiceImpl;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String API_PATH = "/api/v1/**";
    private static final String[] PUBLIC_MVC_ENDPOINTS = {
            "/", "/productList", "/productImage", "/productDetail",
            "/register", "/forgotPassword", "/resetPassword", "/oauth2/**", "/login/oauth2/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**"
    };
    private static final String[] AUTHENTICATED_MVC_ENDPOINTS = {
            "/wishlist", "/admin/orderList", "/admin/order", "/admin/accountInfo",
            "/admin/user/profile", "/product/review", "/product/review/edit", "/product/review/delete"
    };
    private static final String[] ADMIN_MVC_ENDPOINTS = {
            "/admin/product", "/admin/deleteProduct", "/admin/users", "/admin/user/edit"
    };

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // The REST controllers use the same browser session as MVC, so their
        // state-changing requests must also carry a CSRF token. Registration
        // is the only anonymous REST write and does not act on session state.
        http.csrf().ignoringAntMatchers("/api/v1/**");

        configureAuthorization(http);
        configureExceptionHandling(http);

        // Configuration for Form Login
        http.formLogin()
                .loginProcessingUrl("/j_spring_security_check")
                .loginPage("/admin/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/admin/login?error=true")
                .usernameParameter("userName")
                .passwordParameter("password")
                .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout")).logoutSuccessUrl("/");

        // Configuration for Google OAuth2 Login
        http.oauth2Login()
                .loginPage("/admin/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint()
                .userService(customOAuth2UserService);
    }

    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(PUBLIC_MVC_ENDPOINTS).permitAll()

                // Public REST reads and account registration.
                .antMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()

                // REST operations restricted to administrators.
                .antMatchers(HttpMethod.POST, "/api/v1/products").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/v1/orders/*/status").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/v1/admin/orders/*/return-status").hasRole("ADMIN")

                // Profile/address routes must precede the generic user-management matcher.
                .antMatchers("/api/v1/users/profile", "/api/v1/users/addresses/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/v1/users", "/api/v1/users/*").hasRole("ADMIN")

                // Remaining REST operations require an authenticated account.
                .antMatchers(API_PATH).authenticated()

                // Preserve the existing MVC access rules.
                .antMatchers(AUTHENTICATED_MVC_ENDPOINTS).hasAnyRole("USER", "ADMIN")
                .antMatchers(ADMIN_MVC_ENDPOINTS).hasRole("ADMIN")
                .anyRequest().permitAll();
    }

    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        AntPathRequestMatcher apiRequest = new AntPathRequestMatcher(API_PATH);
        AccessDeniedHandlerImpl apiAccessDeniedHandler = new AccessDeniedHandlerImpl();

        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiRequest)
                .defaultAccessDeniedHandlerFor(apiAccessDeniedHandler, apiRequest)
                .accessDeniedPage("/403");
    }
}
