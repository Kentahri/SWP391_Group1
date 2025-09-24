package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoucherService {
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    public Voucher createVoucher(Voucher voucher) {
        // Validate voucher code uniqueness
        if (voucherRepository.existsByCode(voucher.getCode())) {
            throw new IllegalArgumentException("Voucher code already exists: " + voucher.getCode());
        }
        
        // Validate date range
        if (voucher.getValidFrom().isAfter(voucher.getValidTo())) {
            throw new IllegalArgumentException("Valid from date cannot be after valid to date");
        }
        
        // Validate value
        if (voucher.getValue() <= 0) {
            throw new IllegalArgumentException("Voucher value must be greater than 0");
        }
        
        // Validate max uses
        if (voucher.getMaxUses() <= 0) {
            throw new IllegalArgumentException("Max uses must be greater than 0");
        }
        
        // Validate min order amount
        if (voucher.getMinOrderAmount() < 0) {
            throw new IllegalArgumentException("Min order amount cannot be negative");
        }
        
        return voucherRepository.save(voucher);
    }
    
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findByIsActiveTrue();
    }
    
    public List<Voucher> getCurrentValidVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findActiveVouchers(now);
    }
    
    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }
    
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }
    
    public Optional<Voucher> getActiveVoucherByCode(String code) {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findActiveVoucherByCode(code, now);
    }
    
    public Voucher updateVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }
    
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
    
    public void deactivateVoucher(Long id) {
        Optional<Voucher> voucherOpt = voucherRepository.findById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setActive(false);
            voucherRepository.save(voucher);
        }
    }
    
    public boolean isVoucherValid(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();
        return voucher.isActive() && 
               voucher.getValidFrom().isBefore(now) && 
               voucher.getValidTo().isAfter(now) &&
               voucher.getTimesUsed() < voucher.getMaxUses();
    }
    
    public boolean canUseVoucher(Voucher voucher, double orderAmount) {
        return isVoucherValid(voucher) && orderAmount >= voucher.getMinOrderAmount();
    }
}

