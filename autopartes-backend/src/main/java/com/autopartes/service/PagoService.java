package com.autopartes.service;

import com.autopartes.dto.pago.PagoRequest;
import com.autopartes.model.EstadoPago;
import com.autopartes.model.Pago;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class PagoService {

    private final com.autopartes.repository.PagoRepository repository;
    private final com.autopartes.repository.OrdenRepository ordenRepository;

    public PagoService(com.autopartes.repository.PagoRepository repository,
                       com.autopartes.repository.OrdenRepository ordenRepository) {
        this.repository = repository;
        this.ordenRepository = ordenRepository;
    }

    public Pago crear(PagoRequest request) {
        ordenRepository.buscarPorId(request.getOrdenId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Orden no encontrada"));

        if (repository.buscarPorOrden(request.getOrdenId()).isPresent()) {
            throw new com.autopartes.exception.BusinessException("Ya existe un pago para esta orden");
        }

        Pago pago = new Pago();
        pago.setId(UUID.randomUUID());
        pago.setOrdenId(request.getOrdenId());
        pago.setMonto(request.getMonto());
        pago.setMoneda(request.getMoneda() != null ? request.getMoneda() : "PEN");
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(request.getMetodoPago());
        pago.setFechaCreacion(java.time.LocalDateTime.now());

        return repository.guardar(pago);
    }

    public Optional<Pago> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public Optional<Pago> buscarPorOrden(UUID ordenId) {
        return repository.buscarPorOrden(ordenId);
    }

    public Optional<Pago> buscarPorMercadoPagoPagoId(String mercadoPagoPagoId) {
        return repository.buscarPorMercadoPagoPagoId(mercadoPagoPagoId);
    }

    public Optional<Pago> buscarPorMercadoPagoPreferenciaId(String mercadoPagoPreferenciaId) {
        return repository.buscarPorMercadoPagoPreferenciaId(mercadoPagoPreferenciaId);
    }

    public List<Pago> buscarPorEstado(EstadoPago estado) {
        return repository.buscarPorEstado(estado);
    }

    public List<Pago> buscarTodos() {
        return repository.buscarTodos();
    }

    public Pago actualizarEstado(UUID id, EstadoPago nuevoEstado) {
        Pago pago = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Pago no encontrado"));
        pago.setEstado(nuevoEstado);
        return repository.guardar(pago);
    }

    public Pago actualizarDatosMercadoPago(UUID id, String mercadoPagoPagoId, String mercadoPagoPreferenciaId) {
        Pago pago = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Pago no encontrado"));
        if (mercadoPagoPagoId != null) {
            pago.setMercadoPagoPagoId(mercadoPagoPagoId);
        }
        if (mercadoPagoPreferenciaId != null) {
            pago.setMercadoPagoPreferenciaId(mercadoPagoPreferenciaId);
        }
        return repository.guardar(pago);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public long contar() {
        return repository.contar();
    }
}