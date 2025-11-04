package com.group1.swp.pizzario_swp391.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.group1.swp.pizzario_swp391.dto.staff.StaffLoginDTO;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import com.group1.swp.pizzario_swp391.service.LoginService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

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
                audit.recordLoginByEmail(email);
            } catch (Exception ex) {
                log.warn("Warning: " + ex);
            }
            var a = auth.getAuthorities();
            String target = a.stream().anyMatch(x -> x.getAuthority().equals("ROLE_MANAGER")) ? "/manager/analytics"
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
    AuthenticationFailureHandler myFailureHandler(Validator validator, StaffRepository staffRepository) {
        return (req, res, ex) -> {

            String email = req.getParameter("email");
            String password = req.getParameter("password");

            var dto = StaffLoginDTO.builder()
                    .email(email)
                    .password(password)
                    .build();

            var checkValidation = validator.validate(dto);

            String code, msg;

            if (!checkValidation.isEmpty()) {

                var byField = checkValidation.stream().collect(
                        Collectors.groupingBy(v -> v.getPropertyPath().toString(),
                                Collectors.mapping(ConstraintViolation::getMessage,
                                        Collectors.toList())));

                String emailErr = String.join("<br/>", byField.getOrDefault("email", List.of()));
                String passErr = String.join("<br/>", byField.getOrDefault("password", List.of()));

                code = "validation";
                msg = (emailErr.isEmpty() ? "" : "<b>Email:</b> " + emailErr)
                        + (!emailErr.isEmpty() && !passErr.isEmpty() ? "<br/>" : "")
                        + (passErr.isEmpty() ? "" : "<b>Mật khẩu:</b> " + passErr);
            } else if (ex instanceof BadCredentialsException) {

                boolean checkMail = (email != null && staffRepository.existsByEmail(email));
                if (!checkMail) {
                    code = "email_not_found";
                    msg = "Email không tồn tại";
                } else {
                    code = "bad_password";
                    msg = "Mật khẩu không đúng";
                }
            } else if (ex instanceof AccountStatusException) {
                // gồm Locked/AccountExpired/CredentialsExpired
                code = "account_status";
                msg = "Tài khoản không ở trạng thái hợp lệ. Yêu cầu manager cấp quyền";
            } else if (ex instanceof InternalAuthenticationServiceException) {
                code = "internal_auth";
                msg = "Lỗi nội bộ khi xác thực (vui lòng thử lại)";
            } else if (ex instanceof AuthenticationException) {
                code = "auth_error";
                msg = "Không thể xác thực. Vui lòng thử lại.";
            } else {
                code = "unknown";
                msg = "Đăng nhập thất bại. Vui lòng thử lại.";
            }

            req.getSession().setAttribute("LOGIN_ERROR_MSG", msg);
            res.sendRedirect(req.getContextPath() + "/login?error=" + code);
        };
    }

    @Bean
    SecurityFilterChain prodFilter(
            HttpSecurity http,
            UserDetailsService userDetailsService,
            AuthenticationSuccessHandler roleBasedSuccessHandler,
            LogoutSuccessHandler auditLogoutSuccessHandler, AuthenticationFailureHandler myFailureHandler)
            throws Exception {
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/webjars/**", "/css/**", "/images/**", "/static/js/**", "/js/**",
                                "/guest/**", "/missing_pass/**", "/ws/**", "/app/**", "/topic/**", "/queue/**",
                                "/api/chatbot/**")
                        .permitAll()
                        .requestMatchers("/manager/**").hasRole("MANAGER")
                        .requestMatchers("/kitchen/**").hasRole("KITCHEN")
                        .requestMatchers("/cashier/**").hasRole("CASHIER")
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/ws/**", "/app/**", "/topic/**", "/queue/**", "/api/chatbot/**", "/guest/payment/**"))
                // Session Management: Cho phép nhiều session độc lập
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId() // Đổi session ID sau khi login
                        .maximumSessions(10) // Cho phép tối đa 10 sessions đồng thời
                        .maxSessionsPreventsLogin(false)
                        .expiredSessionStrategy(event -> event.getResponse().sendRedirect("/pizzario/login?expired"))
                )
                .formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("email") // <input name="email">
                        .passwordParameter("password") // <input name="password">
                        .successHandler(roleBasedSuccessHandler)
                        .failureHandler(myFailureHandler)
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