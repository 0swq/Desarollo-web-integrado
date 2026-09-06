package com.autopartes.service;

import com.autopartes.dto.stock.StockMovementRequest;
import com.autopartes.dto.stock.StockMovementResponse;
import com.autopartes.dto.stock.StockResponse;
import com.autopartes.dto.stock.StockUpdateRequest;

import java.util.List;

public interface StockService {
    StockResponse getStockByProductId(Long productId);
    StockResponse updateStockSettings(Long productId, StockUpdateRequest request);
    StockMovementResponse recordMovement(StockMovementRequest request, String userEmail);
    List<StockResponse> getLowStockAlerts();
    List<StockMovementResponse> getMovementsByProductId(Long productId);
    List<StockMovementResponse> getRecentMovements();
}
