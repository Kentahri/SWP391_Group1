package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Sản phẩm có size rẻ nhất
    @Query("""
        select p from Product p
        where p.active = true
        and exists (
            select 1 from ProductSize ps
            where ps.product.id = p.id
        )
        order by (
            select min(ps.basePrice) from ProductSize ps
            where ps.product.id = p.id
        ) asc
        """)
    List<Product> findCheapestProducts(Pageable pageable);

    // Sản phẩm có size đắt nhất
    @Query("""
        select p from Product p
        where p.active = true
        and exists (
            select 1 from ProductSize ps
            where ps.product.id = p.id
        )
        order by (
            select max(ps.basePrice) from ProductSize ps
            where ps.product.id = p.id
        ) desc
        """)
    List<Product> findHighestPriceProducts(Pageable pageable);

    // Tìm sản phẩm theo tên category (giữ nguyên)
    @Query("""
        select distinct p from Product p
        where p.active = true
        and lower(p.category.name) like lower(concat('%', :categoryName, '%'))
        """)
    List<Product> findByCategoryNameContainingIgnoreCaseAndActiveTrue(String categoryName);

    // Sản phẩm đang có flash sale
    @Query("""
        select distinct p from Product p
        join p.productSizes ps
        where p.active = true
        and ps.flashSalePrice > 0
        and ps.flashSaleStart <= CURRENT_TIMESTAMP
        and ps.flashSaleEnd >= CURRENT_TIMESTAMP
        order by (
            select max(ps2.basePrice - ps2.flashSalePrice)
            from ProductSize ps2
            where ps2.product.id = p.id
            and ps2.flashSalePrice > 0
            and ps2.flashSaleStart <= CURRENT_TIMESTAMP
            and ps2.flashSaleEnd >= CURRENT_TIMESTAMP
        ) desc
        """)
    List<Product> findPromotionProducts();
}
