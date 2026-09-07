package com.autopartes.service;

import com.autopartes.model.ItemOrden;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ItemOrdenService {

    private final com.autopartes.repository.ItemOrdenRepository repository;

    public ItemOrdenService(com.autopartes.repository.ItemOrdenRepository repository) {
        this.repository = repository;
    }

    public Optional<ItemOrden> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public List<ItemOrden> buscarPorOrden(UUID ordenId) {
        return repository.buscarPorOrden(ordenId);
    }

    public List<ItemOrden> buscarTodos() {
        return repository.buscarTodos();
    }

    public long contar() {
        return repository.contar();
    }
}