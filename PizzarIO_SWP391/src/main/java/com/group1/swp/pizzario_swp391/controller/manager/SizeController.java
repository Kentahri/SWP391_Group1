package com.group1.swp.pizzario_swp391.controller.manager;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import com.group1.swp.pizzario_swp391.dto.size.SizeCreateDTO;
import com.group1.swp.pizzario_swp391.dto.size.SizeResponseDTO;
import com.group1.swp.pizzario_swp391.dto.size.SizeUpdateDTO;
import com.group1.swp.pizzario_swp391.service.SizeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SizeController {

    SizeService sizeService;

    @GetMapping("/sizes")
    public String list(Model model) {
        List<SizeResponseDTO> sizes = sizeService.getAllSizes();
        model.addAttribute("sizes", sizes);
        model.addAttribute("sizeForm", new SizeCreateDTO());
        return "admin_page/size/size-list";
    }

    @PostMapping("/sizes")
    public String searchSize(@RequestParam("query") String query, Model model) {
        List<SizeResponseDTO> sizes = sizeService.searchSizes(query);
        model.addAttribute("sizes", sizes);
        model.addAttribute("query", query);
        model.addAttribute("sizeForm", new SizeCreateDTO());
        return "admin_page/size/size-list";
    }

    @GetMapping("/sizes/new")
    public String newSize(Model model) {
        List<SizeResponseDTO> sizes = sizeService.getAllSizes();
        model.addAttribute("sizes", sizes);
        model.addAttribute("sizeForm", new SizeCreateDTO());
        model.addAttribute("openModal", "create");
        return "admin_page/size/size-list";
    }

    @GetMapping("/sizes/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        SizeUpdateDTO sizeUpdateDTO = sizeService.getSizeForUpdate(id);

        SizeCreateDTO sizeForm = SizeCreateDTO.builder()
                .id(id)
                .sizeName(sizeUpdateDTO.getSizeName())
                .build();

        List<SizeResponseDTO> sizes = sizeService.getAllSizes();
        model.addAttribute("sizes", sizes);
        model.addAttribute("sizeForm", sizeForm);
        model.addAttribute("openModal", "edit");
        return "admin_page/size/size-list";
    }

    @PostMapping("/sizes/save")
    public String save(@Valid @ModelAttribute("sizeForm") SizeCreateDTO sizeForm,
                       BindingResult bindingResult,
                       Model model) {

        if (bindingResult.hasErrors()) {
            List<SizeResponseDTO> sizes = sizeService.getAllSizes();
            model.addAttribute("sizes", sizes);
            model.addAttribute("openModal", sizeForm.getId() == null ? "create" : "edit");
            model.addAttribute("hasErrors", true);
            return "admin_page/size/size-list";
        }

        try {
            if (sizeForm.getId() == null) {
                sizeService.createSize(sizeForm);
            } else {
                SizeUpdateDTO updateDTO = SizeUpdateDTO.builder()
                        .sizeName(sizeForm.getSizeName())
                        .build();
                sizeService.updateSize(sizeForm.getId(), updateDTO);
            }
            return "redirect:/manager/sizes";
        } catch (Exception e) {
            List<SizeResponseDTO> sizes = sizeService.getAllSizes();
            model.addAttribute("sizes", sizes);
            model.addAttribute("openModal", sizeForm.getId() == null ? "create" : "edit");
            model.addAttribute("errorMessage", e.getMessage());
            return "admin_page/size/size-list";
        }
    }

    @PostMapping("/sizes/delete/{id}")
    public String delete(@PathVariable Long id) {
        sizeService.deleteSize(id);
        return "redirect:/manager/sizes";
    }

    @GetMapping("/sizes/search")
    @ResponseBody
    public List<SizeResponseDTO> searchSizes(@RequestParam String query) {
        List<SizeResponseDTO> allSizes = sizeService.getAllSizes();

        if (query == null || query.trim().isEmpty()) {
            return allSizes;
        }

        String queryLower = query.toLowerCase();
        return allSizes.stream()
                .filter(s -> s.getSizeName().toLowerCase().contains(queryLower))
                .toList();
    }
}