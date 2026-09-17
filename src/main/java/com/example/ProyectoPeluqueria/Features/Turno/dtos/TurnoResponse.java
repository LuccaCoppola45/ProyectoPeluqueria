package com.example.ProyectoPeluqueria.Features.Turno.dtos;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse
{
    private Long idTurno;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoTurno estado;
    private Long usuarioId;
    private Long servicioId;
    private Long cancelacionId;
    private Long idComercio;


}
