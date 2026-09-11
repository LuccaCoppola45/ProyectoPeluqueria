package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos;

import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioDeAtencionRequest
{
    @NotNull(message = "El ID del comercio es obligatorio")
    @Positive(message = "El ID del comercio debe ser mayor a cero")
    private Long comercioId;

    @NotNull(message = "El día es obligatorio")
    private DiaSemana diaSemana;;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime horaApertura;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime horaCierre;
}
