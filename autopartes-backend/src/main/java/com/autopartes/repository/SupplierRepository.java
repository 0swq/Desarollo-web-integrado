package com.autopartes.repository;

import com.autopartes.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByActivoTrue();
    Optional<Supplier> findByRuc(String ruc);
    Boolean existsByRuc(String ruc);
}
