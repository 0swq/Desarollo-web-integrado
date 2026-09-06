package com.autopartes.repository;

import com.autopartes.model.Product;
import com.autopartes.model.VehicleCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleCompatibilityRepository extends JpaRepository<VehicleCompatibility, Long> {

    List<VehicleCompatibility> findByProductId(Long productId);

    @Query("SELECT DISTINCT vc.product FROM VehicleCompatibility vc " +
           "WHERE vc.product.activo = true " +
           "AND vc.vehicleModel.id = :modelId " +
           "AND (:year IS NULL OR (:year >= vc.anioInicio AND :year <= vc.anioFin))")
    List<Product> findCompatibleProductsByModelAndYear(@Param("modelId") Long modelId,
                                                      @Param("year") Integer year);

    @Query("SELECT DISTINCT vc.product FROM VehicleCompatibility vc " +
           "WHERE vc.product.activo = true " +
           "AND LOWER(vc.vehicleModel.make.nombre) = LOWER(:makeName) " +
           "AND LOWER(vc.vehicleModel.nombre) = LOWER(:modelName) " +
           "AND (:year IS NULL OR (:year >= vc.anioInicio AND :year <= vc.anioFin))")
    List<Product> findCompatibleProductsByNameAndYear(@Param("makeName") String makeName,
                                                     @Param("modelName") String modelName,
                                                     @Param("year") Integer year);
}
