package com.example.ProyectoPeluqueria.Features.Comercio.Dtos;


import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionResponse;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioResponse;
import lombok.Data;

import java.util.List;


@Data
public class ComercioResponse
{
    private Long idComercio;

    private String nombre;

    private String direccion;

    private String telefono;

    private String descripcion;

    private List<String> categorias;

    private List<ServicioResponse> servicios;

    private List<HorarioDeAtencionResponse> horarios;
    
    private Long usuarioPropietarioId;
}
