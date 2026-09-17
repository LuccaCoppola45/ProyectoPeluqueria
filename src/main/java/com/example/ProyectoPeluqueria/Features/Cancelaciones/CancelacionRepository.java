package com.example.ProyectoPeluqueria.Features.Cancelaciones;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelacionRepository extends JpaRepository<CancelacionEntity, Long>
{
    boolean existsByNombreCategoria(String nombreCategoria);
}
