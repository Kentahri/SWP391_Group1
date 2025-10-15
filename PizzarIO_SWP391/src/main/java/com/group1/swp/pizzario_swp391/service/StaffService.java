package com.group1.swp.pizzario_swp391.service;

import java.util.List;
import java.util.stream.Collectors;


import com.group1.swp.pizzario_swp391.dto.staff.StaffDTO;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public StaffResponseDTO getStaffById(int id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(STAFF_NOT_FOUND));
        return staffMapper.toResponseDTO(staff);
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
                .isActive(staff.isActive())
                .build();
    }

    public void createNewStaff(StaffCreateDTO createDTO) {
        // Validate unique constraints
        if (staffRepository.existsByEmail(createDTO.getEmail())) {
            throw new IllegalArgumentException(EMAIL_ALREADY_EXISTS);
        }
        if (staffRepository.existsByPhone(createDTO.getPhone())) {
            throw new IllegalArgumentException(PHONE_ALREADY_EXISTS);
        }

        Staff staff = staffMapper.toEntity(createDTO);
        String encodedPassword = passwordEncoder.encode(createDTO.getPassword());
        staff.setPassword(encodedPassword);
        staffRepository.save(staff);
    }

    public void updateStaff(int id, StaffUpdateDTO updateDTO) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(STAFF_NOT_FOUND));

        // Validate unique constraints (excluding current staff)
        if (staffRepository.existsByEmailAndIdNot(updateDTO.getEmail(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }
        if (staffRepository.existsByPhoneAndIdNot(updateDTO.getPhone(), id)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }

        staffMapper.updateEntity(staff, updateDTO);
        staffRepository.save(staff);
    }

    public void deleteStaffById(int id) {
        if (!staffRepository.existsById(id)) {
            throw new RuntimeException(STAFF_NOT_FOUND);
        }
        staffRepository.deleteById(id);
    }

    public void updateStaff(int id, StaffDTO staffDTO) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Kiểm tra trùng email (loại trừ chính staff hiện tại)
        if (staffRepository.existsByEmailAndIdNot(staffDTO.getEmail(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        // Kiểm tra trùng số điện thoại
        if (staffRepository.existsByPhoneAndIdNot(staffDTO.getPhone(), id)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }

        // ✅ Tự update field thay vì dùng mapper, để kiểm soát password
        staff.setName(staffDTO.getName());
        staff.setDateOfBirth(staffDTO.getDateOfBirth());
        staff.setPhone(staffDTO.getPhone());
        staff.setAddress(staffDTO.getAddress());
        staff.setEmail(staffDTO.getEmail());
        staff.setRole(staffDTO.getRole());
        staff.setActive(staffDTO.isActive());

        // 🔑 Chỉ update password nếu DTO có giá trị
        if (staffDTO.getPassword() != null && !staffDTO.getPassword().isBlank()) {
            staff.setPassword(staffDTO.getPassword());
        }

        staffRepository.save(staff);
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
