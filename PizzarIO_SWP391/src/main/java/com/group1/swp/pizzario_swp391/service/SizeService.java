package com.group1.swp.pizzario_swp391.service;


import com.group1.swp.pizzario_swp391.entity.Size;
import com.group1.swp.pizzario_swp391.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService{
    private final SizeRepository repo;

    public List<Size> getAll() {
        return repo.findAll();
    }

    public Size getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Size save(Size size) {
        return repo.save(size);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
