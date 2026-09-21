package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionResponse;

import java.util.List;

public interface HorarioDeAtencionService {
    HorarioDeAtencionResponse crearHorariotencion(HorarioDeAtencionRequest request);

    List<HorarioDeAtencionResponse> ListarHorarios();

    List<HorarioDeAtencionResponse> listarHorariosPorComercio(Long comercioId);

    void EliminarHorarioAtencion(long id);

    HorarioDeAtencionResponse ActualizarHorarioAtencion(long id, HorarioDeAtencionRequest request);

    HorarioDeAtencionResponse ObtenerHorarioPorId(long id);
}
