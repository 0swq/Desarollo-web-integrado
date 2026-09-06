package com.autopartes.repository;

import com.autopartes.model.VehicleMake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleMakeRepository extends JpaRepository<VehicleMake, Long> {
    List<VehicleMake> findByActivoTrueOrderByNombreAsc();
    Optional<VehicleMake> findByNombreIgnoreCase(String nombre);
}
