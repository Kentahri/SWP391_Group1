package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.product.ProductCreateDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductResponseDTO;
import com.group1.swp.pizzario_swp391.dto.product.ProductUpdateDTO;
import com.group1.swp.pizzario_swp391.dto.websocket.ProductStatusMessage;
import com.group1.swp.pizzario_swp391.entity.Category;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.entity.Size;
import com.group1.swp.pizzario_swp391.mapper.ProductResponseMapper;
import com.group1.swp.pizzario_swp391.repository.CategoryRepository;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.repository.SizeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductService {
    ProductRepository productRepository;
    ProductResponseMapper productMapper;
    CategoryRepository categoryRepository;
    SizeRepository sizeRepository;
    ProductSizeService productSizeService;

    WebSocketService webSocketService;

    static final String PRODUCT_NOT_FOUND = "Product not found";
    static final String CATEGORY_NOT_FOUND = "Category not found";

    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getActiveProducts() {
        List<Product> products = productRepository.findByActiveTrue();
        return products.stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getActiveProductsWithActiveCategory() {
        List<Product> products = productRepository.findByActiveTrueAndCategoryActiveTrue();
        return products.stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        return productMapper.toResponseDTO(product);
    }

    public ProductUpdateDTO getProductForUpdate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        return ProductUpdateDTO.builder()
                .name(product.getName())
                .description(product.getDescription())
                .imageURL(product.getImageURL())
                .currentImageURL(product.getImageURL())
//                .basePrice(product.getBasePrice())
//                .flashSalePrice(product.getFlashSalePrice())
//                .flashSaleStart(product.getFlashSaleStart())
//                .flashSaleEnd(product.getFlashSaleEnd())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .active(product.isActive())
                .build();
    }

    @Transactional
    public void createProduct(ProductCreateDTO createDTO) {
        // Kiểm tra tên sản phẩm đã tồn tại chưa
        if (productRepository.existsByNameIgnoreCase(createDTO.getName())) {
            throw new RuntimeException("Tên sản phẩm '" + createDTO.getName() + "' đã tồn tại");
        }
        
        Product product = productMapper.toEntity(createDTO);
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        // Set category
        Category category = categoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException(CATEGORY_NOT_FOUND + " with ID: " + createDTO.getCategoryId()));
        product.setCategory(category);

        // Lưu product trước để có ID
        product = productRepository.save(product);

        // Nếu là Combo và có giá combo -> tạo ProductSize mặc định với size "Medium"
        boolean isCombo = category.getName() != null && category.getName().toLowerCase().contains("combo");
        if (isCombo) {
            Double comboPrice = createDTO.getComboPrice();
            if (comboPrice == null || comboPrice < 0) {
                throw new RuntimeException("Giá combo không hợp lệ");
            }
            Size defaultSize = sizeRepository.findAll().stream()
                    .filter(s -> s.getSizeName() != null && s.getSizeName().equalsIgnoreCase("Default"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy size mặc định 'Default'"));

            productSizeService.createProductSize(
                    com.group1.swp.pizzario_swp391.dto.productsize.ProductSizeCreateDTO.builder()
                            .productId(product.getId())
                            .sizeId(defaultSize.getId())
                            .basePrice(comboPrice)
                            .build()
            );
        }

        // Broadcast sau khi tạo đầy đủ
        webSocketService.broadcastProductChange(
                ProductStatusMessage.MessageType.PRODUCT_CREATED,
                product,
                "Manager"
        );
    }

    public void updateProduct(Long id, ProductUpdateDTO updateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));

        // Kiểm tra tên sản phẩm đã tồn tại chưa (trừ chính nó)
        if (productRepository.existsByNameIgnoreCaseAndIdNot(updateDTO.getName(), id)) {
            throw new RuntimeException("Tên sản phẩm '" + updateDTO.getName() + "' đã tồn tại");
        }

        productMapper.updateEntity(product, updateDTO);
        product.setUpdatedAt(LocalDateTime.now());

        // Update category if changed
        if (updateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDTO.getCategoryId())
                    .orElseThrow(
                            () -> new RuntimeException(CATEGORY_NOT_FOUND + " with ID: " + updateDTO.getCategoryId()));
            product.setCategory(category);
        }

        productRepository.save(product);

        webSocketService.broadcastProductChange(
                ProductStatusMessage.MessageType.PRODUCT_UPDATED,
                product,
                "Manager"
        );

    }
    //=============
    /**
     * Cập nhật trạng thái active của sản phẩm
     * @param id ID của sản phẩm
     * @param active Trạng thái active mới
     * @param updatedBy Người cập nhật (Manager hoặc Kitchen)
     */
    public void updateProductActive(Long id, Boolean active, String updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
        product.setActive(active != null && active);
        product.setUpdatedAt(java.time.LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Broadcast WebSocket message to all guests và kitchen
        webSocketService.broadcastProductChange(
                ProductStatusMessage.MessageType.PRODUCT_TOGGLED,
                savedProduct,
                updatedBy != null ? updatedBy : "System"
        );
    }

    /**
     * Overload method để backward compatibility
     * Mặc định updatedBy = "System"
     */
    public void updateProductActive(Long id, Boolean active) {
        updateProductActive(id, active, "System");
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException(PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    // Cập nhật method toggleProductActive()
    /**
     * Toggle trạng thái active của sản phẩm
     * @param id ID của sản phẩm
     * @param updatedBy Người cập nhật (Manager hoặc Kitchen)
     */
    public void toggleProductActive(Long id, String updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));

        boolean newStatus = !product.isActive();

        product.setActive(newStatus);
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        // Broadcast WebSocket message to all guests và kitchen
        webSocketService.broadcastProductChange(
                ProductStatusMessage.MessageType.PRODUCT_TOGGLED,
                savedProduct,
                updatedBy != null ? updatedBy : "Manager"
        );
    }

    /**
     * Overload method để backward compatibility
     * Mặc định updatedBy = "Manager"
     */
    public void toggleProductActive(Long id) {
        toggleProductActive(id, "Manager");
    }

    public List<ProductResponseDTO> searchProducts(String query) {

        List<ProductResponseDTO> products;

        if (query != null && !query.trim().isEmpty()) {
            String queryLower = query.toLowerCase().trim();

            products = this.getAllProducts().stream()
                    .filter(product -> product.getName().toLowerCase().contains(queryLower) ||
                            (product.getDescription() != null
                                    && product.getDescription().toLowerCase().contains(queryLower))
                            ||
                            product.getCategoryName().toLowerCase().contains(queryLower))
                    .collect(Collectors.toList());
        } else {
            products = this.getAllProducts();
        }

        return products;
    }
}