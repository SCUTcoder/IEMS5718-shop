package com.iems5718.shop.repository;

import com.iems5718.shop.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPaypalOrderId(String paypalOrderId);
    Optional<Order> findByTransactionId(String transactionId);
    List<Order> findByUsername(String username, Pageable pageable);
}
