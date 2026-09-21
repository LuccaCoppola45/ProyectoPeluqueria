package com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelacionRequest
{
    @NotNull(message = "El ID del turno es obligatorio")
    @Positive(message = "El ID del turno debe ser un número positivo mayor a 0")
    private Long idTurno;

    /*
     * Se conserva por compatibilidad con clientes antiguos, pero nunca se usa
     * para autorizar la operación. El usuario se obtiene del JWT.
     */
    private Long idUsuario;

    @NotBlank(message = "El motivo de la cancelacion no puede estar vacio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;

}
