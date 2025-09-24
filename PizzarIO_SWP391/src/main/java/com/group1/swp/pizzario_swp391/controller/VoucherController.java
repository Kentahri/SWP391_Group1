package com.group1.swp.pizzario_swp391.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group1.swp.pizzario_swp391.dto.VoucherDTO;
import com.group1.swp.pizzario_swp391.entity.Voucher;
import com.group1.swp.pizzario_swp391.mapper.VoucherMapper;
import com.group1.swp.pizzario_swp391.service.VoucherService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VoucherMapper voucherMapper;
    
    /**
     * Helper method to add voucher data to model for edit form
     */
    private void addVoucherToModel(Long id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id).orElse(null);
        if (voucher != null) {
            model.addAttribute("voucher", voucher);
        }
    }
    
    @GetMapping
    public String listVouchers(Model model) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        model.addAttribute("vouchers", vouchers);
        return "vouchers/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("voucherDTO", new VoucherDTO());
        model.addAttribute("voucherTypes", Voucher.VoucherType.values());
        return "vouchers/create";
    }
    
    @PostMapping("/create")
    public String createVoucher(@Valid @ModelAttribute("voucherDTO") VoucherDTO voucherDTO, 
                               BindingResult bindingResult, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            return "vouchers/create";
        }
        
        try {
            // Convert DTO to Entity using mapper
            Voucher voucher = voucherMapper.toEntity(voucherDTO);
            
            voucherService.createVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/vouchers";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            return "vouchers/create";
        }
    }
    
    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable Long id, Model model) {
        Voucher voucher = voucherService.getVoucherById(id).orElse(null);
        if (voucher == null) {
            return "redirect:/vouchers";
        }
        model.addAttribute("voucher", voucher);
        return "vouchers/view";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Voucher voucher = voucherService.getVoucherById(id).orElse(null);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher not found with ID: " + id);
                return "redirect:/vouchers";
            }
            
            // Convert Entity to DTO using mapper
            VoucherDTO voucherDTO = voucherMapper.toDTO(voucher);
            
            model.addAttribute("voucher", voucher); // Add original voucher for ID access
            model.addAttribute("voucherDTO", voucherDTO);
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            return "vouchers/edit";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading voucher: " + e.getMessage());
            return "redirect:/vouchers";
        }
    }
    
    @PostMapping("/{id}/edit")
    public String updateVoucher(@PathVariable Long id, 
                               @Valid @ModelAttribute("voucherDTO") VoucherDTO voucherDTO, 
                               BindingResult bindingResult, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            addVoucherToModel(id, model);
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            return "vouchers/edit";
        }
        
        try {
            Voucher existingVoucher = voucherService.getVoucherById(id).orElse(null);
            if (existingVoucher == null) {
                return "redirect:/vouchers";
            }
            
            // Update existing voucher using mapper
            voucherMapper.updateEntity(existingVoucher, voucherDTO);
            
            voucherService.updateVoucher(existingVoucher);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher updated successfully!");
            return "redirect:/vouchers";
            
        } catch (IllegalArgumentException e) {
            addVoucherToModel(id, model);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("voucherTypes", Voucher.VoucherType.values());
            return "vouchers/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        voucherService.deleteVoucher(id);
        redirectAttributes.addFlashAttribute("successMessage", "Voucher deleted successfully!");
        return "redirect:/vouchers";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        voucherService.deactivateVoucher(id);
        redirectAttributes.addFlashAttribute("successMessage", "Voucher deactivated successfully!");
        return "redirect:/vouchers";
    }

    

}

