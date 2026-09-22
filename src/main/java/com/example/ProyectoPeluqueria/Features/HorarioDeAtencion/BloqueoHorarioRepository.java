package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BloqueoHorarioRepository extends JpaRepository<BloqueoHorarioEntity, Long> {

    List<BloqueoHorarioEntity> findByComercio_IdComercioAndFechaBetweenOrderByFechaAsc(
            Long comercioId,
            LocalDate desde,
            LocalDate hasta);

    Optional<BloqueoHorarioEntity> findByComercio_IdComercioAndFecha(
            Long comercioId,
            LocalDate fecha);

    boolean existsByComercio_IdComercioAndFecha(Long comercioId, LocalDate fecha);
}
