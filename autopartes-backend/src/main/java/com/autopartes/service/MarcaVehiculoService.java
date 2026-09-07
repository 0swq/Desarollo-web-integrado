package com.autopartes.service;

import com.autopartes.dto.marcavehiculo.MarcaVehiculoRequest;
import com.autopartes.model.MarcaVehiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class MarcaVehiculoService {

    private final com.autopartes.repository.MarcaVehiculoRepository repository;

    public MarcaVehiculoService(com.autopartes.repository.MarcaVehiculoRepository repository) {
        this.repository = repository;
    }

    public MarcaVehiculo crear(MarcaVehiculoRequest request) {
        if (repository.existePorNombre(request.getNombre())) {
            throw new com.autopartes.exception.BusinessException("Ya existe una marca con el nombre: " + request.getNombre());
        }

        MarcaVehiculo marca = new MarcaVehiculo();
        marca.setId(UUID.randomUUID());
        marca.setNombre(request.getNombre().trim());
        marca.setPaisOrigen(request.getPaisOrigen());
        marca.setActivo(request.getActivo() != null ? request.getActivo() : true);

        return repository.guardar(marca);
    }

    public Optional<MarcaVehiculo> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public Optional<MarcaVehiculo> buscarPorNombre(String nombre) {
        return repository.buscarPorNombre(nombre);
    }

    public List<MarcaVehiculo> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<MarcaVehiculo> buscarActivas() {
        return repository.buscarActivas();
    }

    public MarcaVehiculo actualizar(UUID id, MarcaVehiculoRequest request) {
        MarcaVehiculo marca = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Marca no encontrada"));

        if (!marca.getNombre().equalsIgnoreCase(request.getNombre()) && repository.existePorNombre(request.getNombre())) {
            throw new com.autopartes.exception.BusinessException("Ya existe otra marca con el nombre: " + request.getNombre());
        }

        marca.setNombre(request.getNombre().trim());
        marca.setPaisOrigen(request.getPaisOrigen());
        if (request.getActivo() != null) {
            marca.setActivo(request.getActivo());
        }

        return repository.guardar(marca);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void toggleActivo(UUID id) {
        MarcaVehiculo marca = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Marca no encontrada"));
        marca.setActivo(!Boolean.TRUE.equals(marca.getActivo()));
        repository.guardar(marca);
    }

    public long contar() {
        return repository.contar();
    }
}