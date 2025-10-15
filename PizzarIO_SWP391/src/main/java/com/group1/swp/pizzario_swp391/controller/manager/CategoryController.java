package com.group1.swp.pizzario_swp391.controller.manager;

import java.util.List;

import com.group1.swp.pizzario_swp391.annotation.ManagerUrl;
import lombok.AccessLevel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.group1.swp.pizzario_swp391.dto.category.CategoryCreateDTO;
import com.group1.swp.pizzario_swp391.dto.category.CategoryDetailDTO;
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
        return "admin_page/category/category-list";
    }

    @GetMapping("/categories/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CategoryCreateDTO());
        return "admin_page/category/category-create";
    }

    @PostMapping("/categories/create")
    public String createCategory(@Valid @ModelAttribute CategoryCreateDTO categoryCreateDTO) {
        categoryService.createCategory(categoryCreateDTO);
        return "redirect:/manager/categories";
    }

    @GetMapping("/categories/detail/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        CategoryDetailDTO categoryDetail = categoryService.getCategoryById(id);
        model.addAttribute("category", categoryDetail);
        return "admin_page/category/category-detail";
    }

    @GetMapping("/categories/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        CategoryUpdateDTO categoryUpdateDTO = categoryService.getCategoryForUpdate(id);
        model.addAttribute("categoryDTO", categoryUpdateDTO);
        model.addAttribute("catId", id);
        return "admin_page/category/category-edit";
    }

    @PostMapping("/categories/edit/{id}")
    public String updateCategory(@PathVariable Long id, @Valid @ModelAttribute CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(id, categoryUpdateDTO);
        return "redirect:/manager/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/manager/categories";
    }
}
