package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarContrasenaRequest {

    // La pide cuando el cliente cambia la suya. El admin no la necesita (la resetea).
    private String contrasenaActual;

    @NotBlank(message = "La contraseña nueva es obligatoria")
    @Size(min = 6, max = 72, message = "La contraseña nueva tiene que tener entre 6 y 72 caracteres")
    private String contrasenaNueva;
}
