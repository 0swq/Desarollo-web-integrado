package com.autopartes.repository;

import com.autopartes.model.EstadoPago;
import com.autopartes.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {

    Optional<Pago> findFirstByOrden_Id(UUID ordenId);

    Optional<Pago> findByMercadoPagoPagoId(String mercadoPagoPagoId);

    Optional<Pago> findByMercadoPagoPreferenciaId(String mercadoPagoPreferenciaId);

    List<Pago> findByEstado(EstadoPago estado);

    default Optional<Pago> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Pago> buscarPorOrden(UUID ordenId) {
        return findFirstByOrden_Id(ordenId);
    }

    default Optional<Pago> buscarPorMercadoPagoPagoId(String mercadoPagoPagoId) {
        return findByMercadoPagoPagoId(mercadoPagoPagoId);
    }

    default Optional<Pago> buscarPorMercadoPagoPreferenciaId(String mercadoPagoPreferenciaId) {
        return findByMercadoPagoPreferenciaId(mercadoPagoPreferenciaId);
    }

    default List<Pago> buscarPorEstado(EstadoPago estado) {
        return findByEstado(estado);
    }

    default List<Pago> buscarTodos() {
        return findAll();
    }

    default Pago guardar(Pago pago) {
        return save(pago);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}