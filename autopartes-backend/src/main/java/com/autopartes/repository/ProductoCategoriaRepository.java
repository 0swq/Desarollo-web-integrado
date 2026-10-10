package com.autopartes.repository;

import com.autopartes.model.ProductoCategoria;
import com.autopartes.model.ProductoCategoriaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoCategoriaRepository extends JpaRepository<ProductoCategoria, ProductoCategoriaId> {

    @Query("SELECT pc FROM ProductoCategoria pc WHERE pc.id.productoId = :productoId AND pc.id.categoriaId = :categoriaId")
    Optional<ProductoCategoria> findByProductoIdAndCategoriaId(@Param("productoId") UUID productoId,
                                                               @Param("categoriaId") UUID categoriaId);

    @Query("SELECT pc FROM ProductoCategoria pc WHERE pc.id.productoId = :productoId")
    List<ProductoCategoria> findByProductoId(@Param("productoId") UUID productoId);

    @Query("SELECT pc FROM ProductoCategoria pc WHERE pc.id.categoriaId = :categoriaId")
    List<ProductoCategoria> findByCategoriaId(@Param("categoriaId") UUID categoriaId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductoCategoria pc WHERE pc.id.productoId = :productoId AND pc.id.categoriaId = :categoriaId")
    void deleteByProductoIdAndCategoriaId(@Param("productoId") UUID productoId,
                                         @Param("categoriaId") UUID categoriaId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductoCategoria pc WHERE pc.id.productoId = :productoId")
    void deleteByProductoId(@Param("productoId") UUID productoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductoCategoria pc WHERE pc.id.categoriaId = :categoriaId")
    void deleteByCategoriaId(@Param("categoriaId") UUID categoriaId);

    default Optional<ProductoCategoria> buscarPorIds(UUID productoId, UUID categoriaId) {
        return findByProductoIdAndCategoriaId(productoId, categoriaId);
    }

    default List<UUID> buscarCategoriasPorProducto(UUID productoId) {
        return findByProductoId(productoId).stream()
                .map(ProductoCategoria::getCategoriaId)
                .toList();
    }

    default List<UUID> buscarProductosPorCategoria(UUID categoriaId) {
        return findByCategoriaId(categoriaId).stream()
                .map(ProductoCategoria::getProductoId)
                .toList();
    }

    default List<ProductoCategoria> buscarTodos() {
        return findAll();
    }

    default ProductoCategoria guardar(ProductoCategoria productoCategoria) {
        return save(productoCategoria);
    }

    default void eliminar(UUID productoId, UUID categoriaId) {
        deleteByProductoIdAndCategoriaId(productoId, categoriaId);
    }

    default void eliminarPorProducto(UUID productoId) {
        deleteByProductoId(productoId);
    }

    default void eliminarPorCategoria(UUID categoriaId) {
        deleteByCategoriaId(categoriaId);
    }

    default long contar() {
        return count();
    }
}