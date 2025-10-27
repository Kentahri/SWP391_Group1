package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.event.staff.StaffShiftCreatedEvent;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import com.group1.swp.pizzario_swp391.repository.StaffRepository;
import com.group1.swp.pizzario_swp391.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffShiftManagementService {

        private final StaffShiftRepository staffShiftRepository;
        private final StaffRepository staffRepository;
        private final ShiftRepository shiftRepository;
        private final ApplicationEventPublisher eventPublisher;

        /**
         * CHỨC NĂNG: Tạo mới StaffShift từ DTO và trigger event để schedule tasks
         * Được gọi từ Controller Layer
         */
        @Transactional
        public void createStaffShiftFromDTO(StaffShiftDTO staffShiftDTO) {
                // ✅ BUSINESS LOGIC: Convert DTO to Entity
                StaffShift staffShift = new StaffShift();

                // Set Staff entity
                staffShift.setStaff(staffRepository.findById(staffShiftDTO.getStaffId())
                                .orElseThrow(() -> new RuntimeException("Staff not found")));

                // Set Shift entity
                staffShift.setShift(shiftRepository.findById(staffShiftDTO.getShiftId())
                                .orElseThrow(() -> new RuntimeException("Shift not found")));

                // Set other properties
                staffShift.setWorkDate(staffShiftDTO.getWorkDate());
                staffShift.setStatus(StaffShift.Status.SCHEDULED);
                staffShift.setHourlyWage(
                                staffShiftDTO.getHourlyWage() != null ? staffShiftDTO.getHourlyWage().intValue() : 0);
                staffShift.setPenaltyPercent(0);
                staffShift.setNote("");

                // ✅ DATA ACCESS: Save to database
                StaffShift savedShift = staffShiftRepository.save(staffShift);

                // ✅ EVENT PUBLISHING: Trigger event để schedule tasks
                log.info("🔄 Publishing StaffShiftCreatedEvent for shift ID: {}", savedShift.getId());
                eventPublisher.publishEvent(new StaffShiftCreatedEvent(this, savedShift));
                log.info("✅ StaffShiftCreatedEvent published successfully for shift ID: {}", savedShift.getId());

                log.info("Created StaffShift ID: {} for staff: {} on {} - triggered event",
                                savedShift.getId(),
                                savedShift.getStaff().getName(),
                                savedShift.getWorkDate());
        }

}