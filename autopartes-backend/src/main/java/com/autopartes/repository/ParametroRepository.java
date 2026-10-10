package com.autopartes.repository;

import com.autopartes.model.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametroRepository extends JpaRepository<Parametro, UUID> {

    Optional<Parametro> findByClaveIgnoreCase(String clave);

    boolean existsByClaveIgnoreCase(String clave);

    @Transactional
    void deleteByClaveIgnoreCase(String clave);

    default Optional<Parametro> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Parametro> buscarPorClave(String clave) {
        return findByClaveIgnoreCase(clave);
    }

    default boolean existePorClave(String clave) {
        return existsByClaveIgnoreCase(clave);
    }

    default List<Parametro> buscarTodos() {
        return findAll();
    }

    default Parametro guardar(Parametro parametro) {
        return save(parametro);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default void eliminarPorClave(String clave) {
        deleteByClaveIgnoreCase(clave);
    }

    default long contar() {
        return count();
    }
}