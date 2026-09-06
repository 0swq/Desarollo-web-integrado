package com.autopartes.service;

import com.autopartes.dto.order.CreateOrderRequest;
import com.autopartes.dto.order.DashboardSummaryDTO;
import com.autopartes.dto.order.OrderResponse;
import com.autopartes.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponse createOrderFromCart(String userEmail, CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByNumeroOrden(String numeroOrden);
    List<OrderResponse> getOrdersByUserEmail(String userEmail);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String adminEmail);
    DashboardSummaryDTO getDashboardSummary();
}
