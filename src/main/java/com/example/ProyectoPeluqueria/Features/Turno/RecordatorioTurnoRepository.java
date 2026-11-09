package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecordatorioTurnoRepository extends JpaRepository<RecordatorioTurnoEntity, Long> {
    boolean existsByTurno_IdTurnoAndTipo(Long turnoId, String tipo);

    boolean existsByTurno_IdTurno(Long turnoId);

    @Query("select r.turno from RecordatorioTurnoEntity r where r.mensajeId = :mensajeId")
    Optional<TurnoEntity> findTurnoByMensajeId(@Param("mensajeId") String mensajeId);
}
