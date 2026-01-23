package com.iems5718.shop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "thumbnail_urls", length = 1000)
    private String thumbnailUrls;
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(nullable = false)
    private Boolean active = true;
}
