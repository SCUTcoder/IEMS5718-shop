package com.iems5718.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import com.iems5718.shop.repository.OrderItemRepository;
import com.iems5718.shop.repository.OrderRepository;
import com.iems5718.shop.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void createOrderRejectsQuantityAboveLimitBeforeProductLookup() {
        OrderService service = new OrderService(orderRepository, orderItemRepository, productRepository);
        OrderService.CartItemSummary item = new OrderService.CartItemSummary();
        item.pid = 1L;
        item.quantity = OrderService.MAX_ITEM_QUANTITY + 1;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder("user@example.com", List.of(item)));

        assertEquals("Quantity must not exceed " + OrderService.MAX_ITEM_QUANTITY, ex.getMessage());
        verifyNoInteractions(productRepository, orderRepository, orderItemRepository);
    }

    @Test
    void createOrderRejectsMissingProductId() {
        OrderService service = new OrderService(orderRepository, orderItemRepository, productRepository);
        OrderService.CartItemSummary item = new OrderService.CartItemSummary();
        item.quantity = 1;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrder("user@example.com", List.of(item)));

        assertEquals("Product id is required", ex.getMessage());
        verifyNoInteractions(productRepository, orderRepository, orderItemRepository);
    }
}
