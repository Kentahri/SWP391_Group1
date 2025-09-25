package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.StaffDTO;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.mapper.StaffMapper;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffService {
    StaffRepository staffRepository;
    StaffMapper staffMapper;

    public void createNewStaff(StaffDTO staffDTO) {
        Staff staff =staffMapper.toStaff(staffDTO);

        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // Kiểm tra trùng phone
        if (staffRepository.existsByPhone(staff.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng");
        }

        staffRepository.save(staff);
    }

    public java.util.List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Optional<Staff> getStaffById(int id) {
        return staffRepository.findById(id);
    }

    public void deleteStaffById(int id) {
        staffRepository.deleteById(id);
    }

    public void updateStaff(int id,StaffDTO staffDTO) {
        Staff staff = staffRepository.findById(id).orElseThrow(()-> new RuntimeException("Staff not found"));
        // Kiểm tra trùng email (loại trừ chính staff hiện tại)
        if (staffRepository.existsByEmailAndIdNot(staffDTO.getEmail(), id)) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        // Kiểm tra trùng số điện thoại
        if (staffRepository.existsByPhoneAndIdNot(staffDTO.getPhone(), id)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }
        staffMapper.updateStaff(staff,staffDTO);
        staffRepository.save(staff);
    }
}
