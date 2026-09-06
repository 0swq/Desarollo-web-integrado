package com.autopartes.repository;

import com.autopartes.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActivoTrue();
    Optional<Category> findByNombreIgnoreCase(String nombre);
    Boolean existsByNombreIgnoreCase(String nombre);
}
