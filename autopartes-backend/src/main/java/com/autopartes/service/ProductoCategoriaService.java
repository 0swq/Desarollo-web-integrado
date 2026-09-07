package com.autopartes.service;

import com.autopartes.dto.productocategoria.ProductoCategoriaRequest;
import com.autopartes.model.ProductoCategoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ProductoCategoriaService {

    private final com.autopartes.repository.ProductoCategoriaRepository repository;

    public ProductoCategoriaService(com.autopartes.repository.ProductoCategoriaRepository repository) {
        this.repository = repository;
    }

    public ProductoCategoria asociar(ProductoCategoriaRequest request) {
        if (repository.buscarPorIds(request.getProductoId(), request.getCategoriaId()).isPresent()) {
            throw new com.autopartes.exception.BusinessException("La asociación ya existe");
        }

        ProductoCategoria pc = new ProductoCategoria();
        pc.setProductoId(request.getProductoId());
        pc.setCategoriaId(request.getCategoriaId());

        return repository.guardar(pc);
    }

    public Optional<ProductoCategoria> buscarPorIds(UUID productoId, UUID categoriaId) {
        return repository.buscarPorIds(productoId, categoriaId);
    }

    public List<UUID> buscarCategoriasPorProducto(UUID productoId) {
        return repository.buscarCategoriasPorProducto(productoId);
    }

    public List<UUID> buscarProductosPorCategoria(UUID categoriaId) {
        return repository.buscarProductosPorCategoria(categoriaId);
    }

    public List<ProductoCategoria> buscarTodos() {
        return repository.buscarTodos();
    }

    public void eliminar(UUID productoId, UUID categoriaId) {
        repository.eliminar(productoId, categoriaId);
    }

    public void eliminarPorProducto(UUID productoId) {
        repository.eliminarPorProducto(productoId);
    }

    public void eliminarPorCategoria(UUID categoriaId) {
        repository.eliminarPorCategoria(categoriaId);
    }

    public long contar() {
        return repository.contar();
    }
}