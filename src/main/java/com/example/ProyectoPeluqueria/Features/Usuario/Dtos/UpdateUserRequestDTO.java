package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestDTO {

    @NotNull(message = "El nombre es obligatorio")
    @Size(min = 3)
    String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 3)
    String apellido;

    @NotNull(message = "El email es obligatorio")
    @Size(min = 3)
    String email;
}
