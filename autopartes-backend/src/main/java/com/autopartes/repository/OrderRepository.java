package com.autopartes.repository;

import com.autopartes.model.Order;
import com.autopartes.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByNumeroOrden(String numeroOrden);

    List<Order> findByClienteIdOrderByCreatedAtDesc(Long clienteId);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Order> findByEstado(OrderStatus estado);

    Long countByEstado(OrderStatus estado);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.estado = com.autopartes.model.OrderStatus.PAGADO OR o.estado = com.autopartes.model.OrderStatus.ENTREGADO")
    BigDecimal sumTotalVentas();
}
