package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.mapper.VoucherMapper;
import com.group1.swp.pizzario_swp391.repository.VoucherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherService {
    VoucherRepository voucherRepository;
    VoucherMapper voucherMapper;

    public void createNewVoucher(VoucherDTO voucherDTO) {
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        voucherRepository.save(voucher);
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    public void updateVoucher(Long id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucherMapper.updateVoucher(voucher, voucherDTO);
        voucherRepository.save(voucher);
    }
}
