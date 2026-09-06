package com.autopartes.service;

import com.autopartes.dto.order.CreateOrderRequest;
import com.autopartes.dto.order.OrderResponse;
import com.autopartes.dto.stock.StockMovementRequest;
import com.autopartes.exception.BusinessException;
import com.autopartes.model.*;
import com.autopartes.repository.*;
import com.autopartes.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("cliente@test.com")
                .nombre("Carlos")
                .apellido("Gómez")
                .telefono("987654321")
                .direccion("Av. Javier Prado 1234, Lima")
                .rol(Role.ROLE_CLIENTE)
                .activo(true)
                .build();

        testProduct = Product.builder()
                .id(100L)
                .sku("DISC-FR-001")
                .nombre("Disco de Freno Ventilado")
                .precio(new BigDecimal("100.00"))
                .activo(true)
                .build();

        CartItem item = CartItem.builder()
                .id(5L)
                .product(testProduct)
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(item);

        testCart = Cart.builder()
                .id(10L)
                .user(testUser)
                .items(cartItems)
                .build();

        testOrder = Order.builder()
                .id(50L)
                .numeroOrden("ORD-20260904-TEST01")
                .cliente(testUser)
                .estado(OrderStatus.PENDIENTE)
                .subtotal(new BigDecimal("200.00"))
                .igv(new BigDecimal("36.00"))
                .total(new BigDecimal("236.00"))
                .moneda("PEN")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Debe crear la orden, calcular subtotal, 18% IGV, descontar stock y vaciar carrito")
    void shouldCreateOrderFromCartSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setDireccionEntrega("Av. Javier Prado 1234, Lima");
        request.setTelefonoContacto("987654321");
        request.setMetodoPago("MERCADO_PAGO");

        when(userRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse response = orderService.createOrderFromCart("cliente@test.com", request);

        assertThat(response).isNotNull();
        // Subtotal = 2 * 100 = 200.00
        assertThat(response.getSubtotal()).isEqualByComparingTo("200.00");
        // IGV (18%) = 36.00
        assertThat(response.getIgv()).isEqualByComparingTo("36.00");
        // Total en Soles = 236.00
        assertThat(response.getTotal()).isEqualByComparingTo("236.00");
        assertThat(response.getMoneda()).isEqualTo("PEN");

        // Verifica que se descontó stock por cada ítem
        verify(stockService, times(1)).recordMovement(any(StockMovementRequest.class), eq("cliente@test.com"));
        // Verifica que se guardó el pago inicial
        verify(paymentRepository, times(1)).save(any(Payment.class));
        // Verifica que el carrito quedó vacío
        assertThat(testCart.getItems()).isEmpty();
        verify(cartRepository).save(testCart);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el carrito está vacío al intentar comprar")
    void shouldThrowWhenCreatingOrderWithEmptyCart() {
        testCart.getItems().clear();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setDireccionEntrega("Lima");

        when(userRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));

        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart("cliente@test.com", request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Debe restituir stock en inventario cuando se cancela una orden")
    void shouldRestoreStockWhenOrderIsCancelled() {
        OrderItem orderItem = OrderItem.builder()
                .product(testProduct)
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .build();
        testOrder.getItems().add(orderItem);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.updateOrderStatus(50L, OrderStatus.CANCELADO, "admin@autopartes.com");

        assertThat(testOrder.getEstado()).isEqualTo(OrderStatus.CANCELADO);
        // Debe haber llamado a recordMovement para reingresar el stock
        verify(stockService, times(1)).recordMovement(argThat(r ->
                r.getTipo() == StockMovementType.ENTRADA && r.getCantidad() == 2), eq("admin@autopartes.com"));
    }
}
