package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final LoginRepository loginRepository;
    private final StaffShiftRepository staffShiftRepository;

    public Optional<Staff> authenticate(String email, String pas) {

        return loginRepository.findByEmail(email)
                .filter(db -> java.util.Objects.equals(db.getPassword(), pas));
    }

    public Staff findByEmail(String email) {
        return loginRepository.findByEmail(email).orElseThrow();
    }

    @Transactional
    public boolean recordLoginByEmail(String email) {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        Staff staff = this.findByEmail(email);

        var opt = staffShiftRepository.findCurrentShiftByStaffId(staff.getId(), today);

        var ss = opt.get();

        LocalDateTime shiftStart = ss.getShift().getStartTime();
        boolean late = now.isAfter(shiftStart.toLocalTime());

        ss.setStatus(late ? StaffShift.Status.LATE : StaffShift.Status.PRESENT);
        ss.setCheckIn(LocalDateTime.now());
        staffShiftRepository.save(ss);

        return true;
    }

    public void recordLogoutByEmail(String email) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Staff staff = this.findByEmail(email);
        StaffShift staffShift = staffShiftRepository.findCurrentShiftByStaffId(staff.getId(), today).get();

        if (now.isBefore(staffShift.getShift().getEndTime().toLocalTime())){
            staffShift.setStatus(StaffShift.Status.SCHEDULED);
        }
        else{
            staffShift.setStatus(StaffShift.Status.COMPLETED);
        }

        staffShift.setCheckOut(LocalDateTime.now());
        staffShiftRepository.save(staffShift);
    }

}
