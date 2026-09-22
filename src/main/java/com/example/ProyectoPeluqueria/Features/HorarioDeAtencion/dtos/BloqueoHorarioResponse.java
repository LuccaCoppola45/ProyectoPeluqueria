package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BloqueoHorarioResponse {

    private Long id;
    private LocalDate fecha;
    private Long comercioId;
}
