package com.example.ProyectoPeluqueria.Features.Servicio.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ServicioRequest {

    @NotBlank(message = "El nombre del servicio no puede estar vacio")
    private String nombreServicio;

    @Positive(message = "El precio debe ser un numero positivo")
    @NotNull(message = "El precio es obligatorio")
    private double precio;


    @NotNull(message = "La duracion del servicio es obligatorio")
    private Integer duracionDelServicio;


    private Long idComercio;


}
