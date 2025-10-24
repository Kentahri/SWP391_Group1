package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    SimpMessagingTemplate messagingTemplate;
    ReservationRepository reservationRepository;
    TableRepository tableRepository;
    ReservationMapper reservationMapper;
    ReservationSchedulerService reservationSchedulerService;
    WebSocketService webSocketService;

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
        if (dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(15))) {
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

        reservationSchedulerService.ScheduleAutoLockTable(reservation.getId(), reservation.getStartTime());
        reservationSchedulerService.scheduleNoShowCheck(reservation.getId(), reservation.getStartTime());
        return reservationMapper.toReservationDTO(saved);
    }

    /**
     * Throw exception nếu có lỗi business logic cho create
     */
    public void validateReservationBusinessLogicForCreate(ReservationCreateDTO dto) {
        DiningTable table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với ID: " + dto.getTableId()));

        if (dto.getCapacityExpected() > table.getCapacity()) {
            throw new RuntimeException("Vượt quá số người tối đa tại bàn, vui lòng chọn bàn phù hợp");
        }

        Reservation duplicateReservation = reservationRepository.findDuplicateReservation(dto.getTableId(), dto.getStartTime());
        if (duplicateReservation != null) {
            throw new RuntimeException("Bàn đã được đặt trong thời gian này");
        }

        boolean checkConflictReservation = checkConflictReservationForCreate(dto.getTableId(), dto.getStartTime());
        if (checkConflictReservation) {
            throw new RuntimeException("Thời gian đặt trước phải cách nhau ít nhất 90 phút.");
        }

        if ((table.getTableStatus().equals(DiningTable.TableStatus.OCCUPIED) || table.getTableStatus().equals(DiningTable.TableStatus.WAITING_PAYMENT))
                && dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(90))) {
            throw new RuntimeException("Bàn hiện đang có người ngồi, hãy đặt bàn cách thời điểm này ít nhất 90 phút");
        }
    }

    /**
     * Throw exception nếu có lỗi business logic cho update
     */
    public void validateReservationBusinessLogicForUpdate(Long reservationId, ReservationUpdateDTO dto) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));

        DiningTable table = reservation.getDiningTable();
        if (table == null) {
            throw new RuntimeException("Không tìm thấy thông tin bàn cho reservation này");
        }

        // Validate capacity
        if (dto.getCapacityExpected() > table.getCapacity()) {
            throw new RuntimeException("Vượt quá số người tối đa tại bàn, vui lòng chọn bàn phù hợp");
        }

        // Nếu thay đổi thời gian, kiểm tra conflict
        if (!dto.getStartTime().equals(reservation.getStartTime())) {
            // Check duplicate
            Reservation duplicateReservation = reservationRepository.findDuplicateReservation(table.getId(), dto.getStartTime());
            if (duplicateReservation != null && !duplicateReservation.getId().equals(reservationId)) {
                throw new RuntimeException("Bàn đã được đặt trong thời gian này");
            }

            boolean conflictReservations = checkConflictReservationForUpdate(reservationId, table.getId(), dto.getStartTime());
            if (conflictReservations) {
                throw new RuntimeException("Thời gian đặt trước phải cách nhau ít nhất 90 phút.");
            }

            if ((table.getTableStatus().equals(DiningTable.TableStatus.OCCUPIED) ||
                    table.getTableStatus().equals(DiningTable.TableStatus.WAITING_PAYMENT))
                    && dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(90))) {
                throw new RuntimeException("Bàn hiện đang có người ngồi, hãy đặt bàn cách thời điểm này ít nhất 90 phút");
            }
        }
    }

    private boolean checkConflictReservationForCreate(int tableId, LocalDateTime startTime) {
        boolean conflict = false;
        List<Reservation> conflictReservations = reservationRepository.findConflictReservation(
                tableId,
                startTime.minusMinutes(90),
                startTime.plusMinutes(90)
        );
        if(!conflictReservations.isEmpty()){
            conflict = true;
        }
        return conflict;
    }

    /**
     * Kiểm tra quanh 1 khoảng thời gian nhất định (± 90 phút)
     */
    private boolean checkConflictReservationForUpdate(Long reservationId, int tableId, LocalDateTime startTime) {
        boolean conflict = false;
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));
        List<Reservation> conflictReservations = reservationRepository.findConflictReservation(
                tableId,
                startTime.minusMinutes(90),
                startTime.plusMinutes(90)
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

        if (dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(15))) {
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
        reservationRepository.save(reservation);
        tableRepository.save(table);

        log.info("Đã hủy reservation {} cho bàn {}", reservationId, table.getId());
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
        table.setTableStatus(DiningTable.TableStatus.AVAILABLE);

        tableRepository.save(table);
        reservationRepository.save(reservation);
        reservationSchedulerService.cancelNoShowCheck(reservationId);

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
        DiningTable table = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"))
                .getDiningTable();
        if (table.getTableStatus() == DiningTable.TableStatus.AVAILABLE) {
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
                    "Tự động khóa bàn trước 15 phút khi đến giờ đặt (Reservation #" + reservationId + ")"
            );
            log.info("Đã tự động khóa bàn {} trước 15 phút khi đến giờ đặt (Reservation #{})", table.getId(), reservationId);
        } else {
            log.info("Bàn {} không ở trạng thái AVAILABLE, không thực hiện khóa tự động (Reservation #{})", table.getId(), reservationId);
        }


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

        if (reservation == null) {
            log.warn("Không tìm thấy reservation {}", reservationId);
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

        // Xử lý bàn
        DiningTable table = reservation.getDiningTable();
        DiningTable.TableStatus oldStatus = table.getTableStatus();

        // Kiểm tra xem trong 90p tới còn reservation CONFIRMED nào khác không
        List<Reservation> otherActiveReservations = reservationRepository.findConflictReservation(table.getId(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(90));

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
                    "Khách không đến sau 15 phút, bàn được mở lại (Reservation #" + reservationId + ")"
            );
        } else {
            webSocketService.broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
            webSocketService.broadcastTableStatusToCashier(
                    TableStatusMessage.MessageType.TABLE_RESERVED,
                    table.getId(),
                    oldStatus,
                    oldStatus,
                    "SYSTEM",
                    "Có bàn đặt trước trong 90p nữa, bàn đã được khóa lại (Reservation #" + reservationId + ")"
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

