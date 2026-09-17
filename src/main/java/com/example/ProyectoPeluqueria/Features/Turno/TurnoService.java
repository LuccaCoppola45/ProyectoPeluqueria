package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoRequest;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoResponse;

import java.util.List;

public interface TurnoService
{
    TurnoResponse crearTurno(TurnoRequest dto);
    List<TurnoResponse> obtenerTodos ();
    TurnoResponse obtenerPorId (Long id);
    TurnoResponse actualizarTurno(Long id, TurnoRequest dto);
     void eliminarUnTurno (Long id);
}
