package com.autopartes.service;

import com.autopartes.dto.order.OrdenRequest;
import com.autopartes.model.Carrito;
import com.autopartes.model.EstadoOrden;
import com.autopartes.model.EstadoPago;
import com.autopartes.model.ItemCarrito;
import com.autopartes.model.ItemOrden;
import com.autopartes.model.Orden;
import com.autopartes.model.Pago;
import com.autopartes.model.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class OrdenService {

    private final com.autopartes.repository.OrdenRepository ordenRepository;
    private final com.autopartes.repository.ItemOrdenRepository itemOrdenRepository;
    private final com.autopartes.repository.CarritoRepository carritoRepository;
    private final com.autopartes.repository.ItemCarritoRepository itemCarritoRepository;
    private final com.autopartes.repository.PagoRepository pagoRepository;
    private final com.autopartes.repository.ProductoRepository productoRepository;
    private final com.autopartes.service.StockService stockService;
    private final com.autopartes.repository.UsuarioRepository usuarioRepository;

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    public OrdenService(com.autopartes.repository.OrdenRepository ordenRepository,
                        com.autopartes.repository.ItemOrdenRepository itemOrdenRepository,
                        com.autopartes.repository.CarritoRepository carritoRepository,
                        com.autopartes.repository.ItemCarritoRepository itemCarritoRepository,
                        com.autopartes.repository.PagoRepository pagoRepository,
                        com.autopartes.repository.ProductoRepository productoRepository,
                        com.autopartes.service.StockService stockService,
                        com.autopartes.repository.UsuarioRepository usuarioRepository) {
        this.ordenRepository = ordenRepository;
        this.itemOrdenRepository = itemOrdenRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.stockService = stockService;
        this.usuarioRepository = usuarioRepository;
    }

    public Orden crearOrdenDesdeCarrito(UUID usuarioId, OrdenRequest request) {
        usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.buscarPorUsuario(usuarioId)
                .orElseThrow(() -> new com.autopartes.exception.BusinessException("El carrito no existe"));

        List<ItemCarrito> itemsCarrito = itemCarritoRepository.buscarPorCarrito(carrito.getId());
        if (itemsCarrito.isEmpty()) {
            throw new com.autopartes.exception.BusinessException("No puede crear una orden con el carrito vacío");
        }

        // Generar número de orden
        String numeroOrden = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Calcular totales y crear items de orden
        BigDecimal subtotal = BigDecimal.ZERO;
        List<ItemOrden> itemsOrden = new ArrayList<>();

        for (ItemCarrito itemCarrito : itemsCarrito) {
            Producto producto = productoRepository.buscarPorId(itemCarrito.getProductoId())
                    .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado: " + itemCarrito.getProductoId()));

            BigDecimal itemSubtotal = itemCarrito.getPrecioUnitario().multiply(BigDecimal.valueOf(itemCarrito.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);

            ItemOrden itemOrden = new ItemOrden();
            itemOrden.setId(UUID.randomUUID());
            itemOrden.setProductoId(itemCarrito.getProductoId());
            itemOrden.setNombreProducto(producto.getNombre());
            itemOrden.setSku(producto.getSku());
            itemOrden.setCantidad(itemCarrito.getCantidad());
            itemOrden.setPrecioUnitario(itemCarrito.getPrecioUnitario());
            itemOrden.setSubtotal(itemSubtotal);
            itemsOrden.add(itemOrden);

            // Registrar salida de stock automáticamente (sin usuarioId = sistema)
            stockService.registrarSalidaAutomatica(itemCarrito.getProductoId(), itemCarrito.getCantidad(), numeroOrden);
        }

        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        // Crear orden
        Orden orden = new Orden();
        orden.setId(UUID.randomUUID());
        orden.setNumeroOrden(numeroOrden);
        orden.setUsuarioId(usuarioId);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        orden.setIgv(igv);
        orden.setTotal(total);
        orden.setMoneda("PEN");
        orden.setDireccionEntrega(request.getDireccionEntrega().trim());
        orden.setTelefonoContacto(request.getTelefonoContacto());
        orden.setNotas(request.getNotas());
        orden.setFechaCreacion(java.time.LocalDateTime.now());
        orden.setFechaActualizacion(java.time.LocalDateTime.now());

        Orden ordenGuardada = ordenRepository.guardar(orden);

        // Guardar items de orden
        for (ItemOrden item : itemsOrden) {
            item.setOrdenId(ordenGuardada.getId());
            itemOrdenRepository.guardar(item);
        }

        // Crear registro de pago inicial (PENDIENTE)
        Pago pago = new Pago();
        pago.setId(UUID.randomUUID());
        pago.setOrdenId(ordenGuardada.getId());
        pago.setMonto(total);
        pago.setMoneda("PEN");
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : "MERCADO_PAGO");
        pago.setFechaCreacion(java.time.LocalDateTime.now());
        pagoRepository.guardar(pago);

        // Vaciar carrito
        itemCarritoRepository.eliminarPorCarrito(carrito.getId());
        carrito.setFechaActualizacion(java.time.LocalDateTime.now());
        carritoRepository.guardar(carrito);

        return ordenGuardada;
    }

    public Optional<Orden> buscarPorId(UUID id) {
        return ordenRepository.buscarPorId(id);
    }

    public Optional<Orden> buscarPorNumeroOrden(String numeroOrden) {
        return ordenRepository.buscarPorNumeroOrden(numeroOrden);
    }

    public List<Orden> buscarPorUsuario(UUID usuarioId) {
        return ordenRepository.buscarPorUsuario(usuarioId);
    }

    public List<Orden> buscarPorEstado(EstadoOrden estado) {
        return ordenRepository.buscarPorEstado(estado);
    }

    public List<Orden> buscarTodos() {
        return ordenRepository.buscarTodos();
    }

    public Orden actualizarEstado(UUID ordenId, EstadoOrden nuevoEstado) {
        Orden orden = ordenRepository.buscarPorId(ordenId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Orden no encontrada"));

        EstadoOrden estadoAnterior = orden.getEstado();

        // Si se cancela y no estaba cancelada, restituir stock
        if (nuevoEstado == EstadoOrden.CANCELADO && estadoAnterior != EstadoOrden.CANCELADO) {
            List<ItemOrden> items = itemOrdenRepository.buscarPorOrden(ordenId);
            for (ItemOrden item : items) {
                stockService.registrarEntradaAutomatica(item.getProductoId(), item.getCantidad(), orden.getNumeroOrden());
            }
        }

        orden.setEstado(nuevoEstado);
        orden.setFechaActualizacion(java.time.LocalDateTime.now());
        return ordenRepository.guardar(orden);
    }

    public long contar() {
        return ordenRepository.contar();
    }

    public long contarPorEstado(EstadoOrden estado) {
        return ordenRepository.contarPorEstado(estado);
    }
}