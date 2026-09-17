package com.example.ProyectoPeluqueria.Features.Comercio;

import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioRequest;
import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioResponse;

public interface ComercioService {
    ComercioResponse crearComercio(ComercioRequest request);
    ComercioResponse actualizarComercio(Long id, ComercioRequest request);
    ComercioResponse ObtenerInfo();


}
