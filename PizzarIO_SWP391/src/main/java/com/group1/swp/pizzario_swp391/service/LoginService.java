package com.group1.swp.pizzario_swp391.service;


import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private LoginRepository loginRepository;

    @Autowired
    public LoginService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public Optional<Staff> authenticate(String email, String pas) {

        return loginRepository.findByEmail(email)
                .filter(db -> java.util.Objects.equals(db.getPassword(), pas))
                ;
    }

    public void register(Staff acc) {

        if(loginRepository.existsByEmail(acc.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
//
//        String hasded = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(acc.getPassword());
//        acc.setPassword(hasded);

        loginRepository.save(acc);
    }

}
