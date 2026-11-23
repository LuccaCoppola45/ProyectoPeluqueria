package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecordatorioTurnoRepository extends JpaRepository<RecordatorioTurnoEntity, Long> {
    boolean existsByTurno_IdTurnoAndTipo(Long turnoId, String tipo);

    boolean existsByTurno_IdTurno(Long turnoId);

    Optional<RecordatorioTurnoEntity> findFirstByMensajeId(String mensajeId);

    /**
     * Recordatorios de cierto tipo que le llegaron (o leyó) la clienta y cuyo turno sigue
     * pendiente: los candidatos a cancelarse si no confirma a tiempo.
     */
    @Query("""
            select r from RecordatorioTurnoEntity r
            join fetch r.turno t
            where r.tipo = :tipo
              and r.estado in ('ENTREGADO', 'LEIDO')
              and t.estado = com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno.PENDIENTE
            """)
    java.util.List<RecordatorioTurnoEntity> findEntregadosConTurnoPendiente(@Param("tipo") String tipo);

    @Query("select r.turno from RecordatorioTurnoEntity r where r.mensajeId = :mensajeId")
    Optional<TurnoEntity> findTurnoByMensajeId(@Param("mensajeId") String mensajeId);
}
