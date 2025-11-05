package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.config.Setting;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationCreateDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationUpdateDTO;
import com.group1.swp.pizzario_swp391.dto.websocket.TableStatusMessage;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Reservation;
import com.group1.swp.pizzario_swp391.event.reservation.AutoLockTableEvent;
import com.group1.swp.pizzario_swp391.event.reservation.ReservationNoShowEvent;
import com.group1.swp.pizzario_swp391.mapper.ReservationMapper;
import com.group1.swp.pizzario_swp391.repository.ReservationRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    ReservationRepository reservationRepository;
    TableRepository tableRepository;
    ReservationMapper reservationMapper;
    ReservationSchedulerService reservationSchedulerService;
    WebSocketService webSocketService;
    Setting setting;
    PendingReservationTracker pendingReservationTracker;

    /**
     * Tạo reservation mới cho bàn
     */
    @Transactional
    public ReservationDTO createReservation(ReservationCreateDTO dto) {
        DiningTable table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với ID: " + dto.getTableId()));

        // Tạo reservation
        Reservation reservation = reservationMapper.toReservationEntity(dto);
        reservation.setDiningTable(table);
        Reservation saved = reservationRepository.save(reservation);

        DiningTable.TableStatus oldStatus = table.getTableStatus();
        // Kiểm tra nếu bàn được đặt trước trong vòng đúng thời gian đã quy định tới, thì đổi trạng thái bàn thành RESERVED
        if (dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(setting.getAutoLockReservationMinutes()))) {
            table.setTableStatus(DiningTable.TableStatus.RESERVED);
            tableRepository.save(table);

            // Broadcast WebSocket
            webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RESERVED,
                    table.getId(),
                    oldStatus,
                    table.getTableStatus(),
                    "CASHIER",
                    String.format("Bàn đã được đặt trước - Reservation #%d (%s - %s)",
                            saved.getId(), saved.getName(), saved.getPhone())
            );
        } else {
            tableRepository.save(table);
            webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
        }

        reservationSchedulerService.scheduleAutoLockTable(reservation.getId(), reservation.getStartTime());
        reservationSchedulerService.scheduleNoShowCheck(reservation.getId(), reservation.getStartTime());
        return reservationMapper.toReservationDTO(saved);
    }

    /**
     * Thu thập tất cả lỗi business logic cho create và throw một lần duy nhất
     */
    public void validateReservationBusinessLogicForCreate(ReservationCreateDTO dto) {
        Map<String, String> errors = new HashMap<>();

        DiningTable table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với ID: " + dto.getTableId()));

        if (dto.getCapacityExpected() > table.getCapacity()) {
            errors.put("capacityExpected", "Vượt quá số người tối đa tại bàn, vui lòng chọn bàn phù hợp");
        }

        Reservation duplicateReservation = reservationRepository.findDuplicateReservation(dto.getTableId(), dto.getStartTime());
        if (duplicateReservation != null) {
            errors.put("startTime", "Bàn đã được đặt trong thời gian này");
        }

        if (!errors.containsKey("startTime")) {
            boolean checkConflictReservation = checkConflictReservationForCreate(dto.getTableId(), dto.getStartTime());
            if (checkConflictReservation) {
                errors.put("startTime", "Thời gian đặt trước phải cách nhau ít nhất " + setting.getConflictReservationMinutes() + " phút.");
            }
        }

        // Kiểm tra nếu bàn đang bị khóa (LOCKED)
        if (table.getTableStatus().equals(DiningTable.TableStatus.LOCKED)) {
            errors.put("tableId", "Bàn đang bị khóa tạm thời, không thể đặt trước");
        }

        // Check bàn đang có người ngồi (không kiểm tra nếu đã có lỗi startTime hoặc tableId)
        if (!errors.containsKey("startTime") && !errors.containsKey("tableId")) {
            if ((table.getTableStatus().equals(DiningTable.TableStatus.OCCUPIED) || table.getTableStatus().equals(DiningTable.TableStatus.WAITING_PAYMENT))
                    && dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(setting.getConflictReservationMinutes()))) {
                errors.put("startTime", "Bàn hiện đang có người ngồi, hãy đặt bàn cách thời điểm này ít nhất " + setting.getConflictReservationMinutes() + " phút");
            }
        }

        // Throw nếu có lỗi
        if (!errors.isEmpty()) {
            throw new com.group1.swp.pizzario_swp391.exception.ValidationException(errors);
        }
    }

    /**
     * Thu thập tất cả lỗi business logic cho update và throw một lần duy nhất
     */
    public void validateReservationBusinessLogicForUpdate(Long reservationId, ReservationUpdateDTO dto) {
        Map<String, String> errors = new HashMap<>();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));

        DiningTable table = reservation.getDiningTable();
        if (table == null) {
            throw new RuntimeException("Không tìm thấy thông tin bàn cho reservation này");
        }

        // Validate capacity
        if (dto.getCapacityExpected() > table.getCapacity()) {
            errors.put("capacityExpected", "Vượt quá số người tối đa tại bàn, vui lòng chọn bàn phù hợp");
        }

        // Nếu thay đổi thời gian, kiểm tra conflict
        if (!dto.getStartTime().equals(reservation.getStartTime())) {
            // Check duplicate
            Reservation duplicateReservation = reservationRepository.findDuplicateReservation(table.getId(), dto.getStartTime());
            if (duplicateReservation != null && !duplicateReservation.getId().equals(reservationId)) {
                errors.put("startTime", "Bàn đã được đặt trong thời gian này");
            }

            // Check conflict reservation (không kiểm tra nếu đã có lỗi duplicate)
            if (!errors.containsKey("startTime")) {
                boolean conflictReservations = checkConflictReservationForUpdate(reservationId, table.getId(), dto.getStartTime());
                if (conflictReservations) {
                    errors.put("startTime", "Thời gian đặt trước phải cách nhau ít nhất " + setting.getConflictReservationMinutes() + " phút.");
                }
            }

            // Kiểm tra nếu bàn đang bị khóa (LOCKED)
            if (table.getTableStatus().equals(DiningTable.TableStatus.LOCKED)) {
                errors.put("tableId", "Bàn đang bị khóa tạm thời, không thể cập nhật đặt trước");
            }

            // Check bàn đang có người ngồi (không kiểm tra nếu đã có lỗi startTime hoặc tableId)
            if (!errors.containsKey("startTime") && !errors.containsKey("tableId")) {
                if ((table.getTableStatus().equals(DiningTable.TableStatus.OCCUPIED) ||
                        table.getTableStatus().equals(DiningTable.TableStatus.WAITING_PAYMENT))
                        && dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(setting.getConflictReservationMinutes()))) {
                    errors.put("startTime", "Bàn hiện đang có người ngồi, hãy đặt bàn cách thời điểm này ít nhất " + setting.getConflictReservationMinutes() + " phút");
                }
            }
        }

        // Throw nếu có lỗi
        if (!errors.isEmpty()) {
            throw new com.group1.swp.pizzario_swp391.exception.ValidationException(errors);
        }
    }

    private boolean checkConflictReservationForCreate(int tableId, LocalDateTime startTime) {
        boolean conflict = false;
        int conflictMinutes = setting.getConflictReservationMinutes();
        List<Reservation> conflictReservations = reservationRepository.findConflictReservation(
                tableId,
                startTime.minusMinutes(conflictMinutes),
                startTime.plusMinutes(conflictMinutes)
        );
        if(!conflictReservations.isEmpty()){
            conflict = true;
        }
        return conflict;
    }

    /**
     * Kiểm tra quanh 1 khoảng thời gian nhất định (± conflictMinutes phút)
     */
    private boolean checkConflictReservationForUpdate(Long reservationId, int tableId, LocalDateTime startTime) {
        boolean conflict = false;
        int conflictMinutes = setting.getConflictReservationMinutes();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));
        List<Reservation> conflictReservations = reservationRepository.findConflictReservation(
                tableId,
                startTime.minusMinutes(conflictMinutes),
                startTime.plusMinutes(conflictMinutes)
        );
        conflictReservations.remove(reservation);
        if(!conflictReservations.isEmpty()){
            conflict = true;
        }
        for (Reservation reservationTest : conflictReservations) {
            log.info("Conflict Reservation found: ID={}, StartTime={}", reservationTest.getId(), reservationTest.getStartTime());
        }
        return conflict;
    }

    /**
     * Cập nhật thông tin reservation
     */
    @Transactional
    public ReservationDTO updateReservation(Long reservationId, ReservationUpdateDTO dto) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));

        DiningTable table = reservation.getDiningTable();

        if (dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(setting.getAutoLockReservationMinutes()))) {
            table.setTableStatus(DiningTable.TableStatus.RESERVED);
        }else{
            table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
        }

        // Cập nhật thông tin
        reservation.setName(dto.getCustomerName());
        reservation.setPhone(dto.getCustomerPhone());
        reservation.setCapacityExpected(dto.getCapacityExpected());
        reservation.setStartTime(dto.getStartTime());
        reservation.setNote(dto.getNote());

        reservationSchedulerService.updateAutoLockTable(reservationId, reservation.getStartTime());
        reservationSchedulerService.updateNoShowCheck(reservationId, reservation.getStartTime());

        tableRepository.save(table);
        Reservation updated = reservationRepository.save(reservation);
        return reservationMapper.toReservationDTO(updated);
    }

    /**
     * Hủy reservation
     */
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));

        reservation.setStatus(Reservation.Status.CANCELED);
        DiningTable table = reservation.getDiningTable();
        DiningTable.TableStatus oldStatus = table.getTableStatus();

        if (table.getTableStatus() == DiningTable.TableStatus.RESERVED) {
            table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
            tableRepository.save(table);

            // Broadcast WebSocket
            webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RELEASED,
                    table.getId(),
                    oldStatus,
                    table.getTableStatus(),
                    "CASHIER",
                    String.format("Đã hủy đặt bàn #%d (%s - %s)",
                            reservationId, reservation.getName(), reservation.getPhone())
            );
        }

        reservationSchedulerService.cancelAutoLockTable(reservationId);
        reservationSchedulerService.cancelNoShowCheck(reservationId);

        // Cleanup: Remove khỏi pending list nếu đang chờ
        pendingReservationTracker.removePendingReservation(table.getId(), reservationId);

        reservationRepository.save(reservation);
        tableRepository.save(table);
    }

    /**
     * Mở bàn cho khách đã đặt trước
     */
    @Transactional
    public void openTableForGuestWithReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation!"));

        reservation.setStatus(Reservation.Status.ARRIVED);

        DiningTable table = reservation.getDiningTable();
        DiningTable.TableStatus oldStatus = table.getTableStatus();
        if (oldStatus.equals(DiningTable.TableStatus.OCCUPIED) || oldStatus.equals(DiningTable.TableStatus.WAITING_PAYMENT)) {
            throw new RuntimeException("Bàn đang có khách, không thể mở!");
        }

        // Bàn LOCKED cũng không thể mở cho khách đã đặt trước (vì đang bị khóa để merge)
        if (oldStatus.equals(DiningTable.TableStatus.LOCKED)) {
            throw new RuntimeException("Bàn đang bị khóa tạm thời, không thể mở!");
        }

        table.setTableStatus(DiningTable.TableStatus.AVAILABLE);

        tableRepository.save(table);
        reservationRepository.save(reservation);
        reservationSchedulerService.cancelNoShowCheck(reservationId);

        // Cleanup: Remove khỏi pending list nếu đang chờ
        pendingReservationTracker.removePendingReservation(table.getId(), reservationId);

        // Broadcast WebSocket
        webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
        webSocketService.broadcastTableStatusToCashier(
                TableStatusMessage.MessageType.TABLE_RELEASED,
                table.getId(),
                oldStatus,
                table.getTableStatus(),
                "CASHIER",
                String.format("Mở bàn #%d", table.getId())
        );

        log.info("Đã mở bàn {} cho reservation {} (khách đã đến)", table.getId(), reservationId);
    }

    @EventListener
    @Transactional
    public void onAutoLockTableEvent(AutoLockTableEvent event) {
        log.info("Received AutoLockTable event for reservation {}", event.getReservationId());
        closeTable(event.getReservationId());
    }

    /**
     * Khóa bàn tự động nếu đã đến thời gian quy định
     */
    private synchronized void closeTable(Long reservationId) {
        log.info("🔄 Scheduled: closeTable() is running...");

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));

        DiningTable table = reservation.getDiningTable();

        if (table.getTableStatus() == DiningTable.TableStatus.AVAILABLE) {
            lockTableForReservation(table, reservationId);
            log.info("✅ Đã tự động khóa bàn {} trước {} phút khi đến giờ đặt (Reservation #{})",
                    table.getId(), setting.getAutoLockReservationMinutes(), reservationId);
        } else if (table.getTableStatus() == DiningTable.TableStatus.OCCUPIED
                || table.getTableStatus() == DiningTable.TableStatus.WAITING_PAYMENT) {
            pendingReservationTracker.addPendingReservation(table.getId(), reservationId);

            log.warn("⏳ Bàn {} đang {} - Thêm reservation {} vào hàng chờ",
                    table.getId(), table.getTableStatus(), reservationId);

            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RESERVED,
                    table.getId(),
                    table.getTableStatus(),
                    table.getTableStatus(),
                    "SYSTEM",
                    String.format("⚠️ Bàn %d có reservation #%d đang chờ (Khách đặt: %s - %s). Vui lòng xử lý sớm!",
                            table.getId(), reservationId, reservation.getName(), reservation.getPhone())
            );
        } else if (table.getTableStatus() == DiningTable.TableStatus.LOCKED) {
            // Bàn LOCKED cũng vào hàng chờ, vì đang bị khóa tạm thời để merge
            pendingReservationTracker.addPendingReservation(table.getId(), reservationId);

            log.warn("⏳ Bàn {} đang LOCKED - Thêm reservation {} vào hàng chờ",
                    table.getId(), reservationId);

            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RESERVED,
                    table.getId(),
                    table.getTableStatus(),
                    table.getTableStatus(),
                    "SYSTEM",
                    String.format("⚠️ Bàn %d đang bị khóa, reservation #%d đang chờ (Khách đặt: %s - %s).",
                            table.getId(), reservationId, reservation.getName(), reservation.getPhone())
            );
        } else {
            log.info("Bàn {} ở trạng thái {}, không thực hiện khóa tự động (Reservation #{})",
                    table.getId(), table.getTableStatus(), reservationId);
        }
    }

    /**
     * Khóa bàn cho reservation (tái sử dụng được)
     */
    public void lockTableForReservation(DiningTable table, Long reservationId) {
        DiningTable.TableStatus oldStatus = table.getTableStatus();
        table.setTableStatus(DiningTable.TableStatus.RESERVED);
        tableRepository.save(table);

        webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
        webSocketService.broadcastTableStatusToCashier(
                TableStatusMessage.MessageType.TABLE_RESERVED,
                table.getId(),
                oldStatus,
                table.getTableStatus(),
                "SYSTEM",
                String.format("Tự động khóa bàn %d cho Reservation #%d", table.getId(), reservationId)
        );

        log.info("🔒 Đã khóa bàn {} cho reservation {}", table.getId(), reservationId);
    }

    /**
     * Event Listener - Lắng nghe event NO_SHOW từ scheduler
     */
    @EventListener
    @Transactional
    public void onReservationNoShowEvent(ReservationNoShowEvent event) {
        log.info("Received NO_SHOW event for reservation {}", event.getReservationId());
        processNoShowReservation(event.getReservationId());
    }

    /**
     * Xử lý NO_SHOW cho reservation cụ thể (được gọi bởi Event Listener)
     */
    private synchronized void processNoShowReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId);
        DiningTable table = reservation.getDiningTable();
        DiningTable.TableStatus oldStatus = table.getTableStatus();
        // Nếu bàn đang OCCUPIED hoặc WAITING_PAYMENT, không xử lý mở lại bàn, đánh đấu reservation thành đã hủy
        if(oldStatus == DiningTable.TableStatus.OCCUPIED || oldStatus == DiningTable.TableStatus.WAITING_PAYMENT){
            log.info("Bàn {} ở trạng thái OCCUPIED hoặc WAITING_PAYMENT, không xử lý mở lại bàn (Reservation #{})",
                    table.getId(), reservationId);
            reservation.setStatus(Reservation.Status.CANCELED);
            reservationRepository.save(reservation);
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_OCCUPIED,
                    table.getId(),
                    oldStatus,
                    oldStatus,
                    "SYSTEM",
                    "Bàn đang có khách, không thể thực thi tự động mở bàn (Reservation #" + reservationId + ")"
            );
            return;
        }

        // Chỉ xử lý nếu status vẫn là CONFIRMED (chưa ARRIVED, chưa CANCELED)
        if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
            log.info("Reservation {} đã thay đổi status ({}), không xử lý NO_SHOW",
                    reservationId, reservation.getStatus());
            return;
        }

        // Đánh dấu NO_SHOW
        reservation.setStatus(Reservation.Status.NO_SHOW);
        reservationRepository.save(reservation);

        log.info("Đã đánh dấu NO_SHOW cho reservation {}", reservationId);


        // Kiểm tra xem trong 90p tới còn reservation CONFIRMED nào khác không
        List<Reservation> otherActiveReservations = reservationRepository.findConflictReservation(table.getId(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(setting.getConflictReservationMinutes()));

        // Nếu không còn reservation nào và bàn vẫn RESERVED, mở lại
        if (otherActiveReservations.isEmpty()
                && oldStatus == DiningTable.TableStatus.RESERVED) {

            table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
            tableRepository.save(table);

            log.info("Đã mở lại bàn {} (không còn reservation active)", table.getId());

            // Broadcast
            webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RELEASED,
                    table.getId(),
                    oldStatus,
                    table.getTableStatus(),
                    "SYSTEM",
                    "Khách không đến sau " + setting.getNoShowWaitMinutes() + " phút, bàn " + table.getId()  + "được mở lại (Reservation #" + reservationId + ")"
            );
        } else {
            webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RESERVED,
                    table.getId(),
                    oldStatus,
                    oldStatus,
                    "SYSTEM",
                    "Có bàn đặt trước trong " + setting.getConflictReservationMinutes() + " nữa, bàn đã được khóa lại (Reservation #" + reservationId + ")"
            );
        }
    }

    /**
     * Lấy tất cả danh sách reservation
     */
    public List<ReservationDTO> getUpcomingReservations() {
        List<Reservation> reservationList = reservationRepository.findUpcomingReservations();
        return reservationList.stream().map(reservationMapper::toReservationDTO).collect(Collectors.toList());
    }

    /**
     * Lấy reservation của một bàn cụ thể
     */
    public List<ReservationDTO> getReservationsByTableId(Integer tableId) {
        List<Reservation> reservationList = reservationRepository.findAllReservationsByTableId(tableId);
        return reservationList.stream().map(reservationMapper::toReservationDTO).collect(Collectors.toList());
    }

    /**
     * Tìm kiếm reservation sắp tới theo keyword
     */
    public List<ReservationDTO> searchUpcomingReservations(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getUpcomingReservations();
        }
        List<Reservation> reservationList = reservationRepository.searchUpcomingReservations(LocalDateTime.now(), keyword.trim());
        return reservationList.stream().map(reservationMapper::toReservationDTO).collect(Collectors.toList());
    }

    /**
     * Lấy thông tin reservation theo ID để edit
     */
    public ReservationDTO findById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));
        return reservationMapper.toReservationDTO(reservation);
    }

}

