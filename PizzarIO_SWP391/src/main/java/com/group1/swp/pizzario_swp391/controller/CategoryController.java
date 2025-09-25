package com.group1.swp.pizzario_swp391.controller;

import com.group1.swp.pizzario_swp391.dto.CategoryResponse;
import com.group1.swp.pizzario_swp391.dto.CreateCategoryRequest;
import com.group1.swp.pizzario_swp391.dto.UpdateCategoryRequest;
import com.group1.swp.pizzario_swp391.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "category/category-list";
    }

    @GetMapping("/{id}")
    public String getCategory(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "category/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CreateCategoryRequest());
        return "category/category-create";
    }

    @PostMapping("/create")
    public String createCategory(@ModelAttribute CreateCategoryRequest category) {
        CategoryResponse created = categoryService.createCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "category/category-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute UpdateCategoryRequest category) {
        CategoryResponse updated = categoryService.updateCategory(id, category);
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}

