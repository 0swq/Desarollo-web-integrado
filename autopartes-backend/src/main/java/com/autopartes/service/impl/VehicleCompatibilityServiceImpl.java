package com.autopartes.service.impl;

import com.autopartes.dto.product.ProductResponse;
import com.autopartes.dto.vehicle.VehicleCompatibilityRequest;
import com.autopartes.dto.vehicle.VehicleCompatibilityResponse;
import com.autopartes.dto.vehicle.VehicleMakeDTO;
import com.autopartes.dto.vehicle.VehicleModelDTO;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.Product;
import com.autopartes.model.VehicleCompatibility;
import com.autopartes.model.VehicleMake;
import com.autopartes.model.VehicleModel;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.VehicleCompatibilityRepository;
import com.autopartes.repository.VehicleMakeRepository;
import com.autopartes.repository.VehicleModelRepository;
import com.autopartes.service.ProductService;
import com.autopartes.service.VehicleCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleCompatibilityServiceImpl implements VehicleCompatibilityService {

    private final VehicleMakeRepository makeRepository;
    private final VehicleModelRepository modelRepository;
    private final VehicleCompatibilityRepository compatibilityRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleMakeDTO> getAllActiveMakes() {
        return makeRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(m -> VehicleMakeDTO.builder()
                        .id(m.getId())
                        .nombre(m.getNombre())
                        .paisOrigen(m.getPaisOrigen())
                        .activo(m.getActivo())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleMakeDTO createMake(VehicleMakeDTO makeDTO) {
        if (makeRepository.findByNombreIgnoreCase(makeDTO.getNombre().trim()).isPresent()) {
            throw new BusinessException("La marca de vehículo ya existe: " + makeDTO.getNombre());
        }

        VehicleMake make = VehicleMake.builder()
                .nombre(makeDTO.getNombre().trim())
                .paisOrigen(makeDTO.getPaisOrigen())
                .activo(makeDTO.getActivo() != null ? makeDTO.getActivo() : true)
                .build();

        VehicleMake saved = makeRepository.save(make);
        return VehicleMakeDTO.builder()
                .id(saved.getId())
                .nombre(saved.getNombre())
                .paisOrigen(saved.getPaisOrigen())
                .activo(saved.getActivo())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleModelDTO> getModelsByMake(Long makeId) {
        return modelRepository.findByMakeIdAndActivoTrueOrderByNombreAsc(makeId).stream()
                .map(this::mapToModelDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleModelDTO createModel(VehicleModelDTO modelDTO) {
        VehicleMake make = makeRepository.findById(modelDTO.getMakeId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca de vehículo", "id", modelDTO.getMakeId()));

        VehicleModel model = VehicleModel.builder()
                .nombre(modelDTO.getNombre().trim())
                .make(make)
                .tipoVehiculo(modelDTO.getTipoVehiculo())
                .activo(modelDTO.getActivo() != null ? modelDTO.getActivo() : true)
                .build();

        VehicleModel saved = modelRepository.save(model);
        return mapToModelDTO(saved);
    }

    @Override
    @Transactional
    public VehicleCompatibilityResponse addCompatibility(VehicleCompatibilityRequest request) {
        if (request.getAnioInicio() > request.getAnioFin()) {
            throw new BusinessException("El año de inicio no puede ser mayor que el año final");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductId()));

        VehicleModel model = modelRepository.findById(request.getVehicleModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo de vehículo", "id", request.getVehicleModelId()));

        VehicleCompatibility compatibility = VehicleCompatibility.builder()
                .product(product)
                .vehicleModel(model)
                .anioInicio(request.getAnioInicio())
                .anioFin(request.getAnioFin())
                .motor(request.getMotor())
                .notas(request.getNotas())
                .build();

        VehicleCompatibility saved = compatibilityRepository.save(compatibility);
        return mapToCompatibilityResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleCompatibilityResponse> getCompatibilitiesByProduct(Long productId) {
        return compatibilityRepository.findByProductId(productId).stream()
                .map(this::mapToCompatibilityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCompatibility(Long compatibilityId) {
        if (!compatibilityRepository.existsById(compatibilityId)) {
            throw new ResourceNotFoundException("Compatibilidad vehicular", "id", compatibilityId);
        }
        compatibilityRepository.deleteById(compatibilityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findCompatibleProducts(Long modelId, Integer year) {
        List<Product> products = compatibilityRepository.findCompatibleProductsByModelAndYear(modelId, year);
        return products.stream()
                .map(p -> productService.getProductById(p.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findCompatibleProductsByName(String make, String model, Integer year) {
        List<Product> products = compatibilityRepository.findCompatibleProductsByNameAndYear(make.trim(), model.trim(), year);
        return products.stream()
                .map(p -> productService.getProductById(p.getId()))
                .collect(Collectors.toList());
    }

    private VehicleModelDTO mapToModelDTO(VehicleModel model) {
        return VehicleModelDTO.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .makeId(model.getMake().getId())
                .makeNombre(model.getMake().getNombre())
                .tipoVehiculo(model.getTipoVehiculo())
                .activo(model.getActivo())
                .build();
    }

    private VehicleCompatibilityResponse mapToCompatibilityResponse(VehicleCompatibility vc) {
        return VehicleCompatibilityResponse.builder()
                .id(vc.getId())
                .productId(vc.getProduct().getId())
                .productSku(vc.getProduct().getSku())
                .productNombre(vc.getProduct().getNombre())
                .vehicleModelId(vc.getVehicleModel().getId())
                .modelNombre(vc.getVehicleModel().getNombre())
                .makeNombre(vc.getVehicleModel().getMake().getNombre())
                .anioInicio(vc.getAnioInicio())
                .anioFin(vc.getAnioFin())
                .motor(vc.getMotor())
                .notas(vc.getNotas())
                .build();
    }
}
