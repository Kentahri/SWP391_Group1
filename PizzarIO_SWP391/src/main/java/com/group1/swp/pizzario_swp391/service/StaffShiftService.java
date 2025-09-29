package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StaffShiftService {

    private final StaffShiftRepository staffShiftRepository;

    public StaffShiftService(StaffShiftRepository staffShiftRepository) {
        this.staffShiftRepository = staffShiftRepository;
    }

    public List<StaffShift> search(LocalDate from, LocalDate to, Integer shiftId, Integer staffId) {
        return staffShiftRepository.search(from, to, shiftId, staffId);
    }

    public void create(StaffShift staffShift) {
        staffShiftRepository.save(staffShift);
    }

    public StaffShift getById(int id) {
        return staffShiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StaffShift not found"));
    }

    public void update(StaffShift staffShift) {
        staffShiftRepository.save(staffShift);
    }

    public void delete(int id) {

        StaffShift staffShift = staffShiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StaffShift not existed"));

        staffShiftRepository.delete(staffShift);
    }
}