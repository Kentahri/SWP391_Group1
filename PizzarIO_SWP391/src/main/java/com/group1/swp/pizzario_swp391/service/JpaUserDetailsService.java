package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    private final StaffRepository repo;

    public JpaUserDetailsService(StaffRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var s = repo.findByEmail(email).orElseThrow(); // email unique
        return User.withUsername(s.getEmail())
                .password(s.getPassword())          // BCrypt đã lưu trong DB
                .roles(s.getRole().name())          // MANAGER/KITCHEN/CASHIER => ROLE_*
                .accountLocked(!s.isActive())
                .build();
    }
}
