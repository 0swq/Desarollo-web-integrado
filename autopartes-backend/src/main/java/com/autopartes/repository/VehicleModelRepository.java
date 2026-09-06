package com.autopartes.repository;

import com.autopartes.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    List<VehicleModel> findByMakeIdAndActivoTrueOrderByNombreAsc(Long makeId);
    List<VehicleModel> findByActivoTrueOrderByNombreAsc();
}
