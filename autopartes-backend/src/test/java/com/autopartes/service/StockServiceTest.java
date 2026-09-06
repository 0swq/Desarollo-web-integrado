package com.autopartes.service;

import com.autopartes.dto.stock.StockMovementRequest;
import com.autopartes.dto.stock.StockMovementResponse;
import com.autopartes.dto.stock.StockResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.model.*;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.StockMovementRepository;
import com.autopartes.repository.StockRepository;
import com.autopartes.repository.UserRepository;
import com.autopartes.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product testProduct;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("FILT-AC-001")
                .nombre("Filtro de Aceite")
                .precio(new BigDecimal("35.00"))
                .activo(true)
                .build();

        testStock = Stock.builder()
                .id(10L)
                .product(testProduct)
                .cantidad(10)
                .stockMinimo(5)
                .ubicacionAlmacen("Pasillo A-1")
                .build();
    }

    @Test
    @DisplayName("Debe registrar ENTRADA de inventario e incrementar stock")
    void shouldIncreaseStockOnEntrada() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(1L);
        request.setTipo(StockMovementType.ENTRADA);
        request.setCantidad(15);
        request.setMotivo("Llegada de importación");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(testStock));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovementResponse response = stockService.recordMovement(request, "admin@autopartes.com");

        assertThat(response).isNotNull();
        assertThat(testStock.getCantidad()).isEqualTo(25);
        assertThat(response.getStockNuevo()).isEqualTo(25);
        assertThat(response.getTipo()).isEqualTo(StockMovementType.ENTRADA);
        verify(stockRepository).save(testStock);
    }

    @Test
    @DisplayName("Debe registrar SALIDA de inventario y decrementar stock")
    void shouldDecreaseStockOnSalida() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(1L);
        request.setTipo(StockMovementType.SALIDA);
        request.setCantidad(4);
        request.setMotivo("Venta en mostrador");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(testStock));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovementResponse response = stockService.recordMovement(request, "vendedor@autopartes.com");

        assertThat(response).isNotNull();
        assertThat(testStock.getCantidad()).isEqualTo(6);
        assertThat(response.getStockNuevo()).isEqualTo(6);
        verify(stockRepository).save(testStock);
    }

    @Test
    @DisplayName("Debe lanzar BusinessException al intentar sacar más stock del disponible")
    void shouldThrowExceptionWhenInsufficientStock() {
        StockMovementRequest request = new StockMovementRequest();
        request.setProductId(1L);
        request.setTipo(StockMovementType.SALIDA);
        request.setCantidad(50); // Solo hay 10
        request.setMotivo("Venta mayorista");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(testStock));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> stockService.recordMovement(request, "admin@autopartes.com"));

        assertThat(ex.getMessage()).contains("Stock insuficiente");
        verify(movementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Debe devolver las alertas de stock bajo")
    void shouldReturnLowStockAlerts() {
        Stock lowStock = Stock.builder()
                .id(2L)
                .product(testProduct)
                .cantidad(2)
                .stockMinimo(5)
                .build();

        when(stockRepository.findLowStockAlerts()).thenReturn(List.of(lowStock));

        List<StockResponse> alerts = stockService.getLowStockAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getBajoStock()).isTrue();
    }
}
