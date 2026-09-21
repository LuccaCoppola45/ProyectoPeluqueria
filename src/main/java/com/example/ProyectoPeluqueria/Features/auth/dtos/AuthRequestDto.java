package com.example.ProyectoPeluqueria.Features.auth.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequestDto
{
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 3)
    private String apellido;

    @NotBlank(message =  "el email es obligatorio")
    @Email(message = "el email debe cumplir con el formato estandar")
    private  String email;

    @NotBlank(message = "la contraseña es obligatoria")
    @Size(min = 6)
    private String password;
}
