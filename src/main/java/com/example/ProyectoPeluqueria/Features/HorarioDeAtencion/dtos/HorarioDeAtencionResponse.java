package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos;

import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioDeAtencionResponse
{
    private Long idHorarioAtencion;
    private Long comercioId; // ID del comercio asociado
    private DiaSemana diaSemana;
    private LocalTime horaApertura;
    private LocalTime horaCierre;


}
