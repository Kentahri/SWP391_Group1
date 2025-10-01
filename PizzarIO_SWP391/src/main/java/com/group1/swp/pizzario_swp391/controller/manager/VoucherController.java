package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.service.VoucherService;

@Controller
@RequestMapping("/voucher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherController {

    VoucherService voucherService;

    @GetMapping
    public String voucher(Model model) {
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        model.addAttribute("voucherDTO", new VoucherDTO());
        model.addAttribute("vouchers", voucherService.getVouchersSort());
        return "admin_page/voucher/voucher";
    }

    @GetMapping("/edit/{id}")
    public String editVoucher(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        VoucherDTO voucherDTO = VoucherDTO.builder()
            .code(voucher.getCode())
            .type(voucher.getType())
            .value(voucher.getValue())
            .description(voucher.getDescription())
            .maxUses(voucher.getMaxUses())
            .timesUsed(voucher.getTimesUsed())
            .minOrderAmount(voucher.getMinOrderAmount())
            .validFrom(voucher.getValidFrom())
            .validTo(voucher.getValidTo())
            .isActive(voucher.isActive())
            .build();
        model.addAttribute("voucherDTO", voucherDTO);
        model.addAttribute("voucherId", id);
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        return "admin_page/voucher/voucher_edit";
    }

    @PostMapping("/add")
    public String createNewVoucher(@ModelAttribute VoucherDTO voucherDTO) {
        voucherService.createNewVoucher(voucherDTO);
        return "redirect:/voucher";
    }

    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return "redirect:/voucher";
    }

    @PostMapping("/update/{id}")
    public String updateVoucher(@PathVariable Long id, @ModelAttribute VoucherDTO voucherDTO) {
        voucherService.updateVoucher(id, voucherDTO);
        return "redirect:/voucher";
    }
}
