package com.example.ProyectoPeluqueria.Features.Usuario.Mapper;


import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioRequest;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class UsuarioMapper
{
    private final ModelMapper modelMapper;

    public UsuarioMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UsuarioEntity toEntity(UsuarioRequest dto)
    {
        // Configurar ModelMapper para este mapeo específico
        ModelMapper mapper = new ModelMapper();

        // Omitir campos que no existen en el request o que generan conflicto
        mapper.typeMap(UsuarioRequest.class, UsuarioEntity.class)
                .addMapping(src -> Rol.CLIENTE, UsuarioEntity::setRol) // Rol por defecto
                .addMapping(UsuarioRequest::getNombre, UsuarioEntity::setNombre)
                .addMapping(UsuarioRequest::getApellido, UsuarioEntity::setApellido)
                .addMapping(UsuarioRequest::getDni, UsuarioEntity::setDni)
                .addMapping(UsuarioRequest::getEmail, UsuarioEntity::setEmail)
                .addMapping(UsuarioRequest::getPassword, UsuarioEntity::setPassword);

        return mapper.map(dto, UsuarioEntity.class);
    }

    public UsuarioResponse toResponce(UsuarioEntity usuario)
    {
        UsuarioResponse dto = modelMapper.map(usuario, UsuarioResponse.class);

        if(usuario.getServiciosVinculados() != null){
            dto.setServiciosId(
                    usuario.getServiciosVinculados()
                            .stream()
                            .map(ServicioEntity::getIdServicio)
                            .collect(Collectors.toList())
            );
        }

        if (usuario.getTurnosVinculados() != null) {
            dto.setTurnosIds(
                    usuario.getTurnosVinculados()
                            .stream()
                            .map(t -> t.getIdTurno())
                            .collect(Collectors.toList())
            );
        }

        if (usuario.getCancelacionEntitiesVinculados() != null) {
            dto.setCancelacionesIds(
                    usuario.getCancelacionEntitiesVinculados()
                            .stream()
                            .map(c -> c.getId())
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }


}