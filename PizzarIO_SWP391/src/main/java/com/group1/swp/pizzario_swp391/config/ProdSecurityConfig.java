package com.group1.swp.pizzario_swp391.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

// ProdSecurityConfig.java
@Profile("!dev")
@Configuration
@EnableMethodSecurity
public class ProdSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // (tuỳ chọn) điều hướng theo role sau khi đăng nhập
    @Bean
    AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (req, res, auth) -> {
            var a = auth.getAuthorities();
            String target =
                    a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_MANAGER")) ? "/manager" :
                            a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_KITCHEN")) ? "/kitchen" :
                                    a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_CASHIER")) ? "/cashier" :
                                            "/";
            res.sendRedirect(req.getContextPath() + target);
        };
    }

    @Bean
    SecurityFilterChain prodFilter(
            HttpSecurity http,
            AuthenticationSuccessHandler roleBasedSuccessHandler
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/webjars/**", "/css/**", "/images/**", "/js/**", "/guest").permitAll()
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/kitchen/**").hasRole("KITCHEN")
                        .requestMatchers("/cashier/**").hasRole("CASHIER")
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email")     // <input name="email">
                        .passwordParameter("password")  // <input name="password">
                        .successHandler(roleBasedSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")                 // URL để gửi logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)          // huỷ session
                        .clearAuthentication(true)            // xoá SecurityContext
                        .deleteCookies("JSESSIONID")          // xoá cookie phiên
                        .permitAll());
        return http.build();
    }
}

