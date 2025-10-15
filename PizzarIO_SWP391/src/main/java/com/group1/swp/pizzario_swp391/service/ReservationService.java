package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.group1.swp.pizzario_swp391.mapper.ReservationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.reservation.ReservationCreateDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationDTO;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.entity.Reservation;
import com.group1.swp.pizzario_swp391.repository.ReservationRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReservationService {

    ReservationRepository reservationRepository;
    TableRepository tableRepository;
    ReservationMapper reservationMapper;

    /**
     * Tạo reservation mới cho bàn
     */
    @Transactional
    public ReservationDTO createReservation(ReservationCreateDTO dto) {
        // Validate table exists
        DiningTable table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với ID: " + dto.getTableId()));

        // Validate capacity cho 1 bàn
        if (dto.getCapacityExpected() > table.getCapacity()) {
            throw new RuntimeException("Vượt quá số người tối đa tại bàn, vui lòng chọn bàn phù hợp");
        }

        // Check for conflicting reservations
        Reservation duplicateReservation = reservationRepository.findDuplicateReservation(dto.getTableId(), dto.getStartTime());
        if (duplicateReservation != null) {
            throw new RuntimeException("Bàn đã được đặt trong thời gian này");
        }

        // Check for business rule reservation
        boolean checkConflictReservation = checkConflictReservation(dto.getTableId(), dto.getStartTime());
        if(checkConflictReservation) {
            throw new RuntimeException("Thời gian đặt trước phải cách nhau ít nhất 90 phút.");
        }

        if((table.getTableStatus().equals(DiningTable.TableStatus.OCCUPIED) || table.getTableStatus().equals(DiningTable.TableStatus.WAITING_PAYMENT))
        && dto.getStartTime().isBefore(LocalDateTime.now().plusMinutes(90))) {
            throw new RuntimeException("Bàn hiện đang có người ngồi, hãy đặt bàn cách thời điểm này ít nhất 90 phút");
        }

        // Tạo reservation
        Reservation reservation = reservationMapper.toReservationEntity(dto);
        reservation.setDiningTable(table);
        Reservation saved = reservationRepository.save(reservation);

        // Cập nhật trạng thái của bàn thành reserved
        table.setTableStatus(DiningTable.TableStatus.RESERVED);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);

        return reservationMapper.toReservationDTO(saved);
    }

    /**
     * Kiểm tra quanh 1 khoảng thời gian nhất định
     */
    public boolean checkConflictReservation(int tableId, LocalDateTime startTime) {
        boolean conflict = false;
        List<Reservation> brReservation = reservationRepository.findConflictReservation(tableId, startTime.minusMinutes(90), startTime.plusMinutes(90));
        if (!brReservation.isEmpty()) {
            conflict = true;
        }
        return conflict;
    }

    /**
     * Lấy danh sách reservation sắp tới
     */
    public List<ReservationDTO> getUpcomingReservations() {
        List<Reservation> reservationList = reservationRepository.findUpcomingReservations(LocalDateTime.now());
        return reservationList.stream().map(reservationMapper::toReservationDTO).collect(Collectors.toList());
    }

    /**
     * Lấy reservation của một bàn cụ thể
     */
    public List<ReservationDTO> getReservationsByTableId(Integer tableId) {
        List<Reservation> reservationList = reservationRepository.findActiveReservationsByTableId(tableId);
        return reservationList.stream().map(reservationMapper::toReservationDTO).collect(Collectors.toList());
    }

    /**
     * Hủy reservation
     */
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));

        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);

        DiningTable table = reservation.getDiningTable();
        List<Reservation> activeReservations = reservationRepository.findActiveReservationsByTableId(table.getId());

        if (activeReservations.isEmpty() && table.getTableStatus() == DiningTable.TableStatus.RESERVED) {
            table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
    }
}

