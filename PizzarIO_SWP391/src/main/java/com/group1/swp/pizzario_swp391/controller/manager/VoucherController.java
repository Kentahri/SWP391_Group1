package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.service.VoucherService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VoucherController {

    VoucherService voucherService;

    @GetMapping("/vouchers")
    public String voucher(Model model) {
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        model.addAttribute("voucherDTO", new VoucherDTO());
        model.addAttribute("vouchers", voucherService.getVouchersSort());
        return "admin_page/voucher/voucher";
    }

    @GetMapping("/vouchers/edit/{id}")
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

    @PostMapping("/vouchers/add")
    public String createNewVoucher(@ModelAttribute VoucherDTO voucherDTO, Model model, RedirectAttributes redirectAttributes) {
        try {
            voucherService.createNewVoucher(voucherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
            return "redirect:/manager/vouchers";
        } catch (Exception ex) {
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            model.addAttribute("vouchers", voucherService.getVouchersSort());
            model.addAttribute("voucherDTO", voucherDTO); // Để giữ lại dữ liệu form vừa nhập
            model.addAttribute("errorMessage", "Không thể tạo voucher vì: " + ex.getMessage());
            return "admin_page/voucher/voucher";
        }
    }

    @PostMapping("/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return "redirect:/manager/vouchers";
    }

    @PostMapping("/vouchers/update/{id}")
    public String updateVoucher(@PathVariable Long id, @ModelAttribute VoucherDTO voucherDTO, Model model, RedirectAttributes redirectAttributes) {
        try {
            voucherService.updateVoucher(id, voucherDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/manager/vouchers";
        } catch (Exception ex) {
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("voucherId", id);
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            model.addAttribute("errorMessage", "Không thể cập nhật voucher vì: " + ex.getMessage());
            return "admin_page/voucher/voucher_edit";
        }
    }
}
