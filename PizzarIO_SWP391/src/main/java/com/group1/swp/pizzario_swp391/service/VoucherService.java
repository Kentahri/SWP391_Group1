package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.data_analytics.VoucherStatsDTO;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.mapper.VoucherMapper;
import com.group1.swp.pizzario_swp391.repository.VoucherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherCreateDTO;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherService {
    VoucherRepository voucherRepository;
    VoucherMapper voucherMapper;

    public void createNewVoucher(VoucherCreateDTO voucherDTO) {
        // Validate type & value
        if (voucherDTO.getType() == null) {
            throw new IllegalArgumentException("Bạn phải chọn loại voucher.");
        }
        double value = voucherDTO.getValue();
        switch (voucherDTO.getType()) {
            case PERCENTAGE:
                if (value <= 0 || value >= 100) {
                    throw new IllegalArgumentException("Với voucher phần trăm, giá trị phải lớn hơn 0 và nhỏ hơn 100.");
                }
                break;
            case FIXED_AMOUNT:
                if (value <= 0) {
                    throw new IllegalArgumentException("Với voucher số tiền, giá trị phải lớn hơn 0.");
                }
                break;
        }
        // Validate validFrom, validTo
        if (voucherDTO.getValidFrom() == null || voucherDTO.getValidTo() == null) {
            throw new IllegalArgumentException("Bạn phải nhập đủ ngày bắt đầu và kết thúc");
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (voucherDTO.getValidFrom().isBefore(now) || voucherDTO.getValidTo().isBefore(now)) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được ở trong quá khứ.");
        }
        if (!voucherDTO.getValidTo().isAfter(voucherDTO.getValidFrom())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        // Đảm bảo timesUsed = 0 khi tạo mới
        voucher.setTimesUsed(0);
        voucherRepository.save(voucher);
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    public List<Voucher> getVouchersSort() {
        return voucherRepository.findAllVoucherOrderByValidFromAsc();
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    public void updateVoucher(Long id, VoucherDTO voucherDTO) {
        // Validate type & value
        if (voucherDTO.getType() == null) {
            throw new IllegalArgumentException("Bạn phải chọn loại voucher.");
        }
        double value = voucherDTO.getValue();
        switch (voucherDTO.getType()) {
            case PERCENTAGE:
                if (value <= 0 || value >= 100) {
                    throw new IllegalArgumentException("Với voucher phần trăm, giá trị phải lớn hơn 0 và nhỏ hơn 100.");
                }
                break;
            case FIXED_AMOUNT:
                if (value <= 0) {
                    throw new IllegalArgumentException("Với voucher số tiền, giá trị phải lớn hơn 0.");
                }
                break;
        }
        // Validate validFrom, validTo
        if (voucherDTO.getValidFrom() == null || voucherDTO.getValidTo() == null) {
            throw new IllegalArgumentException("Bạn phải nhập đủ ngày bắt đầu và kết thúc");
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (voucherDTO.getValidFrom().isBefore(now) || voucherDTO.getValidTo().isBefore(now)) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được ở trong quá khứ.");
        }
        if (!voucherDTO.getValidTo().isAfter(voucherDTO.getValidFrom())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucherMapper.updateVoucher(voucher, voucherDTO);
        voucherRepository.save(voucher);
    }

    public VoucherStatsDTO getVoucherAnalytics() {
        Integer totalVoucher = voucherRepository.findAll().size();
        Integer activeVoucher = voucherRepository.countByActiveTrue();
        Integer usedVoucher = voucherRepository.totalUsedVoucher();
        Double saveMoneyCustomer = voucherRepository.totalSavedAllOrders();

        return new VoucherStatsDTO(totalVoucher, activeVoucher, usedVoucher, saveMoneyCustomer);
    }

    public void toggleVoucherActive(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setActive(!voucher.isActive());
        voucherRepository.save(voucher);
    }

    public List<Voucher> searchVouchers(String keyword, String status) {
        List<Voucher> all = voucherRepository.findAllVoucherOrderByValidFromAsc();

        return all.stream()
                .filter(v -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String lower = keyword.toLowerCase();
                    return v.getCode().toLowerCase().contains(lower)
                            || (v.getDescription() != null && v.getDescription().toLowerCase().contains(lower));
                })
                .filter(v -> {
                    if (status == null || status.isBlank()) return true;
                    if (status.equals("active")) return v.isActive();
                    if (status.equals("inactive")) return !v.isActive();
                    return true;
                })
                .toList();
    }

}
