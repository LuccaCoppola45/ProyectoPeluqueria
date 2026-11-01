package com.example.ProyectoPeluqueria.Features.Servicio.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ServicioRequest {

    @NotBlank(message = "El nombre del servicio no puede estar vacio")
    private String nombreServicio;

    @Positive(message = "El precio debe ser un numero positivo")
    @NotNull(message = "El precio es obligatorio")
    private double precio;

    // Opcional: precio máximo aproximado (tiene que ser mayor o igual al precio)
    private Double precioHasta;

    // Opcional: horas fijas separadas por coma, ej. "09:00,14:00"
    private String horariosFijos;


    @NotNull(message = "La duracion del servicio es obligatorio")
    @Min(value = 1, message = "La duración debe ser mayor a cero")
    private Integer duracionDelServicio;

    @NotNull(message = "El comercio es obligatorio")
    @Positive(message = "El ID del comercio debe ser positivo")
    private Long idComercio;


}
