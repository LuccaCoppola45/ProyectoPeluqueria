package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordatorioTurnoRepository extends JpaRepository<RecordatorioTurnoEntity, Long> {
    boolean existsByTurno_IdTurnoAndTipo(Long turnoId, String tipo);
}
