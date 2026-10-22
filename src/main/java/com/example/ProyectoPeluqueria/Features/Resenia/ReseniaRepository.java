package com.example.ProyectoPeluqueria.Features.Resenia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReseniaRepository extends JpaRepository<ReseniaEntity, Long> {

    List<ReseniaEntity> findByComercio_IdComercioOrderByFechaCreacionDesc(Long comercioId);
}
