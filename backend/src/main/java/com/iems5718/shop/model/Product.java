package com.iems5718.shop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @Column(name = "pid")
    private Long pid;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "catid", nullable = false)
    @JsonIgnoreProperties("products")
    private Category category;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "thumbnail_urls", length = 1000)
    private String thumbnailUrls;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(nullable = false)
    private Boolean active = true;
}
