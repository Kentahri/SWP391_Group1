package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
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
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.mapper.StaffResponseMapper;
import com.group1.swp.pizzario_swp391.mapper.StaffShiftMapper;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;

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
    StaffShiftRepository staffShiftRepository;
    StaffShiftMapper staffShiftMapper;
    StaffShiftExcelExportService staffShiftExcelExportService;

    PasswordEncoder passwordEncoder;

    static final String STAFF_NOT_FOUND = "Staff not found";
    static final String EMAIL_ALREADY_EXISTS = "Email đã được sử dụng";
    static final String PHONE_ALREADY_EXISTS = "Số điện thoại đã được sử dụng";
    static final String AGE_NOT_ELIGIBLE = "Nhân viên phải đủ 18 tuổi";
    static final String MANAGER_ALREADY_EXISTS = "Hệ thống chỉ cho phép 1 manager";
    static final int MIN_AGE = 18;

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

//    public List<StaffResponseDTO> getAllStaffByRole(Staff.Role role) {
//        if (role == null) {
//            return getAllStaff();
//        }
//        List<Staff> staffList = staffRepository.findByRole(role);
//        return staffList.stream()
//                .map(staffMapper::toResponseDTO)
//                .collect(Collectors.toList());
//    }

    public List<StaffResponseDTO> getStaffWithoutManager() {
        List<Staff> staffList = staffRepository.findAll();
        return staffList.stream()
                .filter(staff -> staff.getRole() != Staff.Role.MANAGER)
                .map(staffMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available roles for staff creation/update
     * If a manager already exists, exclude MANAGER role from the list
     * @param currentStaffId ID of staff being edited (null for create)
     * @return List of available roles
     */
    public List<Staff.Role> getAvailableRoles(Integer currentStaffId) {
        List<Staff.Role> allRoles = Arrays.asList(Staff.Role.values());
        boolean managerExists = staffRepository.existsByRole(Staff.Role.MANAGER);
        
        // If manager exists and we're not editing the current manager, remove MANAGER from list
        if (managerExists) {
            if (currentStaffId != null) {
                Staff currentStaff = staffRepository.findById(currentStaffId).orElse(null);
                // If current staff is the manager, allow MANAGER role (they can keep it)
                if (currentStaff != null && currentStaff.getRole() == Staff.Role.MANAGER) {
                    return allRoles;
                }
            }
            // Remove MANAGER from available roles
            return allRoles.stream()
                    .filter(role -> role != Staff.Role.MANAGER)
                    .collect(Collectors.toList());
        }
        
        return allRoles;
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
        
        // Validate age - must be at least 18 years old
        if (!isAgeEligible(createDTO.getDateOfBirth())) {
            return AGE_NOT_ELIGIBLE;
        }
        
        // Validate: Only allow 1 manager
        if (createDTO.getRole() == Staff.Role.MANAGER && staffRepository.existsByRole(Staff.Role.MANAGER)) {
            return MANAGER_ALREADY_EXISTS;
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

        // Only check email uniqueness if email has changed
        if (!staff.getEmail().equals(updateDTO.getEmail()) &&
                staffRepository.existsByEmail(updateDTO.getEmail())) {
            return "Email đã tồn tại!";
        }

        // Only check phone uniqueness if phone has changed
        if (!staff.getPhone().equals(updateDTO.getPhone()) &&
                staffRepository.existsByPhone(updateDTO.getPhone())) {
            return "Số điện thoại đã tồn tại!";
        }

        // Validate age - must be at least 18 years old
        if (!isAgeEligible(updateDTO.getDateOfBirth())) {
            return AGE_NOT_ELIGIBLE;
        }

        // Validate: Only allow 1 manager
        // If trying to change role to MANAGER and manager already exists (and current staff is not the manager)
        if (updateDTO.getRole() == Staff.Role.MANAGER && 
            staff.getRole() != Staff.Role.MANAGER &&
            staffRepository.existsByRole(Staff.Role.MANAGER)) {
            return MANAGER_ALREADY_EXISTS;
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

    public void add(Staff staff) {
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
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với email: " + email);
        }
        return staff;
    }

    public Staff findByEmailValid(String email){
        Staff staff = loginRepository.findByEmail(email).orElse(null);
        return staff;
    }

    public void updateStaff(Staff staff) {
        staffRepository.save(staff);
    }

    public void toggleStaffActive(int id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(STAFF_NOT_FOUND));
        staff.setActive(!staff.isActive());
        staffRepository.save(staff);
    }

    public List<Staff> searchByName(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return staffRepository.findTopNOrderByNameAsc(limit); // fallback khi chưa gõ, lấy một ít đầu danh sách
        }
        return staffRepository.searchTopByName(keyword, limit);
    }

    /**
     * Check if the person is at least 18 years old based on date of birth
     * @param dateOfBirth the date of birth to check
     * @return true if age is at least 18, false otherwise
     */
    private boolean isAgeEligible(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        int age = Period.between(dateOfBirth, today).getYears();
        return age >= MIN_AGE;
    }

    /**
     * Calculate total monthly salary for current month
     * @return total monthly salary
     */
    public Double getTotalMonthlySalary() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        var staffShifts = staffShiftRepository.findByMonthRange(monthStart, monthEnd);
        double total = 0.0;
        for (var ss : staffShifts) {
            StaffShiftResponseDTO dto = staffShiftMapper.toResponseDTO(ss);
            total += staffShiftExcelExportService.calculateActualWage(dto);
        }
        return total;
    }

    /**
     * Calculate average salary per staff for current month
     * @return average salary per staff
     */
    public Double getAverageSalaryPerStaff() {
        Double totalMonthly = getTotalMonthlySalary();
        long activeStaffCount = getAllStaff().stream()
                .filter(StaffResponseDTO::isActive)
                .count();
        
        if (activeStaffCount == 0) {
            return 0.0;
        }
        
        return totalMonthly / activeStaffCount;
    }

}
