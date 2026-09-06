package com.autopartes.dto.order;

import com.autopartes.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "El nuevo estado es obligatorio")
    private OrderStatus estado;
}
