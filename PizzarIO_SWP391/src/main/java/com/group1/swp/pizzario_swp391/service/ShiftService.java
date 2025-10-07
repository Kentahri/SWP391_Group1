package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.ShiftDTO;
import com.group1.swp.pizzario_swp391.entity.Shift;
import com.group1.swp.pizzario_swp391.mapper.ShiftMapper;
import com.group1.swp.pizzario_swp391.repository.ShiftRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    private final StaffShiftRepository staffShiftRepo;

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
        staffShiftRepo.deleteByShift_Id(id);
        // 2) Xóa cha
        shiftRepository.deleteById(id);
    }
}
