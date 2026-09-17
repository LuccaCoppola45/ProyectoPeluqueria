package com.example.ProyectoPeluqueria.Features.Cancelaciones.Mapper;


import com.example.ProyectoPeluqueria.Features.Cancelaciones.CancelacionEntity;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionRequest;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class CancelacionMapper
{
    private final ModelMapper modelMapper = new ModelMapper();

    public CancelacionEntity requestDtoToEntity(CancelacionRequest requestDto) {
        if (requestDto == null) return null;
        CancelacionEntity entity = new CancelacionEntity();
        entity.setMotivo(requestDto.getMotivo());

        return entity;
    }

    public CancelacionResponse entityToResponseDto(CancelacionEntity entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, CancelacionResponse.class);
    }
}
