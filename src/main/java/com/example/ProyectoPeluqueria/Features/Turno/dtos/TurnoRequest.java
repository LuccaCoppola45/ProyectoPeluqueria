package com.example.ProyectoPeluqueria.Features.Turno.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class TurnoRequest
{
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    // Solo ADMIN lo usa para reservar en nombre de otro usuario.
    @Positive(message = "El id del usuario debe ser positivo")
    private Long usuarioId;

    // Solo ADMIN: turno para alguien sin cuenta, con el nombre que quiera (en vez de usuarioId).
    @Size(max = 80, message = "El nombre puede tener hasta 80 caracteres")
    private String nombreCliente;

    @NotNull(message = "El id del servicio es obligatorio")
    @Positive(message = "El id del servicio debe ser positivo")
    private Long servicioId;

    @NotNull (message = "El id del comercio es obligatorio")
    @Positive(message = "El id del comercio debe ser positivo")
    private Long idComercio;
}
