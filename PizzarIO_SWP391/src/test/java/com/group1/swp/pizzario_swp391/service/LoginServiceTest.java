package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LoginServiceTest.class);
    private static final DateTimeFormatter VIETNAM_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private StaffShiftRepository staffShiftRepository;

    @InjectMocks
    private LoginService loginService;

    private Staff testStaff;
    private Shift testShift;
    private StaffShift testStaffShift;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        // Lấy tên test method thực sự đang chạy
        String testMethod = testInfo.getDisplayName();
        log.info("Setting up test data for: {}", testMethod);

        // Setup test data
        testStaff = new Staff();
        testStaff.setId(1);
        testStaff.setName("Test Staff");
        testStaff.setEmail("test@example.com");

        testShift = new Shift();
        testShift.setId(1);
        testShift.setShiftName(Shift.ShiftType.SÁNG);
        // Set shift time to be around current time for testing
        LocalDateTime now = LocalDateTime.now();
        testShift.setStartTime(now.minusMinutes(5)); // 5 minutes ago
        testShift.setEndTime(now.plusHours(4)); // 4 hours from now

        testStaffShift = new StaffShift();
        testStaffShift.setId(1);
        testStaffShift.setStaff(testStaff);
        testStaffShift.setShift(testShift);
        testStaffShift.setWorkDate(LocalDate.now());
        testStaffShift.setStatus(StaffShift.Status.SCHEDULED);
        testStaffShift.setCheckIn(null);
        testStaffShift.setCheckOut(null);

        log.info("Test data setup completed - Staff: {}, Shift: {}, Status: {}",
                testStaff.getName(), testShift.getShiftName(), testStaffShift.getStatus());
    }

    // ========== TEST LOGIN CASES ==========

    @Test
    @DisplayName("Test Case 1: Login đúng giờ")
    void testCase1_LoginOnTime() {
        log.info("TEST CASE 1: Login đúng giờ");
        log.info("Scenario: Nhân viên check-in đúng giờ bắt đầu ca");

        // Case 1: Đi đúng giờ
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        log.info("Shift start time: {}", testShift.getStartTime().format(VIETNAM_FORMAT));
        log.info("Current time: {}", LocalDateTime.now().format(VIETNAM_FORMAT));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        log.info("Login result: {}", result);
        log.info("Staff shift status: {}", testStaffShift.getStatus());
        log.info("Penalty percent: {}%", testStaffShift.getPenaltyPercent());
        log.info("Staff login time: {}", testStaffShift.getCheckIn().format(VIETNAM_FORMAT));

        assertTrue(result);
        assertEquals(StaffShift.Status.PRESENT, testStaffShift.getStatus());
        assertEquals(0, testStaffShift.getPenaltyPercent());
        assertNotNull(testStaffShift.getCheckIn());
        verify(staffShiftRepository).save(testStaffShift);

        log.info("✅ TEST CASE 1 PASSED: Login đúng giờ thành công");
    }

    @Test
    void testCase1_5_LoginEarly() {
        log.info("TEST CASE 1.5: Login sớm");
        log.info("Scenario: Nhân viên check-in trước giờ bắt đầu ca 30 phút");

        // Case 1.5: Đi sớm (trước giờ bắt đầu ca)
        testShift.setStartTime(LocalDateTime.now().plusMinutes(30)); // Ca bắt đầu sau 30 phút
        testShift.setEndTime(LocalDateTime.now().plusHours(4));

        log.info("Shift start time: {} (30 phút sau)", testShift.getStartTime().format(VIETNAM_FORMAT));
        log.info("Current time: {}", LocalDateTime.now().format(VIETNAM_FORMAT));
        log.info("Early duration: 30 phút");

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        log.info("Login result: {}", result);
        log.info("Staff shift status: {} (Expected: PRESENT)", testStaffShift.getStatus());
        log.info("Penalty percent: {}% (Expected: 0%)", testStaffShift.getPenaltyPercent());
        log.info("Staff login time: {}", testStaffShift.getCheckIn().format(VIETNAM_FORMAT));
        log.info("Note: {}", testStaffShift.getNote());

        assertTrue(result);
        assertEquals(StaffShift.Status.PRESENT, testStaffShift.getStatus());
        assertEquals(0, testStaffShift.getPenaltyPercent());
        assertNotNull(testStaffShift.getCheckIn());
        assertTrue(testStaffShift.getNote().contains("PRESENT"));
        verify(staffShiftRepository).save(testStaffShift);

        log.info("✅ TEST CASE 1.5 PASSED: Login sớm thành công");
    }

    @Test
    void testCase1_6_MultipleShiftsSelectClosest() {
        log.info(" TEST CASE 1.6: Nhiều ca làm việc - chọn ca gần nhất");
        log.info(" Scenario: Nhân viên có 2 ca trong ngày, hệ thống chọn ca gần thời gian hiện tại nhất");

        // Case 1.6: Nhiều ca làm việc - chọn ca gần nhất (cover line 120)
        LocalDateTime now = LocalDateTime.now();

        // Tạo shift 1: bắt đầu 30 phút trước (xa hơn)
        Shift shift1 = new Shift();
        shift1.setId(1);
        shift1.setShiftName(Shift.ShiftType.SÁNG);
        shift1.setStartTime(now.minusMinutes(30));
        shift1.setEndTime(now.plusHours(4));

        StaffShift staffShift1 = new StaffShift();
        staffShift1.setId(1);
        staffShift1.setStaff(testStaff);
        staffShift1.setShift(shift1);
        staffShift1.setWorkDate(LocalDate.now());
        staffShift1.setStatus(StaffShift.Status.SCHEDULED);
        staffShift1.setCheckIn(null);
        staffShift1.setCheckOut(null);

        // Tạo shift 2: bắt đầu 5 phút trước (gần hơn)
        Shift shift2 = new Shift();
        shift2.setId(2);
        shift2.setShiftName(Shift.ShiftType.CHIỀU);
        shift2.setStartTime(now.minusMinutes(5));
        shift2.setEndTime(now.plusHours(4));

        StaffShift staffShift2 = new StaffShift();
        staffShift2.setId(2);
        staffShift2.setStaff(testStaff);
        staffShift2.setShift(shift2);
        staffShift2.setWorkDate(LocalDate.now());
        staffShift2.setStatus(StaffShift.Status.SCHEDULED);
        staffShift2.setCheckIn(null);
        staffShift2.setCheckOut(null);

        log.info(" Shift 1 (SÁNG): Start at {} (30 phút trước)", shift1.getStartTime().format(VIETNAM_FORMAT));
        log.info(" Shift 2 (CHIỀU): Start at {} (5 phút trước)", shift2.getStartTime().format(VIETNAM_FORMAT));
        log.info(" Current time: {}", now.format(VIETNAM_FORMAT));
        log.info(" Expected: Chọn Shift 2 (gần hơn)");

        // Mock repository trả về 2 shifts
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(staffShift1, staffShift2));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        log.info("✅ Login result: {}", result);
        log.info(" Selected shift status: {} (Expected: PRESENT)", staffShift2.getStatus());
        log.info(" Penalty percent: {}% (Expected: 0%)", staffShift2.getPenaltyPercent());
        log.info(" Note: {}", staffShift2.getNote());

        assertTrue(result);
        // Phải chọn shift2 (gần hơn) thay vì shift1
        assertEquals(StaffShift.Status.PRESENT, staffShift2.getStatus());
        assertEquals(0, staffShift2.getPenaltyPercent());
        assertNotNull(staffShift2.getCheckIn());
        assertTrue(staffShift2.getNote().contains("PRESENT"));
        verify(staffShiftRepository).save(staffShift2);

        log.info("✅ TEST CASE 1.6 PASSED: Chọn ca gần nhất thành công");
    }

    @Test
    void testCase1_7_LoginWithExistingNote() {
        // Case 1.7: Login khi shift đã có note từ trước (cover dòng 98)
        testShift.setStartTime(LocalDateTime.now().minusMinutes(5)); // Ca bắt đầu 5 phút trước
        testShift.setEndTime(LocalDateTime.now().plusHours(4));
        testStaffShift.setNote("Existing note from previous action."); // Thiết lập note có sẵn

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertTrue(result);
        assertEquals(StaffShift.Status.PRESENT, testStaffShift.getStatus());
        assertEquals(0, testStaffShift.getPenaltyPercent());
        assertNotNull(testStaffShift.getCheckIn());
        // Kiểm tra xem note mới có được nối vào note cũ không
        assertTrue(testStaffShift.getNote().contains("Existing note from previous action."));
        assertTrue(testStaffShift.getNote().contains("PRESENT"));
        verify(staffShiftRepository).save(testStaffShift);
    }

    @Test
    void testCase1_8_FindShiftIgnoresAbsentShift() {
        // Case 1.8: Tìm shift bỏ qua shift có status ABSENT (cover dòng 107)
        LocalDateTime now = LocalDateTime.now();

        // Tạo shift ABSENT (sẽ bị bỏ qua)
        Shift absentShift = new Shift();
        absentShift.setId(1);
        absentShift.setShiftName(Shift.ShiftType.SÁNG);
        absentShift.setStartTime(now.minusMinutes(10));
        absentShift.setEndTime(now.plusHours(4));

        StaffShift absentStaffShift = new StaffShift();
        absentStaffShift.setId(1);
        absentStaffShift.setStaff(testStaff);
        absentStaffShift.setShift(absentShift);
        absentStaffShift.setWorkDate(LocalDate.now());
        absentStaffShift.setStatus(StaffShift.Status.ABSENT); // Status ABSENT
        absentStaffShift.setCheckIn(null);
        absentStaffShift.setCheckOut(null);

        // Tạo shift hợp lệ (sẽ được chọn)
        Shift validShift = new Shift();
        validShift.setId(2);
        validShift.setShiftName(Shift.ShiftType.CHIỀU);
        validShift.setStartTime(now.minusMinutes(5));
        validShift.setEndTime(now.plusHours(4));

        StaffShift validStaffShift = new StaffShift();
        validStaffShift.setId(2);
        validStaffShift.setStaff(testStaff);
        validStaffShift.setShift(validShift);
        validStaffShift.setWorkDate(LocalDate.now());
        validStaffShift.setStatus(StaffShift.Status.SCHEDULED); // Status hợp lệ
        validStaffShift.setCheckIn(null);
        validStaffShift.setCheckOut(null);

        // Mock repository trả về cả 2 shifts (ABSENT và hợp lệ)
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(absentStaffShift, validStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertTrue(result);
        // Phải chọn validStaffShift (bỏ qua absentStaffShift)
        assertEquals(StaffShift.Status.PRESENT, validStaffShift.getStatus());
        assertEquals(0, validStaffShift.getPenaltyPercent());
        assertNotNull(validStaffShift.getCheckIn());
        assertTrue(validStaffShift.getNote().contains("PRESENT"));
        verify(staffShiftRepository).save(validStaffShift);
    }

    @Test
    void testCase1_9_FindShiftIgnoresAlreadyCheckedIn() {
        // Case 1.9: Tìm shift bỏ qua shift đã check-in (cover dòng 107 - branch còn
        // thiếu)
        LocalDateTime now = LocalDateTime.now();

        // Tạo shift đã check-in (sẽ bị bỏ qua)
        Shift checkedInShift = new Shift();
        checkedInShift.setId(1);
        checkedInShift.setShiftName(Shift.ShiftType.SÁNG);
        checkedInShift.setStartTime(now.minusMinutes(10));
        checkedInShift.setEndTime(now.plusHours(4));

        StaffShift checkedInStaffShift = new StaffShift();
        checkedInStaffShift.setId(1);
        checkedInStaffShift.setStaff(testStaff);
        checkedInStaffShift.setShift(checkedInShift);
        checkedInStaffShift.setWorkDate(LocalDate.now());
        checkedInStaffShift.setStatus(StaffShift.Status.SCHEDULED);
        checkedInStaffShift.setCheckIn(LocalDateTime.now().minusMinutes(5)); // Đã check-in
        checkedInStaffShift.setCheckOut(null);

        // Tạo shift hợp lệ (sẽ được chọn)
        Shift validShift = new Shift();
        validShift.setId(2);
        validShift.setShiftName(Shift.ShiftType.CHIỀU);
        validShift.setStartTime(now.minusMinutes(5));
        validShift.setEndTime(now.plusHours(4));

        StaffShift validStaffShift = new StaffShift();
        validStaffShift.setId(2);
        validStaffShift.setStaff(testStaff);
        validStaffShift.setShift(validShift);
        validStaffShift.setWorkDate(LocalDate.now());
        validStaffShift.setStatus(StaffShift.Status.SCHEDULED);
        validStaffShift.setCheckIn(null); // Chưa check-in
        validStaffShift.setCheckOut(null);

        // Mock repository trả về cả 2 shifts (đã check-in và hợp lệ)
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(checkedInStaffShift, validStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertTrue(result);
        // Phải chọn validStaffShift (bỏ qua checkedInStaffShift)
        assertEquals(StaffShift.Status.PRESENT, validStaffShift.getStatus());
        assertEquals(0, validStaffShift.getPenaltyPercent());
        assertNotNull(validStaffShift.getCheckIn());
        assertTrue(validStaffShift.getNote().contains("PRESENT"));
        verify(staffShiftRepository).save(validStaffShift);
    }

    @Test
    void testCase2_LoginLate10Minutes() {
        // Case 2: Muộn 10 phút (tha thứ)
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertTrue(result);
        assertEquals(StaffShift.Status.PRESENT, testStaffShift.getStatus());
        assertEquals(0, testStaffShift.getPenaltyPercent());
        assertTrue(testStaffShift.getNote().contains("tha thứ"));
        verify(staffShiftRepository).save(testStaffShift);
    }

    @Test
    void testCase3_LoginLate20Minutes() {
        log.info(" TEST CASE 3: Login muộn 20 phút");
        log.info(" Scenario: Nhân viên check-in muộn 20 phút so với giờ bắt đầu ca");

        // Case 3: Muộn 20 phút (phạt 5%) - Set shift to start 20 minutes ago
        testShift.setStartTime(LocalDateTime.now().minusMinutes(20));
        testShift.setEndTime(LocalDateTime.now().plusHours(4));

        log.info(" Shift start time: {} (20 phút trước)", testShift.getStartTime().format(VIETNAM_FORMAT));
        log.info(" Current time: {}", LocalDateTime.now().format(VIETNAM_FORMAT));
        log.info(" Late duration: 20 phút");
        log.info(" Expected penalty: 5%");

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        log.info("Login result: {}", result);
        log.info("Staff shift status: {} (Expected: LATE)", testStaffShift.getStatus());
        log.info("Penalty percent: {}% (Expected: 5%)", testStaffShift.getPenaltyPercent());
        log.info("Staff login time: {}", testStaffShift.getCheckIn().format(VIETNAM_FORMAT));
        log.info("Note: {}", testStaffShift.getNote());

        assertTrue(result);
        assertEquals(StaffShift.Status.LATE, testStaffShift.getStatus());
        assertEquals(5, testStaffShift.getPenaltyPercent());
        assertTrue(testStaffShift.getNote().contains("LATE"));
        verify(staffShiftRepository).save(testStaffShift);

        log.info("✅ TEST CASE 3 PASSED: Xử lý muộn giờ thành công");
    }

    @Test
    void testCase4_LoginLate35Minutes() {
        // Case 4: Muộn 35 phút (phạt 10%) - Set shift to start 35 minutes ago
        testShift.setStartTime(LocalDateTime.now().minusMinutes(35));
        testShift.setEndTime(LocalDateTime.now().plusHours(4));

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertTrue(result);
        assertEquals(StaffShift.Status.LATE, testStaffShift.getStatus());
        assertEquals(10, testStaffShift.getPenaltyPercent());
        verify(staffShiftRepository).save(testStaffShift);
    }

    @Test
    void testCase5_LoginLate50Minutes() {
        // Case 5: Muộn 50 phút (phạt 15%) - Set shift to start 50 minutes ago
        testShift.setStartTime(LocalDateTime.now().minusMinutes(50));
        testShift.setEndTime(LocalDateTime.now().plusHours(4));

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertTrue(result);
        assertEquals(StaffShift.Status.LATE, testStaffShift.getStatus());
        assertEquals(15, testStaffShift.getPenaltyPercent());
        verify(staffShiftRepository).save(testStaffShift);
    }

    @Test
    void testCase6_LoginTooEarly() {
        // Case 6: Login quá sớm (2 giờ trước) - Set shift to start 2 hours from now
        testShift.setStartTime(LocalDateTime.now().plusHours(2));
        testShift.setEndTime(LocalDateTime.now().plusHours(6));

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertFalse(result);
        verify(staffShiftRepository, never()).save(any());
    }

    @Test
    void testCase7_LoginTooLate() {
        // Case 7: Login quá muộn (2 giờ sau) - Set shift to start 2 hours ago
        testShift.setStartTime(LocalDateTime.now().minusHours(2));
        testShift.setEndTime(LocalDateTime.now().plusHours(2));

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList(testStaffShift));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        assertFalse(result);
        verify(staffShiftRepository, never()).save(any());
    }

    // ========== TEST LOGOUT CASES ==========

    @Test
    void testCase8_LogoutOnTime() {
        log.info(" TEST CASE 8: Logout đúng giờ");
        log.info(" Scenario: Nhân viên check-out đúng giờ kết thúc ca");

        // Case 8: Logout đúng giờ - Set shift end time to be in the past
        testShift.setEndTime(LocalDateTime.now().minusMinutes(10)); // 10 minutes ago
        testStaffShift.setCheckIn(LocalDateTime.now().minusHours(1)); // Checked in 1 hour ago
        testStaffShift.setStatus(StaffShift.Status.PRESENT);

        log.info("Shift end time: {} (10 phút trước)", testShift.getEndTime().format(VIETNAM_FORMAT));
        log.info("Current time: {}", LocalDateTime.now().format(VIETNAM_FORMAT));
        log.info("Staff check-in time: {}", testStaffShift.getCheckIn().format(VIETNAM_FORMAT));
        log.info("Current status: {}", testStaffShift.getStatus());

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findCurrentShiftByStaffId(1, LocalDate.now()))
                .thenReturn(Optional.of(testStaffShift));

        assertDoesNotThrow(() -> loginService.recordLogoutByEmail("test@example.com"));

        log.info("Final status: {} (Expected: COMPLETED)", testStaffShift.getStatus());
        log.info("Staff check-out time: {}", testStaffShift.getCheckOut().format(VIETNAM_FORMAT));
        log.info("Note: {}", testStaffShift.getNote());

        assertEquals(StaffShift.Status.COMPLETED, testStaffShift.getStatus());
        assertNotNull(testStaffShift.getCheckOut());
        assertTrue(testStaffShift.getNote().contains("COMPLETED"));
        verify(staffShiftRepository).save(testStaffShift);

        log.info("✅ TEST CASE 8 PASSED: Logout đúng giờ thành công");
    }

    @Test
    void testCase9_LogoutEarly() {
        log.info("TEST CASE 9: Logout sớm");
        log.info("Scenario: Nhân viên check-out trước giờ kết thúc ca");

        // Case 9: Logout sớm - Setup shift end time to be in the future (so current
        // time < end time)
        testShift.setEndTime(LocalDateTime.now().plusHours(2)); // Ca kết thúc sau 2 giờ
        testStaffShift.setCheckIn(LocalDateTime.now().minusHours(1)); // Check-in 1 giờ trước
        testStaffShift.setStatus(StaffShift.Status.PRESENT);

        log.info("Shift end time: {} (2 giờ sau)", testShift.getEndTime().format(VIETNAM_FORMAT));
        log.info("Current time: {}", LocalDateTime.now().format(VIETNAM_FORMAT));
        log.info("Staff check-in time: {}", testStaffShift.getCheckIn().format(VIETNAM_FORMAT));

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findCurrentShiftByStaffId(1, LocalDate.now()))
                .thenReturn(Optional.of(testStaffShift));

        assertDoesNotThrow(() -> loginService.recordLogoutByEmail("test@example.com"));

        log.info("Final status: {} (Expected: LEFT_EARLY)", testStaffShift.getStatus());
        log.info("Staff check-out time: {}", testStaffShift.getCheckOut().format(VIETNAM_FORMAT));
        log.info("Note: {}", testStaffShift.getNote());

        assertEquals(StaffShift.Status.LEFT_EARLY, testStaffShift.getStatus());
        assertNotNull(testStaffShift.getCheckOut());
        assertTrue(testStaffShift.getNote().contains("LEFT_EARLY"));
        verify(staffShiftRepository).save(testStaffShift);

        log.info("✅ TEST CASE 9 PASSED: Logout sớm thành công");
    }

    @Test
    void testCase10_LogoutWithoutLogin() {
        log.info(" TEST CASE 10: Logout chưa login");
        log.info(" Scenario: Nhân viên cố gắng check-out mà chưa check-in");
        log.info(" Expected: RuntimeException với message 'Nhân viên chưa check-in, không thể check-out'");

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findCurrentShiftByStaffId(1, LocalDate.now()))
                .thenReturn(Optional.of(testStaffShift));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loginService.recordLogoutByEmail("test@example.com");
        });

        log.info(" Exception thrown: {}", exception.getMessage());
        log.info("✅ Exception message matches expected: {}",
                exception.getMessage().equals("Nhân viên chưa check-in, không thể check-out"));

        assertEquals("Nhân viên chưa check-in, không thể check-out", exception.getMessage());
        verify(staffShiftRepository, never()).save(any());

        log.info("✅ TEST CASE 10 PASSED: Xử lý lỗi logout chưa login thành công");
    }

    @Test
    void testCase11_LogoutTwice() {
        // Case 11: Logout 2 lần
        testStaffShift.setCheckIn(LocalDateTime.now());
        testStaffShift.setCheckOut(LocalDateTime.now());
        testStaffShift.setStatus(StaffShift.Status.COMPLETED);

        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findCurrentShiftByStaffId(1, LocalDate.now()))
                .thenReturn(Optional.of(testStaffShift));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loginService.recordLogoutByEmail("test@example.com");
        });

        assertEquals("Nhân viên đã check-out rồi", exception.getMessage());
        verify(staffShiftRepository, never()).save(any());
    }

    // ========== TEST EDGE CASES ==========

    @Test
    void testNoShiftsScheduled() {
        log.info(" EDGE CASE: Không có ca nào được lên lịch");
        log.info(" Scenario: Nhân viên cố gắng check-in nhưng không có ca nào trong ngày");
        log.info(" Expected: Login thất bại (return false)");

        // Test khi không có ca nào được lên lịch
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findAllShiftsByStaffIdAndDate(1, LocalDate.now()))
                .thenReturn(Arrays.asList());

        log.info(" Scheduled shifts: 0");
        log.info(" Current date: {}", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        boolean result = loginService.recordLoginByEmail("test@example.com");

        log.info(" Login result: {} (Expected: false)", result);
        log.info("✅ No database save operation performed");

        assertFalse(result);
        verify(staffShiftRepository, never()).save(any());

        log.info("✅ EDGE CASE PASSED: Xử lý không có ca thành công");
    }

    @Test
    void testStaffNotFound() {
        log.info(" EDGE CASE: Không tìm thấy nhân viên");
        log.info(" Scenario: Email không tồn tại trong hệ thống");
        log.info(" Expected: RuntimeException");

        // Test khi không tìm thấy staff
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        log.info(" Searching for email: test@example.com");
        log.info(" Staff not found in database");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loginService.recordLoginByEmail("test@example.com");
        });

        log.info(" Exception thrown: {}", exception.getMessage());

        log.info("✅ EDGE CASE PASSED: Xử lý nhân viên không tồn tại thành công");
    }

    @Test
    void testLogoutStaffNotFound() {
        // Test logout khi không tìm thấy staff
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            loginService.recordLogoutByEmail("test@example.com");
        });
    }

    @Test
    void testLogoutNoCurrentShift() {
        // Test logout khi không có ca hiện tại
        when(loginRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testStaff));
        when(staffShiftRepository.findCurrentShiftByStaffId(1, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            loginService.recordLogoutByEmail("test@example.com");
        });
    }
}