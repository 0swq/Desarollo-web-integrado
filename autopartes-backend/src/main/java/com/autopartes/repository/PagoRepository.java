package com.autopartes.repository;

import com.autopartes.model.EstadoPago;
import com.autopartes.DataStore;
import com.autopartes.model.Pago;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class PagoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Pago> buscarPorId(UUID id) {
        return data.getPagos().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<Pago> buscarPorOrden(UUID ordenId) {
        return data.getPagos().stream()
                .filter(p -> p.getOrdenId().equals(ordenId))
                .findFirst();
    }

    public Optional<Pago> buscarPorMercadoPagoPagoId(String mercadoPagoPagoId) {
        return data.getPagos().stream()
                .filter(p -> mercadoPagoPagoId.equals(p.getMercadoPagoPagoId()))
                .findFirst();
    }

    public Optional<Pago> buscarPorMercadoPagoPreferenciaId(String mercadoPagoPreferenciaId) {
        return data.getPagos().stream()
                .filter(p -> mercadoPagoPreferenciaId.equals(p.getMercadoPagoPreferenciaId()))
                .findFirst();
    }

    public List<Pago> buscarPorEstado(EstadoPago estado) {
        return data.getPagos().stream()
                .filter(p -> p.getEstado() == estado)
                .toList();
    }

    public List<Pago> buscarTodos() {
        return new ArrayList<>(data.getPagos());
    }

    public Pago guardar(Pago pago) {
        data.getPagos().removeIf(p -> p.getId().equals(pago.getId()));
        data.getPagos().add(pago);
        return pago;
    }

    public void eliminar(UUID id) {
        data.getPagos().removeIf(p -> p.getId().equals(id));
    }

    public long contar() {
        return data.getPagos().size();
    }
}
