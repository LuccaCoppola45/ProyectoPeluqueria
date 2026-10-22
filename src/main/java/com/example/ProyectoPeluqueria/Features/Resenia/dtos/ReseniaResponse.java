package com.example.ProyectoPeluqueria.Features.Resenia.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReseniaResponse {

    private Long id;
    private Integer valoracion;
    private String comentario;
    private LocalDateTime fechaCreacion;
    private Long comercioId;
    private Long usuarioId;
    private String nombreUsuario;
    private String apellidoUsuario;
}
