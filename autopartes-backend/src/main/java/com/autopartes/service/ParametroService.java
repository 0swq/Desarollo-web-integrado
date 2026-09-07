package com.autopartes.service;

import com.autopartes.dto.parametro.ParametroRequest;
import com.autopartes.model.Parametro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ParametroService {

    private final com.autopartes.repository.ParametroRepository repository;

    public ParametroService(com.autopartes.repository.ParametroRepository repository) {
        this.repository = repository;
    }

    public Parametro crear(ParametroRequest request) {
        if (repository.existePorClave(request.getClave())) {
            throw new com.autopartes.exception.BusinessException("Ya existe un parámetro con la clave: " + request.getClave());
        }

        Parametro parametro = new Parametro();
        parametro.setId(UUID.randomUUID());
        parametro.setClave(request.getClave().trim());
        parametro.setValor(request.getValor());
        parametro.setDescripcion(request.getDescripcion());
        parametro.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(parametro);
    }

    public Optional<Parametro> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public Optional<Parametro> buscarPorClave(String clave) {
        return repository.buscarPorClave(clave);
    }

    public List<Parametro> buscarTodos() {
        return repository.buscarTodos();
    }

    public Parametro actualizar(UUID id, ParametroRequest request) {
        Parametro parametro = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Parámetro no encontrado"));

        if (!parametro.getClave().equalsIgnoreCase(request.getClave()) && repository.existePorClave(request.getClave())) {
            throw new com.autopartes.exception.BusinessException("Ya existe otro parámetro con la clave: " + request.getClave());
        }

        parametro.setClave(request.getClave().trim());
        parametro.setValor(request.getValor());
        parametro.setDescripcion(request.getDescripcion());
        parametro.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(parametro);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void eliminarPorClave(String clave) {
        repository.eliminarPorClave(clave);
    }

    public long contar() {
        return repository.contar();
    }
}