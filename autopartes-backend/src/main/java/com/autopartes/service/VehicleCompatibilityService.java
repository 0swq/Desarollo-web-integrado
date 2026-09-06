package com.autopartes.service;

import com.autopartes.dto.product.ProductResponse;
import com.autopartes.dto.vehicle.VehicleCompatibilityRequest;
import com.autopartes.dto.vehicle.VehicleCompatibilityResponse;
import com.autopartes.dto.vehicle.VehicleMakeDTO;
import com.autopartes.dto.vehicle.VehicleModelDTO;

import java.util.List;

public interface VehicleCompatibilityService {
    List<VehicleMakeDTO> getAllActiveMakes();
    VehicleMakeDTO createMake(VehicleMakeDTO makeDTO);

    List<VehicleModelDTO> getModelsByMake(Long makeId);
    VehicleModelDTO createModel(VehicleModelDTO modelDTO);

    VehicleCompatibilityResponse addCompatibility(VehicleCompatibilityRequest request);
    List<VehicleCompatibilityResponse> getCompatibilitiesByProduct(Long productId);
    void deleteCompatibility(Long compatibilityId);

    List<ProductResponse> findCompatibleProducts(Long modelId, Integer year);
    List<ProductResponse> findCompatibleProductsByName(String make, String model, Integer year);
}
