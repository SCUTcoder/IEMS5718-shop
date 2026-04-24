package com.iems5718.shop.service;

import com.iems5718.shop.dto.OrderResponse;
import com.iems5718.shop.model.Order;
import com.iems5718.shop.model.OrderItem;
import com.iems5718.shop.model.Product;
import com.iems5718.shop.repository.OrderItemRepository;
import com.iems5718.shop.repository.OrderRepository;
import com.iems5718.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Value("${shop.merchant-email:admin@shop.local}")
    private String merchantEmail;
    
    @Value("${shop.currency:USD}")
    private String currency;

    public final OrderRepository orderRepository;
    public final OrderItemRepository orderItemRepository;
    public final ProductRepository productRepository;
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(String username, List<CartItemSummary> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        String salt = generateSalt();
        StringBuilder digestSource = new StringBuilder();
        digestSource.append(currency).append("|").append(merchantEmail).append("|").append(salt).append("|");

        double totalPrice = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemSummary item : items) {
            if (item.quantity == null || item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be a positive number");
            }
            Product product = productRepository.findById(item.pid)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.pid));

            double currentPrice = product.getPrice();
            digestSource.append(item.pid).append(":").append(item.quantity).append(":").append(currentPrice).append("|");
            totalPrice += currentPrice * item.quantity;

            OrderItem oi = new OrderItem();
            oi.setProductId(item.pid);
            oi.setProductName(product.getName());
            oi.setQuantity(item.quantity);
            oi.setPrice(currentPrice);
            orderItems.add(oi);
        }
        digestSource.append(totalPrice);
        String digest = sha256Hex(digestSource.toString());

        Order order = new Order();
        order.setUsername(username);
        order.setCurrency(currency);
        order.setMerchantEmail(merchantEmail);
        order.setSalt(salt);
        order.setDigest(digest);
        order.setTotalPrice(totalPrice);
        order.setPaymentStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        for (OrderItem oi : orderItems) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
        }
        return toOrderResponse(savedOrder, orderItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserRecentOrders(String username) {
        List<Order> orders = orderRepository.findByUsername(username, PageRequest.of(0, 5));
        List<OrderResponse> result = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
            result.add(toOrderResponse(order, items));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> result = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
            result.add(toOrderResponse(order, items));
        }
        return result;
    }

    private String generateSalt() {
        byte[] salt = new byte[32];
        SECURE_RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute digest", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items) {
        List<OrderResponse.OrderItemDto> itemDtos = new ArrayList<>();
        for (OrderItem item : items) {
            itemDtos.add(OrderResponse.OrderItemDto.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build());
        }
        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .username(order.getUsername())
            .currency(order.getCurrency())
            .totalPrice(order.getTotalPrice())
            .paymentStatus(order.getPaymentStatus())
            .transactionId(order.getTransactionId())
            .createdAt(order.getCreatedAt())
            .paidAt(order.getPaidAt())
            .items(itemDtos)
            .build();
    }

    public static class CartItemSummary {
        public Long pid;
        public Integer quantity;
    }

    /**
     * Recalculate digest from the raw data stored in an Order and its OrderItems.
     * Used to verify integrity when a webhook arrives.
     */
    public String recalculateDigest(Order order, List<OrderItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getCurrency()).append("|")
          .append(order.getMerchantEmail()).append("|")
          .append(order.getSalt()).append("|");

        double calculatedTotal = 0.0;
        for (OrderItem item : items) {
            sb.append(item.getProductId()).append(":")
              .append(item.getQuantity()).append(":")
              .append(item.getPrice()).append("|");
            calculatedTotal += item.getPrice() * item.getQuantity();
        }
        sb.append(calculatedTotal);
        return sha256Hex(sb.toString());
    }
}
