package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class StockRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Stock> buscarPorId(UUID id) {
        return data.getStocks().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public Optional<Stock> buscarPorProducto(UUID productoId) {
        return data.getStocks().stream()
                .filter(s -> s.getProductoId().equals(productoId))
                .findFirst();
    }

    public List<Stock> buscarBajoStock() {
        return data.getStocks().stream()
                .filter(s -> s.getCantidad() <= s.getStockMinimo())
                .toList();
    }

    public List<Stock> buscarTodos() {
        return new ArrayList<>(data.getStocks());
    }

    public Stock guardar(Stock stock) {
        data.getStocks().removeIf(s -> s.getId().equals(stock.getId()));
        data.getStocks().add(stock);
        return stock;
    }

    public void eliminar(UUID id) {
        data.getStocks().removeIf(s -> s.getId().equals(id));
    }

    public void eliminarPorProducto(UUID productoId) {
        data.getStocks().removeIf(s -> s.getProductoId().equals(productoId));
    }

    public long contar() {
        return data.getStocks().size();
    }
}
