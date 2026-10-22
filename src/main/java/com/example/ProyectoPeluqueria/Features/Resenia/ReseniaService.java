package com.example.ProyectoPeluqueria.Features.Resenia;

import com.example.ProyectoPeluqueria.Features.Resenia.dtos.ReseniaRequest;
import com.example.ProyectoPeluqueria.Features.Resenia.dtos.ReseniaResponse;

import java.util.List;

public interface ReseniaService {

    List<ReseniaResponse> listar(Long comercioId);

    ReseniaResponse crear(ReseniaRequest request);

    void eliminar(Long id);
}
