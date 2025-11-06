package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.size.SizeCreateDTO;
import com.group1.swp.pizzario_swp391.dto.size.SizeResponseDTO;
import com.group1.swp.pizzario_swp391.dto.size.SizeUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Size;
import com.group1.swp.pizzario_swp391.repository.SizeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SizeService{

    SizeRepository sizeRepository;

    public List<SizeResponseDTO> getAllSizes() {
        return sizeRepository.findAllWithProductSizes().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SizeResponseDTO> searchSizes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllSizes();
        }
        String q = query.toLowerCase();
        return getAllSizes().stream()
                .filter(s -> s.getSizeName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    @Transactional
    public SizeResponseDTO createSize(SizeCreateDTO dto) {
        if (sizeRepository.existsBySizeNameIgnoreCase(dto.getSizeName())) {
            throw new RuntimeException("Tên kích thước '" + dto.getSizeName() + "' đã tồn tại");
        }

        Size size = new Size(dto.getSizeName());
        size = sizeRepository.save(size);
        return toResponseDTO(size);
    }

    public SizeUpdateDTO getSizeForUpdate(Long id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước ID: " + id));

        return SizeUpdateDTO.builder()
                .sizeName(size.getSizeName())
                .build();
    }

    @Transactional
    public void updateSize(Long id, SizeUpdateDTO dto) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước ID: " + id));

        if (sizeRepository.existsBySizeNameIgnoreCaseAndIdNot(dto.getSizeName(), id)) {
            throw new RuntimeException("Tên kích thước '" + dto.getSizeName() + "' đã tồn tại");
        }

        size.setSizeName(dto.getSizeName());
        sizeRepository.save(size);
    }

    @Transactional
    public void deleteSize(Long id) {
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước ID: " + id));

        if (!size.getProductSizes().isEmpty()) {
            throw new RuntimeException("Không thể xóa: Kích thước đang được dùng trong sản phẩm");
        }

        sizeRepository.delete(size);
    }

    public List<SizeResponseDTO> getAllSizesForSelect() {
        return sizeRepository.findAll().stream()
                .map(s -> SizeResponseDTO.builder()
                        .id(s.getId())
                        .sizeName(s.getSizeName())
                        .productSizes(s.getProductSizes())
                        .build())
                .collect(Collectors.toList());
    }

    private SizeResponseDTO toResponseDTO(Size size) {
        return SizeResponseDTO.builder()
                .id(size.getId())
                .sizeName(size.getSizeName())
                .productSizes(size.getProductSizes())
                .build();
    }

    public SizeResponseDTO getSizeById(Long sizeId) {
        Size size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new RuntimeException("Size not found"));
        return SizeResponseDTO.builder()
                .id(size.getId())
                .sizeName(size.getSizeName())
                .productSizes(size.getProductSizes())
                .build();
    }
}