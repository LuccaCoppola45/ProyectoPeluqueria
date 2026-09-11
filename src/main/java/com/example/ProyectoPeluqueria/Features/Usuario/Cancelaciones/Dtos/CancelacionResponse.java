package com.example.ProyectoPeluqueria.Features.Usuario.Cancelaciones.Dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelacionResponse
{
    private Long idCancelacion;
    private LocalDateTime fechaCancelacion;
    private String Motivo;
    private long idUsuario;


}
