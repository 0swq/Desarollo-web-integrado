package com.autopartes.service;

import com.autopartes.model.Carrito;
import com.autopartes.model.ItemCarrito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CarritoService {

    private final com.autopartes.repository.CarritoRepository carritoRepository;
    private final com.autopartes.repository.ItemCarritoRepository itemRepository;
    private final com.autopartes.repository.UsuarioRepository usuarioRepository;

    public CarritoService(com.autopartes.repository.CarritoRepository carritoRepository,
                          com.autopartes.repository.ItemCarritoRepository itemRepository,
                          com.autopartes.repository.UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.itemRepository = itemRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Carrito obtenerOCrearCarrito(UUID usuarioId) {
        Optional<Carrito> existente = carritoRepository.buscarPorUsuario(usuarioId);
        if (existente.isPresent()) {
            return existente.get();
        }

        usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));

        Carrito carrito = new Carrito();
        carrito.setId(UUID.randomUUID());
        carrito.setUsuarioId(usuarioId);
        carrito.setFechaActualizacion(java.time.LocalDateTime.now());

        return carritoRepository.guardar(carrito);
    }

    public Optional<Carrito> buscarPorUsuario(UUID usuarioId) {
        return carritoRepository.buscarPorUsuario(usuarioId);
    }

    public Optional<Carrito> buscarPorId(UUID id) {
        return carritoRepository.buscarPorId(id);
    }

    public List<ItemCarrito> obtenerItems(UUID carritoId) {
        return itemRepository.buscarPorCarrito(carritoId);
    }

    public BigDecimal calcularTotal(UUID carritoId) {
        List<ItemCarrito> items = itemRepository.buscarPorCarrito(carritoId);
        return items.stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int calcularTotalItems(UUID carritoId) {
        List<ItemCarrito> items = itemRepository.buscarPorCarrito(carritoId);
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    public void vaciarCarrito(UUID carritoId) {
        itemRepository.eliminarPorCarrito(carritoId);
        Carrito carrito = carritoRepository.buscarPorId(carritoId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Carrito no encontrado"));
        carrito.setFechaActualizacion(java.time.LocalDateTime.now());
        carritoRepository.guardar(carrito);
    }

    public void eliminarCarrito(UUID id) {
        itemRepository.eliminarPorCarrito(id);
        carritoRepository.eliminar(id);
    }

    public void eliminarCarritoPorUsuario(UUID usuarioId) {
        carritoRepository.eliminarPorUsuario(usuarioId);
    }

    public long contar() {
        return carritoRepository.contar();
    }
}