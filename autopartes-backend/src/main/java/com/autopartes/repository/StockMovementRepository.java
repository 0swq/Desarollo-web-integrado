package com.autopartes.repository;

import com.autopartes.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByFechaDesc(Long productId);
    List<StockMovement> findTop100ByOrderByFechaDesc();
}
