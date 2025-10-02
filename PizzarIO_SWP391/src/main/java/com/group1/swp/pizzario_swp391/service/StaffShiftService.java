package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.mapper.StaffShiftMapper;
import com.group1.swp.pizzario_swp391.repository.ShiftRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.management.RuntimeMBeanException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffShiftService {

    private final StaffShiftRepository staffShiftRepository;

    private final StaffShiftMapper staffShiftMapper;
    private final StaffRepository staffRepository;
    private final ShiftRepository shiftRepository;


    public List<StaffShift> search(LocalDate from, LocalDate to, Integer shiftId, Integer staffId) {
        return staffShiftRepository.search(from, to, shiftId, staffId);
    }


    @Transactional
    public void create(StaffShiftDTO staffShiftDTO) {

        StaffShift staffShift = staffShiftMapper.toStaffShift(staffShiftDTO);

        staffShift.setStaff(staffRepository.findById(staffShiftDTO.getStaffId()).orElseThrow(() -> new RuntimeException("Staff not found")));
        staffShift.setShift(shiftRepository.findById(staffShiftDTO.getShiftId()).orElseThrow(() -> new RuntimeException("Shift not found")));

        staffShiftRepository.save(staffShift);

    }

    public StaffShiftDTO getById(int id) {
        StaffShift staffShift =  staffShiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StaffShift not found"));
        return staffShiftMapper.toStaffShiftDTO(staffShift);
    }

    @Transactional
    public void update(StaffShiftDTO staffShiftDTO) {
        StaffShift staffShift = staffShiftMapper.toStaffShift(staffShiftDTO);

        staffShift.setStaff(staffRepository.findById(staffShiftDTO.getStaffId()).orElseThrow(() -> new RuntimeException("Staff not found")));
        staffShift.setShift(shiftRepository.findById(staffShiftDTO.getShiftId()).orElseThrow(() -> new RuntimeException("Shift not found")));

        staffShiftMapper.updateStaffShift(staffShift, staffShiftDTO);

        staffShiftRepository.save(staffShift);
    }

    public void delete(int id) {

       StaffShift staffShift = staffShiftRepository.findById(id).orElseThrow(() -> new RuntimeException("STAFFSHIFT NOT FOUND"));

        staffShiftRepository.delete(staffShift);

    }
}