package com.autopartes.dto.order;

import com.autopartes.dto.stock.StockResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private BigDecimal totalVentasPEN;
    private Long totalOrdenes;
    private Long ordenesPendientes;
    private Long ordenesPagadas;
    private Long totalProductosActivos;
    private Long totalClientes;
    private List<StockResponse> alertasBajoStock;
}
