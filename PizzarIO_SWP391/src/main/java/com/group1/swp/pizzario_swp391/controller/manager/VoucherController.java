package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import com.group1.swp.pizzario_swp391.dto.voucher.VoucherCreateDTO;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String listVouchers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {

        // Gọi service filter
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        model.addAttribute("voucherForm", new VoucherDTO());
        model.addAttribute("vouchers", voucherService.searchVouchers(keyword, status));
        model.addAttribute("stats", voucherService.getVoucherAnalytics());

        // Giữ lại giá trị tìm kiếm để hiển thị lại trong form
        model.addAttribute("paramKeyword", keyword);
        model.addAttribute("paramStatus", status);

        return "admin_page/voucher/voucher-list";
    }
    @GetMapping("/vouchers/new")
    public String newVoucher(Model model) {
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        model.addAttribute("voucherForm", new VoucherCreateDTO());
        model.addAttribute("vouchers", voucherService.getVouchersSort());
        model.addAttribute("stats", voucherService.getVoucherAnalytics());
        model.addAttribute("openModal", "create");
        return "admin_page/voucher/voucher-list";
    }

    @GetMapping("/vouchers/edit/{id}")
    public String editVoucher(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        VoucherDTO voucherForm = VoucherDTO.builder()
                .id(id)
                .code(voucher.getCode())
                .type(voucher.getType())
                .value(voucher.getValue())
                .description(voucher.getDescription())
                .maxUses(voucher.getMaxUses())
                .timesUsed(voucher.getTimesUsed())
                .minOrderAmount(voucher.getMinOrderAmount())
                .validFrom(voucher.getValidFrom())
                .validTo(voucher.getValidTo())
                .active(voucher.isActive())
                .build();
        model.addAttribute("voucherForm", voucherForm);
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        model.addAttribute("vouchers", voucherService.getVouchersSort());
        model.addAttribute("stats", voucherService.getVoucherAnalytics());
        model.addAttribute("openModal", "edit");
        return "admin_page/voucher/voucher-list";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@Valid @ModelAttribute("voucherForm") VoucherCreateDTO voucherForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if(voucherForm.getType() == Voucher.VoucherType.PERCENTAGE && voucherForm.getValue() > 100) {
            bindingResult.rejectValue("value", "value.invalid", "Giá trị voucher phải nhỏ hơn hoặc bằng 100");
        }

        if(voucherForm.getValue() >= voucherForm.getMinOrderAmount()){
            bindingResult.rejectValue("value", "value.invalid","Số tiền giảm giá không được vượt quá giá trị đơn hàng tối thiểu");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            model.addAttribute("vouchers", voucherService.getVouchersSort());
            model.addAttribute("stats", voucherService.getVoucherAnalytics());
            model.addAttribute("openModal", "create");
            model.addAttribute("hasErrors", true);
            return "admin_page/voucher/voucher-list";
        }

        try {
            voucherService.createNewVoucher(voucherForm);
            return "redirect:/manager/vouchers";
        } catch (Exception ex) {
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            model.addAttribute("vouchers", voucherService.getVouchersSort());
            model.addAttribute("stats", voucherService.getVoucherAnalytics());
            model.addAttribute("openModal", "create");
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin_page/voucher/voucher-list";
        }
    }

    @PostMapping("/vouchers/toggle/{id}")
    public String toggleActive(@PathVariable Long id) {
        voucherService.toggleVoucherActive(id);
        return "redirect:/manager/vouchers";
    }
}
