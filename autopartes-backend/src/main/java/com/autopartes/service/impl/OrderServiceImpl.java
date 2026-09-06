package com.autopartes.service.impl;

import com.autopartes.dto.auth.UserResponse;
import com.autopartes.dto.order.CreateOrderRequest;
import com.autopartes.dto.order.DashboardSummaryDTO;
import com.autopartes.dto.order.OrderItemResponse;
import com.autopartes.dto.order.OrderResponse;
import com.autopartes.dto.stock.StockMovementRequest;
import com.autopartes.dto.stock.StockResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.*;
import com.autopartes.repository.*;
import com.autopartes.service.OrderService;
import com.autopartes.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(String userEmail, CreateOrderRequest request) {
        User cliente = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", userEmail));

        Cart cart = cartRepository.findByUserId(cliente.getId())
                .orElseThrow(() -> new BusinessException("El carrito no existe"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("No puede crear una orden con el carrito vacío");
        }

        // 1. Validar y calcular totales
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        String numeroOrden = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Order order = Order.builder()
                .numeroOrden(numeroOrden)
                .cliente(cliente)
                .estado(OrderStatus.PENDIENTE)
                .moneda("PEN")
                .direccionEntrega(request.getDireccionEntrega().trim())
                .telefonoContacto(request.getTelefonoContacto())
                .notas(request.getNotas())
                .items(new ArrayList<>())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal itemSubtotal = cartItem.getPrecioUnitario().multiply(BigDecimal.valueOf(cartItem.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .nombreProducto(product.getNombre())
                    .sku(product.getSku())
                    .cantidad(cartItem.getCantidad())
                    .precioUnitario(cartItem.getPrecioUnitario())
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(orderItem);

            // 2. Descontar inventario automáticamente mediante StockService (Kardex: SALIDA)
            StockMovementRequest movementRequest = new StockMovementRequest();
            movementRequest.setProductId(product.getId());
            movementRequest.setTipo(StockMovementType.SALIDA);
            movementRequest.setCantidad(cartItem.getCantidad());
            movementRequest.setMotivo("Venta realizada - Orden " + numeroOrden);
            movementRequest.setReferencia(numeroOrden);

            stockService.recordMovement(movementRequest, userEmail);
        }

        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setIgv(igv);
        order.setTotal(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // 3. Crear registro de pago inicial
        Payment payment = Payment.builder()
                .order(savedOrder)
                .monto(total)
                .moneda("PEN")
                .status(PaymentStatus.PENDIENTE)
                .metodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : "MERCADO_PAGO")
                .build();
        paymentRepository.save(payment);

        // 4. Vaciar el carrito del usuario
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", id));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumeroOrden(String numeroOrden) {
        Order order = orderRepository.findByNumeroOrden(numeroOrden)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "numeroOrden", numeroOrden));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", userEmail));
        return orderRepository.findByClienteIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String adminEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", orderId));

        OrderStatus previousStatus = order.getEstado();

        // Si la orden pasa a cancelada y no estaba cancelada previamente, restituir stock
        if (newStatus == OrderStatus.CANCELADO && previousStatus != OrderStatus.CANCELADO) {
            for (OrderItem item : order.getItems()) {
                StockMovementRequest returnRequest = new StockMovementRequest();
                returnRequest.setProductId(item.getProduct().getId());
                returnRequest.setTipo(StockMovementType.ENTRADA);
                returnRequest.setCantidad(item.getCantidad());
                returnRequest.setMotivo("Cancelación de Orden " + order.getNumeroOrden() + " - Retorno a inventario");
                returnRequest.setReferencia(order.getNumeroOrden());
                stockService.recordMovement(returnRequest, adminEmail);
            }
        }

        order.setEstado(newStatus);
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary() {
        BigDecimal totalVentas = orderRepository.sumTotalVentas();
        Long totalOrdenes = orderRepository.count();
        Long ordenesPendientes = orderRepository.countByEstado(OrderStatus.PENDIENTE);
        Long ordenesPagadas = orderRepository.countByEstado(OrderStatus.PAGADO);
        Long totalProductos = productRepository.count();
        Long totalClientes = (long) userRepository.findByRol(Role.ROLE_CLIENTE).size();
        List<StockResponse> alertasBajoStock = stockService.getLowStockAlerts();

        return DashboardSummaryDTO.builder()
                .totalVentasPEN(totalVentas != null ? totalVentas : BigDecimal.ZERO)
                .totalOrdenes(totalOrdenes)
                .ordenesPendientes(ordenesPendientes)
                .ordenesPagadas(ordenesPagadas)
                .totalProductosActivos(totalProductos)
                .totalClientes(totalClientes)
                .alertasBajoStock(alertasBajoStock)
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        UserResponse clienteDTO = UserResponse.builder()
                .id(order.getCliente().getId())
                .email(order.getCliente().getEmail())
                .nombre(order.getCliente().getNombre())
                .apellido(order.getCliente().getApellido())
                .telefono(order.getCliente().getTelefono())
                .direccion(order.getCliente().getDireccion())
                .rol(order.getCliente().getRol())
                .activo(order.getCliente().getActivo())
                .build();

        List<OrderItemResponse> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .nombreProducto(item.getNombreProducto())
                        .sku(item.getSku())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .numeroOrden(order.getNumeroOrden())
                .cliente(clienteDTO)
                .estado(order.getEstado())
                .subtotal(order.getSubtotal())
                .igv(order.getIgv())
                .total(order.getTotal())
                .moneda(order.getMoneda())
                .direccionEntrega(order.getDireccionEntrega())
                .telefonoContacto(order.getTelefonoContacto())
                .notas(order.getNotas())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
