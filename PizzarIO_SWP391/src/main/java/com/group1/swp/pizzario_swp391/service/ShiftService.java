package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.mapper.ShiftMapper;
import com.group1.swp.pizzario_swp391.repository.ShiftRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;
    private final StaffShiftRepository staffShiftRepository;

    // default salary per assignment (per shift type) — chỉnh theo thực tế
    private final Map<String, Long> defaultSalaryPerShift = Map.of(
            "SANG", 150_000L,
            "CHIEU", 150_000L,
            "TOI", 200_000L,
            "DEM", 250_000L
    );

    public List<ShiftDTO> getAllShift() {
        List<ShiftDTO> list = shiftMapper.toShiftDTOs( shiftRepository.findAll());
        return list;
    }

    public ShiftDTO getShiftById(int id) {
        Shift shift =  shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not existed"));
        ShiftDTO dto = shiftMapper.toShiftDTO(shift);

        return dto;
    }

    public Shift createShift(ShiftDTO shiftDTO) {
        LocalDateTime now = LocalDateTime.now();
        shiftDTO.setCreatedAt(now);

        Shift shift = shiftMapper.toShift(shiftDTO);

        return shiftRepository.save(shift);
    }

    @Transactional
    public void updateShift(int id, ShiftDTO shiftDTO) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new RuntimeException("Shift not found"));
        LocalDateTime now = LocalDateTime.now();
        shiftDTO.setCreatedAt(now);

        shiftMapper.updateShift(shift, shiftDTO);

    }

    public void deleteShift(int id) {
        shiftRepository.deleteById(id);
    }

    @Transactional
    public void deleteShift(Integer id) {
        // 1) Xóa con
        staffShiftRepository.deleteByShift_Id(id);
        // 2) Xóa cha
        shiftRepository.deleteById(id);
    }

    public List<ShiftDTO> findAllDto() {
        List<Shift> shifts = shiftRepository.findAll();
        return shiftMapper.toShiftDTOs(shifts);
    }

    /**
     * Trả LinkedHashMap<label, totalSalary> cho 7 ngày (start..end) giữ thứ tự.
     * Sử dụng StaffShift.workDate và Shift.shiftName để xác định mức lương assignment.
     */
    public LinkedHashMap<String, Long> getWeeklyPayrollTotals() {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) days.add(start.plusDays(i));

        List<StaffShift> assignments = staffShiftRepository.findByWorkDateBetween(start, today);

        // initialize totals
        Map<LocalDate, Long> totals = days.stream().collect(Collectors.toMap(d -> d, d -> 0L, (a,b)->a, LinkedHashMap::new));

        for (StaffShift ss : assignments) {
            LocalDate d = ss.getWorkDate();
            if (d == null) continue;
            String shiftName = ss.getShift() != null && ss.getShift().getShiftName() != null
                    ? ss.getShift().getShiftName().name()
                    : "SANG";
            long salary = defaultSalaryPerShift.getOrDefault(shiftName, 150_000L);
            totals.computeIfPresent(d, (k,v) -> v + salary);
        }

        for (LocalDate d : days) {
            String label = d.getDayOfWeek().getValue() == 7 ? "CN" : "T" + (d.getDayOfWeek().getValue() + 1 - 1);
            result.put(label, totals.getOrDefault(d, 0L));
        }
        return result;
    }
}
