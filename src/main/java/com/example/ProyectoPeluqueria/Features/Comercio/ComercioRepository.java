package com.example.ProyectoPeluqueria.Features.Comercio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComercioRepository extends JpaRepository<ComercioEntity, Long> {
    List<ComercioEntity> findByNombreIgnoreCase(String nombre);
    Optional<ComercioEntity> findByNombreIgnoreCaseAndDireccionIgnoreCase(String nombre, String direccion);
    List<ComercioEntity> findByCategorias_UniqueId(Long categoriaId);
}
