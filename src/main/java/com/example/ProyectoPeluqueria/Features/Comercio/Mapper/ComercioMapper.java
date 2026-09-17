package com.example.ProyectoPeluqueria.Features.Comercio.Mapper;

import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioRequest;
import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioResponse;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.Mapper.HorarioAtencionMapper;
import com.example.ProyectoPeluqueria.Features.Servicio.Mapper.ServicioMapper;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComercioMapper {

    private final ServicioMapper servicioMapper;
    private final HorarioAtencionMapper horarioMapper;

    public ComercioMapper(ServicioMapper servicioMapper,
                          HorarioAtencionMapper horarioMapper) {
        this.servicioMapper = servicioMapper;
        this.horarioMapper = horarioMapper;
    }

    public ComercioResponse toResponse(ComercioEntity entity) {
        if (entity == null) {
            return null;
        }

        ComercioResponse response = new ComercioResponse();
        response.setIdComercio(entity.getIdComercio());
        response.setNombre(entity.getNombre());
        response.setDireccion(entity.getDireccion());
        response.setTelefono(entity.getTelefono());
        response.setDescripcion(entity.getDescripcion());

        if (entity.getServicios() != null) {
            response.setServicios(entity.getServicios()
                    .stream()
                    .map(servicioMapper::toDto)
                    .toList());
        } else {
            response.setServicios(List.of());
        }

        if (entity.getHorarios() != null) {
            response.setHorarios(entity.getHorarios()
                    .stream()
                    .map(horario -> {
                        var horarioResponse = horarioMapper.toDto(horario);
                        if (horarioResponse != null) {
                            horarioResponse.setComercioId(entity.getIdComercio());
                        }
                        return horarioResponse;
                    })
                    .toList());
        } else {
            response.setHorarios(List.of());
        }

        return response;
    }

    public ComercioEntity toEntity(ComercioRequest request) {
        if (request == null) {
            return null;
        }

        ComercioEntity entity = new ComercioEntity();
        entity.setNombre(request.getNombre());
        entity.setDireccion(request.getDireccion());
        entity.setTelefono(request.getTelefono());

        // si en tu DB descripcion es NOT NULL, esto te faltaba
        entity.setDescripcion("Sin descripción");

        return entity;
    }

    public void updateEntity(ComercioRequest request, ComercioEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setNombre(request.getNombre());
        entity.setDireccion(request.getDireccion());
        entity.setTelefono(request.getTelefono());

        // si agregás descripcion a request:
        // entity.setDescripcion(request.getDescripcion());
    }
}