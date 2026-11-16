package com.autopartes.service;

import com.autopartes.dto.compatibilidadvehiculo.CompatibilidadVehiculoRequest;
import com.autopartes.model.CompatibilidadVehiculo;
import com.autopartes.model.ModeloVehiculo;
import com.autopartes.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CompatibilidadVehiculoService {

    private final com.autopartes.repository.CompatibilidadVehiculoRepository repository;
    private final com.autopartes.repository.ProductoRepository productoRepository;
    private final com.autopartes.repository.ModeloVehiculoRepository modeloRepository;

    public CompatibilidadVehiculoService(com.autopartes.repository.CompatibilidadVehiculoRepository repository,
                                          com.autopartes.repository.ProductoRepository productoRepository,
                                          com.autopartes.repository.ModeloVehiculoRepository modeloRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
        this.modeloRepository = modeloRepository;
    }

    public CompatibilidadVehiculo crear(CompatibilidadVehiculoRequest request) {
        return guardar(new CompatibilidadVehiculo(), request);
    }

    public CompatibilidadVehiculo actualizar(UUID id, CompatibilidadVehiculoRequest request) {
        CompatibilidadVehiculo actual = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Compatibilidad no encontrada"));
        return guardar(actual, request);
    }

    private CompatibilidadVehiculo guardar(CompatibilidadVehiculo compatibilidad, CompatibilidadVehiculoRequest request) {
        if (request.getAnioInicio() > request.getAnioFin()) {
            throw new com.autopartes.exception.BusinessException("El año de inicio no puede ser mayor que el año final");
        }

        Producto producto = productoRepository.buscarPorId(request.getProductoId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));

        ModeloVehiculo modeloVehiculo = modeloRepository.buscarPorId(request.getModeloVehiculoId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Modelo de vehículo no encontrado"));



        boolean duplicada = repository.buscarPorProductoYModelo(request.getProductoId(), request.getModeloVehiculoId()).stream()
                .anyMatch(c -> !c.getId().equals(compatibilidad.getId())
                        && java.util.Objects.equals(c.getAnioInicio(), request.getAnioInicio())
                        && java.util.Objects.equals(c.getAnioFin(), request.getAnioFin())
                        && java.util.Objects.toString(c.getMotor(), "").trim().equalsIgnoreCase(
                                java.util.Objects.toString(request.getMotor(), "").trim()));
        if (duplicada) throw new com.autopartes.exception.BusinessException("Esta compatibilidad ya está registrada para el producto");
        compatibilidad.setProducto(producto);
        compatibilidad.setModeloVehiculo(modeloVehiculo);
        compatibilidad.setAnioInicio(request.getAnioInicio());
        compatibilidad.setAnioFin(request.getAnioFin());
        compatibilidad.setMotor(request.getMotor());
        compatibilidad.setNotas(request.getNotas());

        return repository.guardar(compatibilidad);
    }

    public Optional<CompatibilidadVehiculo> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public List<CompatibilidadVehiculo> buscarPorProducto(UUID productoId) {
        return repository.buscarPorProducto(productoId);
    }

    public List<CompatibilidadVehiculo> buscarPorModeloVehiculo(UUID modeloVehiculoId) {
        return repository.buscarPorModeloVehiculo(modeloVehiculoId);
    }

    public List<CompatibilidadVehiculo> buscarPorProductoYModelo(UUID productoId, UUID modeloVehiculoId) {
        return repository.buscarPorProductoYModelo(productoId, modeloVehiculoId);
    }

    public List<CompatibilidadVehiculo> buscarPorAnio(Integer anio) {
        return repository.buscarPorAnio(anio);
    }

    public List<CompatibilidadVehiculo> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<CompatibilidadVehiculo> buscarCompatiblesPorModeloYAnio(UUID modeloVehiculoId, Integer anio) {
        return repository.buscarPorModeloVehiculo(modeloVehiculoId).stream()
                .filter(c -> c.getAnioInicio() <= anio && c.getAnioFin() >= anio)
                .toList();
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void eliminarPorProducto(UUID productoId) {
        repository.eliminarPorProducto(productoId);
    }

    public long contar() {
        return repository.contar();
    }
}