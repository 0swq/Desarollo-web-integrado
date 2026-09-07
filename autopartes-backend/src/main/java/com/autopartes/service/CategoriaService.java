package com.autopartes.service;

import com.autopartes.dto.categoria.CategoriaRequest;
import com.autopartes.model.Categoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final com.autopartes.repository.CategoriaRepository repository;

    public CategoriaService(com.autopartes.repository.CategoriaRepository repository) {
        this.repository = repository;
    }

    public Categoria crear(CategoriaRequest request) {
        if (repository.existePorNombre(request.getNombre())) {
            throw new com.autopartes.exception.BusinessException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setImagenUrl(request.getImagenUrl());
        categoria.setActivo(request.getActivo() != null ? request.getActivo() : true);
        categoria.setFechaCreacion(java.time.LocalDateTime.now());
        categoria.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(categoria);
    }

    public Optional<Categoria> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public Optional<Categoria> buscarPorNombre(String nombre) {
        return repository.buscarPorNombre(nombre);
    }

    public List<Categoria> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<Categoria> buscarActivas() {
        return repository.buscarActivas();
    }

    public Categoria actualizar(UUID id, CategoriaRequest request) {
        Categoria categoria = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Categoría no encontrada"));

        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre()) && repository.existePorNombre(request.getNombre())) {
            throw new com.autopartes.exception.BusinessException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion());
        if (request.getImagenUrl() != null) {
            categoria.setImagenUrl(request.getImagenUrl());
        }
        if (request.getActivo() != null) {
            categoria.setActivo(request.getActivo());
        }
        categoria.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(categoria);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void toggleActivo(UUID id) {
        Categoria categoria = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Categoría no encontrada"));
        categoria.setActivo(!Boolean.TRUE.equals(categoria.getActivo()));
        categoria.setFechaActualizacion(java.time.LocalDateTime.now());
        repository.guardar(categoria);
    }

    public long contar() {
        return repository.contar();
    }
}