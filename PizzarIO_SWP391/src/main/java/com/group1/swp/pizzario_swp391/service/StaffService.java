package com.group1.swp.pizzario_swp391.service;

import java.util.List;
import java.util.stream.Collectors;


// ...existing code...

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.group1.swp.pizzario_swp391.dto.staff.StaffCreateDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffResponseDTO;
import com.group1.swp.pizzario_swp391.dto.staff.StaffUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.mapper.StaffResponseMapper;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffService {
    StaffRepository staffRepository;
    LoginRepository loginRepository;
    @Qualifier("staffResponseMapper")
    StaffResponseMapper staffMapper;

    PasswordEncoder passwordEncoder;

    static final String STAFF_NOT_FOUND = "Staff not found";
    static final String EMAIL_ALREADY_EXISTS = "Email đã được sử dụng";
    static final String PHONE_ALREADY_EXISTS = "Số điện thoại đã được sử dụng";

    public List<Staff> getAll() {
        return staffRepository.findAll();
    }

    public Staff getById(int id) {
        return staffRepository.findById(id).orElse(null);
    }

    public List<StaffResponseDTO> getAllStaff() {
        List<Staff> staffList = staffRepository.findAll();
        return staffList.stream()
                .map(staffMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


    public StaffUpdateDTO getStaffForUpdate(int id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(STAFF_NOT_FOUND));
        return StaffUpdateDTO.builder()
                .name(staff.getName())
                .dateOfBirth(staff.getDateOfBirth())
                .phone(staff.getPhone())
                .address(staff.getAddress())
                .email(staff.getEmail())
                .role(staff.getRole())
                .active(staff.isActive())
                .build();
    }

    public String createNewStaff(StaffCreateDTO createDTO) {
        if (staffRepository.existsByEmail(createDTO.getEmail())) {
            return EMAIL_ALREADY_EXISTS;
        }
        if (staffRepository.existsByPhone(createDTO.getPhone())) {
            return PHONE_ALREADY_EXISTS;
        }
        Staff staff = staffMapper.toEntity(createDTO);
        String encodedPassword = passwordEncoder.encode(createDTO.getPassword());
        staff.setPassword(encodedPassword);
        staffRepository.save(staff);
        return null;
    }

    public String updateStaff(int id, StaffUpdateDTO updateDTO) {
        Staff staff = staffRepository.findById(id)
                .orElse(null);
        if (staff == null) {
            return STAFF_NOT_FOUND;
        }
        if (staffRepository.existsByEmailAndIdNot(updateDTO.getEmail(), id)) {
            return "Email đã tồn tại!";
        }
        if (staffRepository.existsByPhoneAndIdNot(updateDTO.getPhone(), id)) {
            return "Số điện thoại đã tồn tại!";
        }
        staffMapper.updateEntity(staff, updateDTO);
        staffRepository.save(staff);
        return null;
    }

    public void deleteStaffById(int id) {
        if (!staffRepository.existsById(id)) {
            throw new RuntimeException(STAFF_NOT_FOUND);
        }
        staffRepository.deleteById(id);
    }



    public void add(Staff staff){
        staffRepository.save(staff);
    }

    public void updatePasswordByEmail(String email, String password) {
        Staff staff = findByEmail(email);
        String encodedPassword = passwordEncoder.encode(password);

        staff.setPassword(encodedPassword);
        staffRepository.save(staff);
    }

    public Staff findByEmail(String email) {
        Staff staff = loginRepository.findByEmail(email).orElse(null);
        if(staff == null){
            throw new IllegalArgumentException("Không tìm thấy nhân viên với email: " + email);
        }
        return staff;
    }

    public void updateStaff(Staff staff) {
        staffRepository.save(staff);
    }
}
