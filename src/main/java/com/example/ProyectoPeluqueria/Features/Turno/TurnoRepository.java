package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long>
{
    boolean existsByUsuario_IdUsuarioAndFechaAndHoraInicio(
            Long usuarioId,
            LocalDate fecha,
            LocalTime horaInicio);

    List<TurnoEntity> findByServicio_IdServicioAndFecha(
            Long servicioId,
            LocalDate fecha);

    List<TurnoEntity> findByComercio_IdComercioAndFecha(
            Long comercioId,
            LocalDate fecha);

    List<TurnoEntity> findByUsuario_IdUsuarioAndFecha(
            Long usuarioId,
            LocalDate fecha);

    List<TurnoEntity> findByUsuario_IdUsuario(Long usuarioId);

    boolean existsByUsuario_IdUsuario(Long idUsuario);

    boolean existsByServicio_IdServicio(long id);
}
