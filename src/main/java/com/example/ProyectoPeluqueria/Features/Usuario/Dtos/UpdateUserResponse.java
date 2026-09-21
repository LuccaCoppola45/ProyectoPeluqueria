package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserResponse {

    boolean success;
    String message;
    UsuarioResponse data;
}
