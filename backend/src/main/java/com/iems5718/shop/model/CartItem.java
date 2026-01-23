package com.iems5718.shop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    private Long productId;
    private String name;
    private Double price;
    private String imageUrl;
    private Integer quantity;
    
    public Double getTotalPrice() {
        return price * quantity;
    }
}
