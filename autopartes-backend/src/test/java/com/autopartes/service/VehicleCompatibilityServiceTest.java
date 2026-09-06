package com.autopartes.service;

import com.autopartes.dto.product.ProductResponse;
import com.autopartes.dto.vehicle.VehicleCompatibilityRequest;
import com.autopartes.dto.vehicle.VehicleCompatibilityResponse;
import com.autopartes.dto.vehicle.VehicleMakeDTO;
import com.autopartes.exception.BusinessException;
import com.autopartes.model.Category;
import com.autopartes.model.Product;
import com.autopartes.model.VehicleCompatibility;
import com.autopartes.model.VehicleMake;
import com.autopartes.model.VehicleModel;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.VehicleCompatibilityRepository;
import com.autopartes.repository.VehicleMakeRepository;
import com.autopartes.repository.VehicleModelRepository;
import com.autopartes.service.impl.VehicleCompatibilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleCompatibilityServiceTest {

    @Mock
    private VehicleMakeRepository makeRepository;

    @Mock
    private VehicleModelRepository modelRepository;

    @Mock
    private VehicleCompatibilityRepository compatibilityRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private VehicleCompatibilityServiceImpl compatibilityService;

    private VehicleMake testMake;
    private VehicleModel testModel;
    private Product testProduct;
    private VehicleCompatibility testCompatibility;

    @BeforeEach
    void setUp() {
        testMake = VehicleMake.builder()
                .id(1L)
                .nombre("Toyota")
                .paisOrigen("Japón")
                .activo(true)
                .build();

        testModel = VehicleModel.builder()
                .id(10L)
                .nombre("Yaris")
                .make(testMake)
                .tipoVehiculo("Sedan")
                .activo(true)
                .build();

        Category cat = Category.builder().id(1L).nombre("Filtros").build();

        testProduct = Product.builder()
                .id(100L)
                .sku("FILT-AC-TY01")
                .nombre("Filtro de Aceite Toyota")
                .precio(new BigDecimal("45.00"))
                .categoria(cat)
                .activo(true)
                .build();

        testCompatibility = VehicleCompatibility.builder()
                .id(50L)
                .product(testProduct)
                .vehicleModel(testModel)
                .anioInicio(2014)
                .anioFin(2021)
                .motor("1.5L Dual VVT-i")
                .build();
    }

    @Test
    @DisplayName("Debe asociar compatibilidad vehicular correctamente")
    void shouldAddCompatibilitySuccessfully() {
        VehicleCompatibilityRequest request = new VehicleCompatibilityRequest();
        request.setProductId(100L);
        request.setVehicleModelId(10L);
        request.setAnioInicio(2014);
        request.setAnioFin(2021);
        request.setMotor("1.5L Dual VVT-i");

        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(modelRepository.findById(10L)).thenReturn(Optional.of(testModel));
        when(compatibilityRepository.save(any(VehicleCompatibility.class))).thenReturn(testCompatibility);

        VehicleCompatibilityResponse response = compatibilityService.addCompatibility(request);

        assertThat(response).isNotNull();
        assertThat(response.getModelNombre()).isEqualTo("Yaris");
        assertThat(response.getMakeNombre()).isEqualTo("Toyota");
        assertThat(response.getAnioInicio()).isEqualTo(2014);
        assertThat(response.getAnioFin()).isEqualTo(2021);
    }

    @Test
    @DisplayName("Debe rechazar compatibilidad si añoInicio > añoFin")
    void shouldThrowExceptionWhenInvalidYearRange() {
        VehicleCompatibilityRequest request = new VehicleCompatibilityRequest();
        request.setProductId(100L);
        request.setVehicleModelId(10L);
        request.setAnioInicio(2022);
        request.setAnioFin(2015); // Inválido

        assertThrows(BusinessException.class, () -> compatibilityService.addCompatibility(request));
        verify(compatibilityRepository, never()).save(any(VehicleCompatibility.class));
    }

    @Test
    @DisplayName("Debe listar productos compatibles por modelo y año")
    void shouldFindCompatibleProductsByModelAndYear() {
        when(compatibilityRepository.findCompatibleProductsByModelAndYear(10L, 2018))
                .thenReturn(List.of(testProduct));

        ProductResponse pResponse = ProductResponse.builder()
                .id(100L)
                .sku("FILT-AC-TY01")
                .nombre("Filtro de Aceite Toyota")
                .precio(new BigDecimal("45.00"))
                .build();

        when(productService.getProductById(100L)).thenReturn(pResponse);

        List<ProductResponse> results = compatibilityService.findCompatibleProducts(10L, 2018);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getSku()).isEqualTo("FILT-AC-TY01");
    }

    @Test
    @DisplayName("Debe registrar una marca de vehículo")
    void shouldCreateMakeSuccessfully() {
        VehicleMakeDTO makeDTO = VehicleMakeDTO.builder()
                .nombre("Honda")
                .paisOrigen("Japón")
                .build();

        when(makeRepository.findByNombreIgnoreCase("Honda")).thenReturn(Optional.empty());
        when(makeRepository.save(any(VehicleMake.class))).thenAnswer(i -> {
            VehicleMake m = i.getArgument(0);
            m.setId(2L);
            return m;
        });

        VehicleMakeDTO result = compatibilityService.createMake(makeDTO);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getNombre()).isEqualTo("Honda");
    }
}
