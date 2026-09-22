package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BloqueoHorarioRequest {

    @NotNull(message = "El comercio es obligatorio")
    private Long comercioId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;
}
