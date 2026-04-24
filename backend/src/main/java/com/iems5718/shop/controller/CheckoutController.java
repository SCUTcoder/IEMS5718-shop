package com.iems5718.shop.controller;

import com.iems5718.shop.dto.CheckoutRequest;
import com.iems5718.shop.dto.OrderResponse;
import com.iems5718.shop.model.Order;
import com.iems5718.shop.model.OrderItem;
import com.iems5718.shop.security.CurrentUser;
import com.iems5718.shop.service.AuthService;
import com.iems5718.shop.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthService authService;

    @Value("${shop.stripe-secret-key:}")
    private String stripeSecretKey;

    @Value("${shop.stripe-publishable-key:}")
    private String stripePublishableKey;

    @Value("${shop.stripe-webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${app.frontend-url:http://127.0.0.1:8080}")
    private String frontendUrl;

    @PostMapping("/create")
    public ResponseEntity<?> createCheckout(HttpServletRequest request,
            @RequestBody List<CheckoutRequest.CartItemDto> cartItems) {
        CurrentUser currentUser = authService.getCurrentUser(request)
                .orElseThrow(() -> new RuntimeException("Authentication required"));
        String username = currentUser.user().getEmail();

        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        List<OrderService.CartItemSummary> summaries = new ArrayList<>();
        for (CheckoutRequest.CartItemDto dto : cartItems) {
            OrderService.CartItemSummary s = new OrderService.CartItemSummary();
            s.pid = dto.getPid();
            s.quantity = dto.getQuantity();
            summaries.add(s);
        }

        OrderResponse orderResponse = orderService.createOrder(username, summaries);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderResponse.getOrderId());
        result.put("order", orderResponse);

        // If Stripe is configured, create a checkout session
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()
                && !stripeSecretKey.contains("your_secret_key")) {
            try {
                com.stripe.Stripe.apiKey = stripeSecretKey;
                com.stripe.param.checkout.SessionCreateParams.Builder builder =
                    com.stripe.param.checkout.SessionCreateParams.builder()
                    .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/checkout-success.html?order_id=" + orderResponse.getOrderId())
                    .setCancelUrl(frontendUrl + "/checkout-cancel.html?order_id=" + orderResponse.getOrderId())
                    .putMetadata("order_id", String.valueOf(orderResponse.getOrderId()));

                for (OrderResponse.OrderItemDto item : orderResponse.getItems()) {
                    long unitAmount = BigDecimal.valueOf(item.getPrice())
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP).longValue();
                    builder.addLineItem(
                        com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
                            .setPriceData(
                                com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(unitAmount)
                                    .setProductData(
                                        com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(item.getProductName())
                                            .build())
                                    .build())
                            .build());
                }

                com.stripe.model.checkout.Session session =
                    com.stripe.model.checkout.Session.create(builder.build());
                result.put("checkoutUrl", session.getUrl());
                result.put("sessionId", session.getId());
                result.put("useStripe", true);
            } catch (Exception e) {
                result.put("useStripe", false);
                result.put("stripeError", e.getMessage());
            }
        } else {
            result.put("useStripe", false);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            List<OrderItem> items = orderService.orderItemRepository.findByOrderOrderId(orderId);

            List<OrderResponse.OrderItemDto> itemDtos = new ArrayList<>();
            for (OrderItem item : items) {
                itemDtos.add(OrderResponse.OrderItemDto.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build());
            }
            OrderResponse response = OrderResponse.builder()
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
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Stripe sends the raw body + Stripe-Signature header.
     * We must read the raw bytes to verify the HMAC signature before parsing.
     * P5 Requirement: "Validate the authenticity of data by verifying it is sent from Stripe"
     */
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<?> handleWebhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        String payload = new String(rawBody, StandardCharsets.UTF_8);
        System.out.println("[Webhook] Incoming event payload length=" + payload.length());

        com.stripe.model.Event event;

        // P5 Requirement: Verify the webhook signature if a webhook secret is configured
        if (stripeWebhookSecret != null && !stripeWebhookSecret.isBlank()) {
            if (sigHeader == null || sigHeader.isBlank()) {
                System.err.println("[Webhook] Missing Stripe-Signature header — rejected.");
                return ResponseEntity.status(400).body(Map.of("error", "Missing signature"));
            }
            try {
                event = com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
                System.out.println("[Webhook] Signature verified. Event type: " + event.getType());
            } catch (com.stripe.exception.SignatureVerificationException e) {
                System.err.println("[Webhook] Signature verification FAILED: " + e.getMessage());
                return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
            } catch (Exception e) {
                System.err.println("[Webhook] Failed to construct Stripe event: " + e.getMessage());
                return ResponseEntity.status(400).body(Map.of("error", "Malformed event"));
            }
        } else {
            // No webhook secret configured — parse JSON manually (test/dev mode)
            System.out.println("[Webhook] No webhook secret configured, skipping signature check (dev mode).");
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonPayload = mapper.readValue(payload, Map.class);
                processWebhookEvent((String) jsonPayload.getOrDefault("type", ""),
                        jsonPayload);
                return ResponseEntity.ok(Map.of("received", true));
            } catch (Exception e) {
                return ResponseEntity.status(400).body(Map.of("error", "Malformed JSON"));
            }
        }

        if ("checkout.session.completed".equals(event.getType())) {
            com.stripe.model.checkout.Session session =
                (com.stripe.model.checkout.Session) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
            if (session != null) {
                String sessionId     = session.getId();
                String paymentIntent = session.getPaymentIntent();
                String orderIdStr    = session.getMetadata() != null
                        ? session.getMetadata().get("order_id") : null;
                processCompletedOrder(orderIdStr, paymentIntent, sessionId);
            }
        }

        return ResponseEntity.ok(Map.of("received", true));
    }

    @SuppressWarnings("unchecked")
    private void processWebhookEvent(String eventType, Map<String, Object> payload) {
        if (!"checkout.session.completed".equals(eventType)) return;
        Map<String, Object> dataObj   = (Map<String, Object>) payload.get("data");
        Map<String, Object> sessionObj = (Map<String, Object>) dataObj.get("object");
        String sessionId     = (String) sessionObj.get("id");
        String paymentIntent = (String) sessionObj.get("payment_intent");
        Map<String, String> metadata = (Map<String, String>) sessionObj.get("metadata");
        String orderIdStr = metadata != null ? metadata.get("order_id") : null;
        processCompletedOrder(orderIdStr, paymentIntent, sessionId);
    }

    private void processCompletedOrder(String orderIdStr, String paymentIntent, String sessionId) {
        if (orderIdStr == null) {
            System.err.println("[Webhook] No order_id in metadata");
            return;
        }
        Long orderId = Long.parseLong(orderIdStr);
        try {
            java.util.Optional<Order> optOrder = orderService.orderRepository.findById(orderId);
            if (optOrder.isEmpty()) {
                System.err.println("[Webhook] Order #" + orderId + " not found");
                return;
            }
            Order order = optOrder.get();

            // P5 Requirement: Check the transaction has not been previously processed
            if ("COMPLETED".equals(order.getPaymentStatus())) {
                System.out.println("[Webhook] Order #" + orderId + " already completed, skipping.");
                return;
            }

            // P5 Requirement: Regenerate digest and validate against stored digest
            List<OrderItem> items = orderService.orderItemRepository.findByOrderOrderId(orderId);
            String recalculated = orderService.recalculateDigest(order, items);

            if (recalculated != null && recalculated.equals(order.getDigest())) {
                order.setPaymentStatus("COMPLETED");
                order.setTransactionId(paymentIntent != null ? paymentIntent : sessionId);
                order.setPaypalOrderId(sessionId);
                order.setPaidAt(LocalDateTime.now());
                orderService.orderRepository.save(order);
                System.out.println("[Webhook] Order #" + orderId + " COMPLETED. Digest verified.");
            } else {
                System.err.println("[Webhook] DIGEST MISMATCH for order #" + orderId);
            }
        } catch (Exception e) {
            System.err.println("[Webhook] Error processing order #" + orderId + ": " + e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest request) {
        CurrentUser currentUser = authService.getCurrentUser(request)
                .orElseThrow(() -> new RuntimeException("Authentication required"));
        String username = currentUser.user().getEmail();
        return ResponseEntity.ok(orderService.getUserRecentOrders(username));
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/stripe-config")
    public Map<String, String> getStripeConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", stripePublishableKey != null ? stripePublishableKey : "");
        return config;
    }
}
