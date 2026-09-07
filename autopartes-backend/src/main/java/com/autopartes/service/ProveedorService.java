package com.autopartes.service;

import com.autopartes.dto.proveedor.ProveedorRequest;
import com.autopartes.model.Proveedor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ProveedorService {

    private final com.autopartes.repository.ProveedorRepository repository;

    public ProveedorService(com.autopartes.repository.ProveedorRepository repository) {
        this.repository = repository;
    }

    public Proveedor crear(ProveedorRequest request) {
        if (repository.existePorRuc(request.getRuc())) {
            throw new com.autopartes.exception.BusinessException("Ya existe un proveedor con el RUC: " + request.getRuc());
        }

        Proveedor proveedor = new Proveedor();
        proveedor.setId(UUID.randomUUID());
        proveedor.setRuc(request.getRuc().trim());
        proveedor.setRazonSocial(request.getRazonSocial().trim());
        proveedor.setContactoNombre(request.getContactoNombre());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setActivo(request.getActivo() != null ? request.getActivo() : true);
        proveedor.setFechaCreacion(java.time.LocalDateTime.now());
        proveedor.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(proveedor);
    }

    public Optional<Proveedor> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public Optional<Proveedor> buscarPorRuc(String ruc) {
        return repository.buscarPorRuc(ruc);
    }

    public List<Proveedor> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<Proveedor> buscarActivos() {
        return repository.buscarActivos();
    }

    public Proveedor actualizar(UUID id, ProveedorRequest request) {
        Proveedor proveedor = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Proveedor no encontrado"));

        if (!proveedor.getRuc().equals(request.getRuc().trim()) && repository.existePorRuc(request.getRuc())) {
            throw new com.autopartes.exception.BusinessException("Ya existe otro proveedor con el RUC: " + request.getRuc());
        }

        proveedor.setRuc(request.getRuc().trim());
        proveedor.setRazonSocial(request.getRazonSocial().trim());
        proveedor.setContactoNombre(request.getContactoNombre());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());
        if (request.getActivo() != null) {
            proveedor.setActivo(request.getActivo());
        }
        proveedor.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(proveedor);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void toggleActivo(UUID id) {
        Proveedor proveedor = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Proveedor no encontrado"));
        proveedor.setActivo(!Boolean.TRUE.equals(proveedor.getActivo()));
        proveedor.setFechaActualizacion(java.time.LocalDateTime.now());
        repository.guardar(proveedor);
    }

    public long contar() {
        return repository.contar();
    }
}