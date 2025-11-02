package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{

    //    @Query("select p from Product p " +
//            "where p.active = true " +
//            "order by p.basePrice asc")
//    List<Product> findCheapestProducts(Pageable pageable);
//
//    @Query("select p from Product p " +
//            "where p.active = true " +
//            "order by p.basePrice desc")
//    List<Product> findHighestPriceProducts(Pageable pageable);
//
//    @Query("select p from Product p " +
//            "where p.active = true " +
//            "and lower(p.category.name) like lower(concat('%', :categoryName, '%'))")
//    List<Product> findByCategoryNameContainingIgnoreCaseAndActiveTrue(String categoryName);
//
//    @Query("select p from Product p " +
//            "where p.active = true " +
//            "and p.flashSalePrice > 0 " +
//            "and p.flashSaleStart <= CURRENT_TIMESTAMP " +
//            "and p.flashSaleEnd >= CURRENT_TIMESTAMP " +
//            "order by (p.basePrice - p.flashSalePrice) desc " +
//            "limit 5")
//    List<Product> findPromotionProducts();
// Sản phẩm có size rẻ nhất
    @Query("""
            select distinct p from Product p
            join p.productSizes ps
            where p.active = true
            order by ps.basePrice asc
            """)
    List<Product> findCheapestProducts(Pageable pageable);

    // Sản phẩm có size đắt nhất
    @Query("""
            select distinct p from Product p
            join p.productSizes ps
            where p.active = true
            order by ps.basePrice desc
            """)
    List<Product> findHighestPriceProducts(Pageable pageable);

    // Tìm sản phẩm theo tên category
    @Query("""
            select distinct p from Product p
            where p.active = true
            and lower(p.category.name) like lower(concat('%', :categoryName, '%'))
            """)
    List<Product> findByCategoryNameContainingIgnoreCaseAndActiveTrue(String categoryName);

    // Sản phẩm đang có flash sale (theo ProductSize)
    @Query("""
            select distinct p from Product p
            join p.productSizes ps
            where p.active = true
            and ps.flashSalePrice > 0
            and ps.flashSaleStart <= CURRENT_TIMESTAMP
            and ps.flashSaleEnd >= CURRENT_TIMESTAMP
            order by (ps.basePrice - ps.flashSalePrice) desc
            """)
    List<Product> findPromotionProducts();
}
