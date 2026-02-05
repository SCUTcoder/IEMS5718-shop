package com.iems5718.shop.repository;

import com.iems5718.shop.model.Category;
import com.iems5718.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByActiveTrue();
    
    List<Product> findByCategoryAndActiveTrue(Category category);
    
    List<Product> findByCategoryCatidAndActiveTrue(Long catid);
    
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);
}
