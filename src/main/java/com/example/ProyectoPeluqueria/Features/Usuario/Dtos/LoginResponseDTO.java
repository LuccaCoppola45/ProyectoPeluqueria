package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    boolean success;
    private String mensaje;
    private String token;
    private String rol;
}
