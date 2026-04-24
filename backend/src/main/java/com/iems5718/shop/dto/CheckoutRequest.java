package com.iems5718.shop.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequest {
    private List<CartItemDto> items;
    
    @Data
    public static class CartItemDto {
        private Long pid;
        private Integer quantity;
    }
}
