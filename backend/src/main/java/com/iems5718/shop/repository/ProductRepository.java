package com.iems5718.shop.repository;

import com.iems5718.shop.model.Category;
import com.iems5718.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByActiveTrueOrderByWeightDescPidAsc();

    List<Product> findByCategoryAndActiveTrueOrderByWeightDescPidAsc(Category category);

    List<Product> findByCategoryCatidAndActiveTrueOrderByWeightDescPidAsc(Long catid);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrueOrderByWeightDescPidAsc(String keyword);
}
