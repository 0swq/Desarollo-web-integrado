package com.autopartes.repository;

import com.autopartes.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Boolean existsBySku(String sku);

    Page<Product> findByActivoTrue(Pageable pageable);

    Page<Product> findByCategoriaIdAndActivoTrue(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchActiveProducts(@Param("query") String query, Pageable pageable);

    List<Product> findTop8ByActivoTrueOrderByCreatedAtDesc();
}
