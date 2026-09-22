package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoHorarioResponse;

import java.time.LocalDate;
import java.util.List;

public interface BloqueoHorarioService {

    List<BloqueoHorarioResponse> listar(Long comercioId, LocalDate desde, LocalDate hasta);

    BloqueoHorarioResponse bloquear(Long comercioId, LocalDate fecha);

    void desbloquear(Long comercioId, LocalDate fecha);

    boolean estaBloqueado(Long comercioId, LocalDate fecha);
}
