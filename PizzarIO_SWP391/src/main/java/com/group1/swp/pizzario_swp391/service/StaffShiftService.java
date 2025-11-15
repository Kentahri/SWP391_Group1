package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.data_analytics.StaffShiftCalendarDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.StatsStaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.data_analytics.WeekDayDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.ManualCompleteShiftRequest;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.event.staff.StaffShiftUpdatedEvent;
import com.group1.swp.pizzario_swp391.mapper.StaffShiftMapper;
import com.group1.swp.pizzario_swp391.repository.ShiftRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffShiftService {

        private final StaffShiftRepository staffShiftRepository;

        private final StaffShiftMapper staffShiftMapper;
        private final StaffRepository staffRepository;
        private final ShiftRepository shiftRepository;
        private final StaffShiftManagementService staffShiftManagementService;
        private final ApplicationEventPublisher eventPublisher;
        private final StaffScheduleService staffScheduleService;

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

                // Publish event to reschedule tasks when shift is updated
                log.info("🔄 Publishing StaffShiftUpdatedEvent for shift ID: {} to reschedule tasks", id);
                eventPublisher.publishEvent(new StaffShiftUpdatedEvent(this, staffShift, true));
                log.info("✅ StaffShiftUpdatedEvent published successfully for shift ID: {}", id);
        }

        @Transactional
        public void delete(int id) {

                StaffShift staffShift = staffShiftRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("STAFFSHIFT NOT FOUND"));

                staffShiftRepository.delete(staffShift);

                // Hủy tất cả scheduled tasks (absent check + auto-complete) cho ca làm này
                staffScheduleService.cancelAllTasks(id);
                log.info("✅ Deleted shift {} and cancelled all scheduled tasks", id);
        }

        /**
         * Kiểm tra xem ca làm có thể xóa được không
         * - Chỉ cho phép xóa nếu ca chưa bắt đầu (workDate + startTime > hiện tại)
         * - Và status không phải COMPLETED hoặc CANCELLED
         */
        public boolean canDelete(int id) {
                StaffShift staffShift = staffShiftRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("STAFFSHIFT NOT FOUND"));

                // Không cho xóa nếu đã COMPLETED hoặc CANCELLED
                String status = staffShift.getStatus().name();
                if (!"SCHEDULED".equals(status)) {
                        return false;
                }

                // Kiểm tra thời gian: workDate + startTime phải > hiện tại
                LocalDateTime shiftStartDateTime = LocalDateTime.of(
                                staffShift.getWorkDate(),
                                staffShift.getShift().getStartTime().toLocalTime());

                return shiftStartDateTime.isAfter(LocalDateTime.now());
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


        @Transactional
        public void manuallyCompleteShift(int id, ManualCompleteShiftRequest request) {
                // Load shift with pessimistic lock to prevent concurrent updates
                StaffShift staffShift = staffShiftRepository.findByIdWithLock(id)
                        .orElseThrow(() -> new RuntimeException("Staff shift not found with ID: " + id));

                // Validate shift is in NOT_CHECKOUT status
                if (staffShift.getStatus() != StaffShift.Status.NOT_CHECKOUT) {
                        throw new RuntimeException("Can only manually complete shifts with status NOT_CHECKOUT. Current status: " + staffShift.getStatus());
                }

                // Validate checkout time is not null
                if (request.getCheckoutTime() == null) {
                        throw new RuntimeException("Checkout time cannot be null");
                }

                // Validate checkout time is after check-in time
                if (staffShift.getCheckIn() != null && request.getCheckoutTime().isBefore(staffShift.getCheckIn())) {
                        throw new RuntimeException("Checkout time cannot be before check-in time");
                }

                // Validate checkout time is not in the future
                if (request.getCheckoutTime().isAfter(LocalDateTime.now())) {
                        throw new RuntimeException("Checkout time cannot be in the future");
                }

                // Set checkout time
                staffShift.setCheckOut(request.getCheckoutTime());

                // Determine status based on checkout time vs shift end time
                LocalTime checkoutTime = request.getCheckoutTime().toLocalTime();
                LocalTime shiftEndTime = staffShift.getShift().getEndTime().toLocalTime();

                if (checkoutTime.isBefore(shiftEndTime)) {
                        // Checked out before shift end -> LEFT_EARLY
                        staffShift.setStatus(StaffShift.Status.LEFT_EARLY);
                        log.info("Manual complete: Shift {} marked as LEFT_EARLY (checkout: {}, shift end: {})",
                                id, checkoutTime, shiftEndTime);
                } else {
                        // Checked out at or after shift end -> COMPLETED
                        staffShift.setStatus(StaffShift.Status.COMPLETED);
                        log.info("Manual complete: Shift {} marked as COMPLETED (checkout: {}, shift end: {})",
                                id, checkoutTime, shiftEndTime);
                }

                // Update penalty percent
                staffShift.setPenaltyPercent(request.getPenaltyPercent());

                // Append manager's note with timestamp
                String existingNote = staffShift.getNote() != null ? staffShift.getNote() : "";
                String manualCompleteNote = String.format(
                        "[MANUAL COMPLETE by Manager at %s] %s | Penalty: %d%%",
                        LocalDateTime.now(),
                        request.getNote(),
                        request.getPenaltyPercent()
                );
                staffShift.setNote(existingNote + " | " + manualCompleteNote);

                // Save the shift
                staffShiftRepository.save(staffShift);

                // Publish event to cancel any scheduled tasks for this shift
                eventPublisher.publishEvent(new StaffShiftUpdatedEvent(this, staffShift, true));

                log.info("StaffShiftService: Manually completed shift {} for staff {} - Status: {}, Checkout: {}, Penalty: {}%",
                        id, staffShift.getStaff().getName(), staffShift.getStatus(),
                        request.getCheckoutTime(), request.getPenaltyPercent());
        }
}