package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.CategoryDTO;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/category")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryController {

    CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "category/category-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        return "category/category-create";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute CategoryDTO categoryDTO) {
        categoryService.createCategory(categoryDTO);
        return "redirect:/category";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .build();
        model.addAttribute("categoryDTO", categoryDTO);
        model.addAttribute("catId", category.getId());
        return "category/category-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @Valid @ModelAttribute CategoryDTO categoryDTO) {
        categoryService.updateCategory(id, categoryDTO);
        return "redirect:/category";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/category";
    }
}

