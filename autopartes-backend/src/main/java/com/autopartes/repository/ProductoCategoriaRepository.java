package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.ProductoCategoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ProductoCategoriaRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<ProductoCategoria> buscarPorIds(UUID productoId, UUID categoriaId) {
        return data.getProductoCategorias().stream()
                .filter(pc -> pc.getProductoId().equals(productoId) && pc.getCategoriaId().equals(categoriaId))
                .findFirst();
    }

    public List<UUID> buscarCategoriasPorProducto(UUID productoId) {
        return data.getProductoCategorias().stream()
                .filter(pc -> pc.getProductoId().equals(productoId))
                .map(ProductoCategoria::getCategoriaId)
                .toList();
    }

    public List<UUID> buscarProductosPorCategoria(UUID categoriaId) {
        return data.getProductoCategorias().stream()
                .filter(pc -> pc.getCategoriaId().equals(categoriaId))
                .map(ProductoCategoria::getProductoId)
                .toList();
    }

    public List<ProductoCategoria> buscarTodos() {
        return new ArrayList<>(data.getProductoCategorias());
    }

    public ProductoCategoria guardar(ProductoCategoria productoCategoria) {
        data.getProductoCategorias().removeIf(pc ->
                pc.getProductoId().equals(productoCategoria.getProductoId()) &&
                pc.getCategoriaId().equals(productoCategoria.getCategoriaId()));
        data.getProductoCategorias().add(productoCategoria);
        return productoCategoria;
    }

    public void eliminar(UUID productoId, UUID categoriaId) {
        data.getProductoCategorias().removeIf(pc ->
                pc.getProductoId().equals(productoId) && pc.getCategoriaId().equals(categoriaId));
    }

    public void eliminarPorProducto(UUID productoId) {
        data.getProductoCategorias().removeIf(pc -> pc.getProductoId().equals(productoId));
    }

    public void eliminarPorCategoria(UUID categoriaId) {
        data.getProductoCategorias().removeIf(pc -> pc.getCategoriaId().equals(categoriaId));
    }

    public long contar() {
        return data.getProductoCategorias().size();
    }
}
