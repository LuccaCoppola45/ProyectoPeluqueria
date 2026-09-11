package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;

import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse
{
    private Long idUsuario;
    private String nombre;
    private String apellido;
    private Rol rol;
    private String email;
}
