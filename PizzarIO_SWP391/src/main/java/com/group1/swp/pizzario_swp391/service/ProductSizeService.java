package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSizeService{
    private final ProductSizeRepository repo;

    public ProductSize getById(Long productSizeId) {
        return repo.getOne(productSizeId);
    }

    public List<ProductSize> findByProductId(Long productId) {
        return repo.findByProductId(productId);
    }

    public List<ProductSize> getAll() {
        return repo.findAll();
    }

    public ProductSize save(ProductSize ps) {
        return repo.save(ps);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}