package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.data_analytics.StaffShiftCalendarDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.StatsStaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.WeekDayDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.mapper.StaffShiftMapper;
import com.group1.swp.pizzario_swp391.repository.ShiftRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffShiftService {

        private final StaffShiftRepository staffShiftRepository;

        private final StaffShiftMapper staffShiftMapper;
        private final StaffRepository staffRepository;
        private final ShiftRepository shiftRepository;
        private final StaffShiftManagementService staffShiftManagementService;

        @Transactional
        public void create(StaffShiftDTO staffShiftDTO) {
                // Lấy thông tin shift để lấy salaryPerShift
                Shift shift = shiftRepository.findById(staffShiftDTO.getShiftId())
                        .orElseThrow(() -> new RuntimeException("Shift not found"));

                // Tự động set lương từ shift
                staffShiftDTO.setHourlyWage(BigDecimal.valueOf(shift.getSalaryPerShift()));

                // Mặc định status là SCHEDULED
                staffShiftDTO.setStatus("SCHEDULED");

                // ✅ BUSINESS LOGIC: Convert DTO to Entity
                StaffShift staffShift = staffShiftMapper.toStaffShift(staffShiftDTO);

                staffShift.setStaff(staffRepository.findById(staffShiftDTO.getStaffId())
                        .orElseThrow(() -> new RuntimeException("Staff not found")));
                staffShift.setShift(shift);

                // ✅ SỬA: Save trực tiếp vào Repository (không có event)
                staffShiftRepository.save(staffShift);

        }

        public StaffShiftDTO getById(int id) {
                StaffShift staffShift = staffShiftRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("StaffShift not found"));
                return staffShiftMapper.toStaffShiftDTO(staffShift);
        }

        @Transactional
        public void update(StaffShiftDTO staffShiftDTO, int id) {
                StaffShift staffShift = staffShiftRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("STAFF SHIFT NOT FOUND"));

                // Lấy thông tin shift mới để cập nhật lương
                Shift newShift = shiftRepository.findById(staffShiftDTO.getShiftId())
                                .orElseThrow(() -> new RuntimeException("Shift not found"));

                // Tự động cập nhật lương từ shift mới
                staffShiftDTO.setHourlyWage(BigDecimal.valueOf(newShift.getSalaryPerShift()));

                staffShift.setStaff(staffRepository.findById(staffShiftDTO.getStaffId())
                                .orElseThrow(() -> new RuntimeException("Staff not found")));
                staffShift.setShift(newShift);

                staffShiftMapper.updateStaffShift(staffShift, staffShiftDTO);

                staffShiftRepository.save(staffShift);
        }

        public void delete(int id) {

                StaffShift staffShift = staffShiftRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("STAFFSHIFT NOT FOUND"));

                staffShiftRepository.delete(staffShift);
        }

        public StatsStaffShiftDTO getAnalyticsStaffShift() {
                Integer totalShifts = staffShiftRepository.findAll().size();
                Integer totalHours = staffShiftRepository.totalHours();
                Double totalWage = staffShiftRepository.totalWage();
                Integer completedShifts = staffShiftRepository.completedShift();

                return new StatsStaffShiftDTO(totalShifts, totalHours, totalWage, completedShifts);
        }

        public List<StaffShiftResponseDTO> findByDateRangeWithFilters(LocalDate weekStart, LocalDate weekEnd,
                        Long staffId, Long shiftId) {
                List<StaffShift> staffShifts;

                if (staffId != null && shiftId != null) {
                        staffShifts = staffShiftRepository.findByWeekRangeAndFilters(
                                        weekStart, weekEnd, staffId.intValue(), shiftId.intValue());
                } else if (staffId != null) {
                        staffShifts = staffShiftRepository.findByWeekRangeAndStaff(
                                        weekStart, weekEnd, staffId.intValue());
                } else if (shiftId != null) {
                        staffShifts = staffShiftRepository.findByWeekRangeAndShift(
                                        weekStart, weekEnd, shiftId.intValue());
                } else {
                        staffShifts = staffShiftRepository.findByWeekRange(weekStart, weekEnd);
                }

                return staffShifts.stream()
                                .map(staffShiftMapper::toResponseDTO)
                                .collect(Collectors.toList());
        }

        public List<WeekDayDTO> buildWeekDays(LocalDate weekStart, LocalDate weekEnd,
                        List<StaffShiftResponseDTO> staffShifts) {

                Map<LocalDate, List<StaffShiftCalendarDTO>> shiftsByDate = staffShifts.stream()
                                .collect(Collectors.groupingBy(
                                                StaffShiftResponseDTO::getWorkDate,
                                                Collectors.mapping(
                                                                staffShiftMapper::toCalendarDTO,
                                                                Collectors.toList())));

                List<WeekDayDTO> weekDays = new ArrayList<>();
                String[] dayNames = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                LocalDate today = LocalDate.now();

                for (int i = 0; i < 7; i++) {
                        LocalDate date = weekStart.plusDays(i);
                        List<StaffShiftCalendarDTO> shiftsForDay = shiftsByDate.getOrDefault(date, new ArrayList<>());

                        weekDays.add(new WeekDayDTO(
                                        date,
                                        dayNames[i],
                                        date.equals(today),
                                        shiftsForDay));
                }

                return weekDays;
        }

        public List<StaffShift> findAllShiftsByStaffIdAndDate(Integer staffId, LocalDate workDate) {
                return staffShiftRepository.findAllShiftsByStaffIdAndDate(staffId, workDate);
        }
}