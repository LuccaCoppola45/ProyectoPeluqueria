package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Todos los turnos con su cliente, servicio y cancelación en UNA sola consulta.
     * Con findAll() se hacía una consulta extra por cada turno (la cancelación), y con
     * la base lejos de Render eso hacía tardar mucho el panel.
     */
    @Query("""
            select distinct t from TurnoEntity t
            left join fetch t.usuario
            left join fetch t.servicio
            left join fetch t.comercio
            left join fetch t.cancelacion c
            left join fetch c.usuario
            """)
    List<TurnoEntity> findAllConDetalle();

    /** Igual que findAllConDetalle, pero solo los turnos de un cliente ("Mis turnos") */
    @Query("""
            select distinct t from TurnoEntity t
            left join fetch t.usuario u
            left join fetch t.servicio
            left join fetch t.comercio
            left join fetch t.cancelacion c
            left join fetch c.usuario
            where u.idUsuario = :usuarioId
            """)
    List<TurnoEntity> findByUsuarioConDetalle(@Param("usuarioId") Long usuarioId);

    List<TurnoEntity> findByComercio_IdComercioAndFechaBetween(
            Long comercioId,
            LocalDate desde,
            LocalDate hasta);

    boolean existsByUsuario_IdUsuario(Long idUsuario);

    boolean existsByServicio_IdServicio(long id);

    List<TurnoEntity> findByUsuario_IdUsuarioAndEstadoOrderByFechaAscHoraInicioAsc(
            Long usuarioId,
            EstadoTurno estado);
}
