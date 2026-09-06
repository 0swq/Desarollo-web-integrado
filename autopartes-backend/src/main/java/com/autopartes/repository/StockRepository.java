package com.autopartes.repository;

import com.autopartes.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long productId);

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p WHERE s.cantidad <= s.stockMinimo AND p.activo = true")
    List<Stock> findLowStockAlerts();
}
