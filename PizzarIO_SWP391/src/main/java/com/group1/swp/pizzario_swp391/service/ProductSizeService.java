package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeCreateDTO;
import com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeResponseDTO;
import com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeUpdateDTO;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.entity.Size;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.repository.ProductSizeRepository;
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
public class ProductSizeService{

    ProductSizeRepository productSizeRepository;
    ProductRepository productRepository;
    SizeRepository sizeRepository;

    public ProductSize getById(Long productSizeId) {
        return productSizeRepository.getOne(productSizeId);
    }

    public List<ProductSize> findByProductId(Long productId) {
        return productSizeRepository.findByProductId(productId);
    }

    public List<ProductSizeResponseDTO> getAllProductSizes() {
        return productSizeRepository.findAllWithRelations().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductSizeResponseDTO> searchProductSizes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProductSizes();
        }
        String q = query.toLowerCase();
        return getAllProductSizes().stream()
                .filter(ps -> ps.getProductName().toLowerCase().contains(q) ||
                        ps.getSizeName().toLowerCase().contains(q) ||
                        String.valueOf(ps.getBasePrice()).contains(q))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductSizeResponseDTO createProductSize(ProductSizeCreateDTO dto) {
        if (productSizeRepository.existsByProductIdAndSizeId(dto.getProductId(), dto.getSizeId())) {
            throw new RuntimeException("Cặp sản phẩm và kích thước đã tồn tại");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại ID: " + dto.getProductId()));

        Size size = sizeRepository.findById(dto.getSizeId())
                .orElseThrow(() -> new RuntimeException("Kích thước không tồn tại ID: " + dto.getSizeId()));

        ProductSize ps = new ProductSize();
        ps.setProduct(product);
        ps.setSize(size);
        ps.setBasePrice(dto.getBasePrice());
        ps.setFlashSalePrice(dto.getFlashSalePrice());
        ps.setFlashSaleStart(dto.getFlashSaleStart());
        ps.setFlashSaleEnd(dto.getFlashSaleEnd());

        ps = productSizeRepository.save(ps);
        return toResponseDTO(ps);
    }

    public ProductSizeUpdateDTO getProductSizeForUpdate(Long id) {
        ProductSize ps = productSizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi ID: " + id));

        return ProductSizeUpdateDTO.builder()
                .productId(ps.getProduct().getId())
                .sizeId(ps.getSize().getId())
                .basePrice(ps.getBasePrice())
                .flashSalePrice(ps.getFlashSalePrice())
                .flashSaleStart(ps.getFlashSaleStart())
                .flashSaleEnd(ps.getFlashSaleEnd())
                .build();
    }

    @Transactional
    public void updateProductSize(Long id, ProductSizeUpdateDTO dto) {
        ProductSize ps = productSizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi ID: " + id));

        if (productSizeRepository.existsByProductIdAndSizeIdAndIdNot(dto.getProductId(), dto.getSizeId(), id)) {
            throw new RuntimeException("Cặp sản phẩm và kích thước đã tồn tại");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại ID: " + dto.getProductId()));

        Size size = sizeRepository.findById(dto.getSizeId())
                .orElseThrow(() -> new RuntimeException("Kích thước không tồn tại ID: " + dto.getSizeId()));

        ps.setProduct(product);
        ps.setSize(size);
        ps.setBasePrice(dto.getBasePrice());
        ps.setFlashSalePrice(dto.getFlashSalePrice());
        ps.setFlashSaleStart(dto.getFlashSaleStart());
        ps.setFlashSaleEnd(dto.getFlashSaleEnd());

        productSizeRepository.save(ps);
    }

    @Transactional
    public void deleteProductSize(Long id) {
        if (!productSizeRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bản ghi ID: " + id);
        }
        productSizeRepository.deleteById(id);
    }

    private ProductSizeResponseDTO toResponseDTO(ProductSize ps) {
        return ProductSizeResponseDTO.builder()
                .id(ps.getId())
                .productId(ps.getProduct().getId())
                .productName(ps.getProduct().getName())
                .sizeId(ps.getSize().getId())
                .sizeName(ps.getSize().getSizeName())
                .basePrice(ps.getBasePrice())
                .basePriceFormatted(ps.getBasePriceFormatted())
                .flashSalePrice(ps.getFlashSalePrice())
                .flashSaleStart(ps.getFlashSaleStart())
                .flashSaleStartFormatted(ps.getFlashSaleStartFormatted())
                .flashSaleEnd(ps.getFlashSaleEnd())
                .flashSaleEndFormatted(ps.getFlashSaleEndFormatted())
                .onFlashSale(ps.isOnFlashSale())
                .currentPrice(ps.getCurrentPrice())
                .currentPriceFormatted(ps.getCurrentPriceFormatted())
                .build();
    }
}