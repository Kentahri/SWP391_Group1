package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.List;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import lombok.AccessLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.group1.swp.pizzario_swp391.dto.category.CategoryCreateDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryResponseDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryUpdateDTO;
import com.group1.swp.pizzario_swp391.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@ManagerUrl
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryController {

    CategoryService categoryService;

    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("categoryForm", new CategoryCreateDTO());
        return "admin_page/category/category-list";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("categoryForm", new CategoryCreateDTO());
        model.addAttribute("openModal", "create");
        return "admin_page/category/category-list";
    }

    @GetMapping("/categories/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        CategoryUpdateDTO categoryUpdateDTO = categoryService.getCategoryForUpdate(id);

        // Convert to CategoryCreateDTO for unified form
        CategoryCreateDTO categoryForm = CategoryCreateDTO.builder()
                .id(id)
                .name(categoryUpdateDTO.getName())
                .description(categoryUpdateDTO.getDescription())
                .active(categoryUpdateDTO.isActive())
                .build();

        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("categoryForm", categoryForm);
        model.addAttribute("openModal", "edit");
        return "admin_page/category/category-list";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("categoryForm") CategoryCreateDTO categoryForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("openModal", categoryForm.getId() == null ? "create" : "edit");
            model.addAttribute("hasErrors", true);
            return "admin_page/category/category-list";
        }

        try {
            if (categoryForm.getId() == null) {
                // Create new category
                categoryService.createCategory(categoryForm);
            } else {
                // Update existing category
                CategoryUpdateDTO updateDTO = CategoryUpdateDTO.builder()
                        .name(categoryForm.getName())
                        .description(categoryForm.getDescription())
                        .active(categoryForm.isActive())
                        .build();
                categoryService.updateCategory(categoryForm.getId(), updateDTO);
            }
            return "redirect:/manager/categories";
        } catch (Exception e) {
            List<CategoryResponseDTO> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("openModal", categoryForm.getId() == null ? "create" : "edit");
            model.addAttribute("errorMessage", e.getMessage());
            return "admin_page/category/category-list";
        }
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/manager/categories";
    }

    @PostMapping("/categories/toggle/{id}")
    public String toggleActive(@PathVariable Long id) {
        categoryService.toggleCategoryActive(id);
        return "redirect:/manager/categories";
    }
}
