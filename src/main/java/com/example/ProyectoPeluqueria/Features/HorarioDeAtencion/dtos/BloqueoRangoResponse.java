package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BloqueoRangoResponse {
    private LocalDate desde;
    private LocalDate hasta;
    // Cantidad de días que quedaron bloqueados (o desbloqueados) en el período
    private int dias;
    // Turnos ya reservados en esas fechas: el bloqueo no los cancela, hay que avisarle al cliente
    private long turnosExistentes;
}
