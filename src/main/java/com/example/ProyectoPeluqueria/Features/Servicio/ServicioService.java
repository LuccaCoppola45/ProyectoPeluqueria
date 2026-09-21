package com.example.ProyectoPeluqueria.Features.Servicio;

import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioRequest;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioResponse;

import java.util.List;

public interface ServicioService {
    ServicioResponse crearServicio(ServicioRequest request);

    List<ServicioResponse> listarServicios();

    ServicioResponse  BuscarporId(long id);

    void eliminarServicio(long id);

    ServicioResponse ActualizarServicio(long id, ServicioRequest request);
}
