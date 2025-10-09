package com.group1.swp.pizzario_swp391.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.group1.swp.pizzario_swp391.service.LoginService;

import lombok.extern.slf4j.Slf4j;

// ProdSecurityConfig.java
@Profile("!dev")
@Configuration
@EnableMethodSecurity
@Slf4j
public class ProdSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationSuccessHandler roleBasedSuccessHandler(LoginService audit) {
        return (req, res, auth) -> {
            String email = auth.getName();
            try {
                boolean checkedIn = audit.recordLoginByEmail(email);
                // tuỳ chọn: gắn cờ session để hiện toast ở UI
                req.getSession().setAttribute("CHECKIN_RESULT", checkedIn ? "OK" : "SKIPPED");
            } catch (Exception ex) {
                // Không để lỗi check-in phá vỡ đăng nhập/redirect
                req.getSession().setAttribute("CHECKIN_RESULT", "ERROR");
            }
            var a = auth.getAuthorities();
            String target = a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_MANAGER")) ? "/manager"
                    : a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_KITCHEN")) ? "/kitchen"
                            : a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_CASHIER")) ? "/cashier" : "/";
            res.sendRedirect(req.getContextPath() + target);
        };
    }

    @Bean
    LogoutSuccessHandler auditLogoutSuccessHandler(LoginService audit) {
        return (request, response, authentication) -> {
            if (authentication != null) {
                audit.recordLogoutByEmail(authentication.getName());
            }
            response.sendRedirect(request.getContextPath() + "/login?logout");
        };
    }

    @Bean
    SecurityFilterChain prodFilter(
            HttpSecurity http,
            UserDetailsService userDetailsService,
            AuthenticationSuccessHandler roleBasedSuccessHandler,
            LogoutSuccessHandler auditLogoutSuccessHandler) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/webjars/**", "/css/**", "/images/**", "/js/**", "/guest",
                                "/missing_pass/**", "/ws/**", "/app/**", "/topic/**", "/queue/**")
                        .permitAll()
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/kitchen/**").hasRole("KITCHEN")
                        .requestMatchers("/cashier/**").hasRole("CASHIER")
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**", "/app/**", "/topic/**", "/queue/**"))
                .formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email") // <input name="email">
                        .passwordParameter("password") // <input name="password">
                        .successHandler(roleBasedSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(l -> l
                        .logoutUrl("/logout") // URL để gửi logout
                        .logoutSuccessHandler(auditLogoutSuccessHandler)
                        .invalidateHttpSession(true) // huỷ session
                        .clearAuthentication(true) // xoá SecurityContext
                        .deleteCookies("JSESSIONID") // xoá cookie phiên
                        .permitAll());
        return http.build();
    }
}
