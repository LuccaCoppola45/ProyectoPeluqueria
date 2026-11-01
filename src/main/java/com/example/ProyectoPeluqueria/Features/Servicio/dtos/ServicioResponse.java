package com.example.ProyectoPeluqueria.Features.Servicio.dtos;

import lombok.Data;

@Data
public class ServicioResponse
{
    private Long idServicio;
    private String nombreServicio;
    private Integer duracionDelServicio;
    private double precio;
    private Double precioHasta;
    private String horariosFijos;
    private boolean deAyudante;
    private Long idComercio;

}
