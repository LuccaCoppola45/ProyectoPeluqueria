package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoRequest;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoResponse;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public interface TurnoService
{
    TurnoResponse crearTurno(TurnoRequest dto);
    List<TurnoResponse> obtenerTodos ();
    TurnoResponse obtenerPorId (Long id);
    TurnoResponse actualizarTurno(Long id, TurnoRequest dto);
    void eliminarUnTurno (Long id);
    List<LocalTime> obtenerDisponibilidad(LocalDate fecha, Long servicioId);
    void enviarRecordatorioPrueba(Long turnoId);
}
