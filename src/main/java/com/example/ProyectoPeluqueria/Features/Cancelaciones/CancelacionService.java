package com.example.ProyectoPeluqueria.Features.Cancelaciones;

import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionRequest;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionResponse;

import java.util.List;

public interface CancelacionService {
    CancelacionResponse cancelarTurno(CancelacionRequest cancelacionRequest);
    //GET
    List<CancelacionResponse> listarCancelaciones();
    //GET id
    CancelacionResponse obtenerCancelacionPorId(Long id);
}
