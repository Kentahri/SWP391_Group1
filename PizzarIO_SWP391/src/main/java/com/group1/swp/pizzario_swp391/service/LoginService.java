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

}
