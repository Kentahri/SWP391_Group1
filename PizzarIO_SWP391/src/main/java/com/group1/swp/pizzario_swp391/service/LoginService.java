package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Account;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private LoginRepository loginRepository;

    @Autowired
    public LoginService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public Account authenticate(Account acc) {

        return loginRepository.findByEmail(acc.getEmail())
                .filter(db -> java.util.Objects.equals(db.getPassword(), acc.getPassword()))
                .orElse(null);
    }

    public void register(Account acc) {

        if(loginRepository.existsByEmail(acc.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
//
//        String hasded = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(acc.getPassword());
//        acc.setPassword(hasded);

        loginRepository.save(acc);
    }

}
