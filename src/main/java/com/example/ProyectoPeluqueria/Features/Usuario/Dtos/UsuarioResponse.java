package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;

import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


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

    private List<Long> serviciosId;
    private List<Long>turnosIds;
    private List<Long>cancelacionesIds;

}
