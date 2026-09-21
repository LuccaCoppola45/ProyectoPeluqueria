package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.Mapper;


import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.HorarioDeAtencionEntity;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class HorarioAtencionMapper
{
    private final ModelMapper modelMapper;

    public HorarioAtencionMapper(ModelMapper modelMapper)
    {
        this.modelMapper = modelMapper;
    }

    public HorarioDeAtencionEntity toEntity(HorarioDeAtencionRequest dto)
    {
        return modelMapper.map(dto, HorarioDeAtencionEntity.class);
    }

    public HorarioDeAtencionResponse toDto(HorarioDeAtencionEntity entity)
    {
        HorarioDeAtencionResponse response = modelMapper.map(entity, HorarioDeAtencionResponse.class);
        if (entity.getComercio() != null) {
            response.setComercioId(entity.getComercio().getIdComercio());
        }
        return response;
    }
}
