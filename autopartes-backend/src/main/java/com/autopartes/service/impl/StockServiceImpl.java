package com.autopartes.service.impl;

import com.autopartes.dto.stock.StockMovementRequest;
import com.autopartes.dto.stock.StockMovementResponse;
import com.autopartes.dto.stock.StockResponse;
import com.autopartes.dto.stock.StockUpdateRequest;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.*;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.StockMovementRepository;
import com.autopartes.repository.StockRepository;
import com.autopartes.repository.UserRepository;
import com.autopartes.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStockByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock no encontrado para el producto con id: " + productId));
        return mapToStockResponse(stock);
    }

    @Override
    @Transactional
    public StockResponse updateStockSettings(Long productId, StockUpdateRequest request) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock no encontrado para el producto con id: " + productId));

        if (request.getStockMinimo() != null) {
            stock.setStockMinimo(request.getStockMinimo());
        }
        if (request.getUbicacionAlmacen() != null) {
            stock.setUbicacionAlmacen(request.getUbicacionAlmacen());
        }

        return mapToStockResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional
    public StockMovementResponse recordMovement(StockMovementRequest request, String userEmail) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductId()));

        Stock stock = stockRepository.findByProductId(product.getId())
                .orElseGet(() -> stockRepository.save(Stock.builder()
                        .product(product)
                        .cantidad(0)
                        .stockMinimo(5)
                        .build()));

        User usuario = null;
        if (userEmail != null) {
            usuario = userRepository.findByEmail(userEmail).orElse(null);
        }

        int stockAnterior = stock.getCantidad();
        int stockNuevo;

        switch (request.getTipo()) {
            case ENTRADA:
                stockNuevo = stockAnterior + request.getCantidad();
                break;
            case SALIDA:
                if (stockAnterior < request.getCantidad()) {
                    throw new BusinessException(String.format("Stock insuficiente para %s. Stock actual: %d, solicitado: %d",
                            product.getNombre(), stockAnterior, request.getCantidad()));
                }
                stockNuevo = stockAnterior - request.getCantidad();
                break;
            case AJUSTE:
                stockNuevo = request.getCantidad();
                break;
            default:
                throw new BusinessException("Tipo de movimiento desconocido: " + request.getTipo());
        }

        stock.setCantidad(stockNuevo);
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .tipo(request.getTipo())
                .cantidad(request.getCantidad())
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(request.getMotivo())
                .referencia(request.getReferencia())
                .usuario(usuario)
                .build();

        StockMovement savedMovement = movementRepository.save(movement);
        return mapToMovementResponse(savedMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockAlerts() {
        return stockRepository.findLowStockAlerts().stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByProductId(Long productId) {
        return movementRepository.findByProductIdOrderByFechaDesc(productId).stream()
                .map(this::mapToMovementResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getRecentMovements() {
        return movementRepository.findTop100ByOrderByFechaDesc().stream()
                .map(this::mapToMovementResponse)
                .collect(Collectors.toList());
    }

    private StockResponse mapToStockResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .productId(stock.getProduct().getId())
                .productSku(stock.getProduct().getSku())
                .productNombre(stock.getProduct().getNombre())
                .cantidad(stock.getCantidad())
                .stockMinimo(stock.getStockMinimo())
                .bajoStock(stock.isBajoStock())
                .ubicacionAlmacen(stock.getUbicacionAlmacen())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }

    private StockMovementResponse mapToMovementResponse(StockMovement m) {
        return StockMovementResponse.builder()
                .id(m.getId())
                .productId(m.getProduct().getId())
                .productSku(m.getProduct().getSku())
                .productNombre(m.getProduct().getNombre())
                .tipo(m.getTipo())
                .cantidad(m.getCantidad())
                .stockAnterior(m.getStockAnterior())
                .stockNuevo(m.getStockNuevo())
                .motivo(m.getMotivo())
                .referencia(m.getReferencia())
                .usuarioEmail(m.getUsuario() != null ? m.getUsuario().getEmail() : "Sistema")
                .fecha(m.getFecha())
                .build();
    }
}
