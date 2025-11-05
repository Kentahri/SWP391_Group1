package com.group1.swp.pizzario_swp391.repository;

import com.group1.swp.pizzario_swp391.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {

    boolean existsBySizeNameIgnoreCase(String sizeName);

    boolean existsBySizeNameIgnoreCaseAndIdNot(String sizeName, Long id);

    @Query("SELECT s FROM Size s LEFT JOIN FETCH s.productSizes")
    List<Size> findAllWithProductSizes();
}