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
public class LoginResquestDTO {

    @NotBlank(message = "El email es obligatorio")
    @Size(min = 3)
    private String email;

    @NotBlank(message = "la contraseña es obligatoria")
    @Size(min = 6)
    private String password;


}
