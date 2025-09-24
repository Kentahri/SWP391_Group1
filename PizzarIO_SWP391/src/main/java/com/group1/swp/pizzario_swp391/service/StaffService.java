package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffService {

    private StaffRepository staffRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    public StaffService(StaffRepository loginRepository) {
        this.staffRepository = loginRepository;
    }

    public void add(Staff staff){
        staffRepository.save(staff);
    }

    public void updatePasswordByEmail(String email, String password) {
        Staff staff = loginRepository.findByEmail(email).orElse(null);

        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với email: " + email);
        }

        staff.setPassword(password);
        staffRepository.save(staff);
    }

    public Staff findByEmail(String email){
        Staff staff = loginRepository.findByEmail(email).orElse(null);

        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với email: " + email);
        }
        return staff;
    }

}
