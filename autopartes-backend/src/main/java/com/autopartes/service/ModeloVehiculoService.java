package com.autopartes.service;

import com.autopartes.dto.modelovehiculo.ModeloVehiculoRequest;
import com.autopartes.model.ModeloVehiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ModeloVehiculoService {

    private final com.autopartes.repository.ModeloVehiculoRepository repository;
    private final com.autopartes.repository.MarcaVehiculoRepository marcaRepository;

    public ModeloVehiculoService(com.autopartes.repository.ModeloVehiculoRepository repository,
                                  com.autopartes.repository.MarcaVehiculoRepository marcaRepository) {
        this.repository = repository;
        this.marcaRepository = marcaRepository;
    }

    public ModeloVehiculo crear(ModeloVehiculoRequest request) {
        marcaRepository.buscarPorId(request.getMarcaId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Marca no encontrada"));

        if (repository.existePorMarcaYNombre(request.getMarcaId(), request.getNombre())) {
            throw new com.autopartes.exception.BusinessException("Ya existe un modelo con ese nombre para esta marca");
        }

        ModeloVehiculo modelo = new ModeloVehiculo();
        modelo.setId(UUID.randomUUID());
        modelo.setNombre(request.getNombre().trim());
        modelo.setMarcaId(request.getMarcaId());
        modelo.setTipoVehiculo(request.getTipoVehiculo());
        modelo.setActivo(request.getActivo() != null ? request.getActivo() : true);

        return repository.guardar(modelo);
    }

    public Optional<ModeloVehiculo> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public List<ModeloVehiculo> buscarPorMarca(UUID marcaId) {
        return repository.buscarPorMarca(marcaId);
    }

    public Optional<ModeloVehiculo> buscarPorMarcaYNombre(UUID marcaId, String nombre) {
        return repository.buscarPorMarcaYNombre(marcaId, nombre);
    }

    public List<ModeloVehiculo> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<ModeloVehiculo> buscarActivos() {
        return repository.buscarActivos();
    }

    public ModeloVehiculo actualizar(UUID id, ModeloVehiculoRequest request) {
        ModeloVehiculo modelo = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Modelo no encontrado"));

        marcaRepository.buscarPorId(request.getMarcaId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Marca no encontrada"));

        if (!modelo.getNombre().equalsIgnoreCase(request.getNombre()) && repository.existePorMarcaYNombre(request.getMarcaId(), request.getNombre())) {
            throw new com.autopartes.exception.BusinessException("Ya existe otro modelo con ese nombre para esta marca");
        }

        modelo.setNombre(request.getNombre().trim());
        modelo.setMarcaId(request.getMarcaId());
        modelo.setTipoVehiculo(request.getTipoVehiculo());
        if (request.getActivo() != null) {
            modelo.setActivo(request.getActivo());
        }

        return repository.guardar(modelo);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void eliminarPorMarca(UUID marcaId) {
        repository.eliminarPorMarca(marcaId);
    }

    public void toggleActivo(UUID id) {
        ModeloVehiculo modelo = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Modelo no encontrado"));
        modelo.setActivo(!Boolean.TRUE.equals(modelo.getActivo()));
        repository.guardar(modelo);
    }

    public long contar() {
        return repository.contar();
    }
}