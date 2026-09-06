package com.autopartes.dto.order;

import com.autopartes.dto.auth.UserResponse;
import com.autopartes.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String numeroOrden;
    private UserResponse cliente;
    private OrderStatus estado;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String moneda;
    private String direccionEntrega;
    private String telefonoContacto;
    private String notas;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
