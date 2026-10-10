package com.autopartes.repository;

import com.autopartes.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    Optional<Producto> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    List<Producto> findByProveedor_Id(UUID proveedorId);

    List<Producto> findByActivoTrue();

    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String nombre, String descripcion, String sku);

    default Optional<Producto> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Producto> buscarPorSku(String sku) {
        return findBySkuIgnoreCase(sku);
    }

    default boolean existePorSku(String sku) {
        return existsBySkuIgnoreCase(sku);
    }

    default List<Producto> buscarPorProveedor(UUID proveedorId) {
        return findByProveedor_Id(proveedorId);
    }

    default List<Producto> buscarActivos() {
        return findByActivoTrue();
    }

    default List<Producto> buscarPorNombreContiene(String texto) {
        return findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrSkuContainingIgnoreCase(
                texto, texto, texto);
    }

    default List<Producto> buscarTodos() {
        return findAll();
    }

    default Producto guardar(Producto producto) {
        return save(producto);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}