package com.example.ProyectoPeluqueria.Features.Usuario.Mapper;


import com.example.ProyectoPeluqueria.Features.Enums.Rol;
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
        // Solo los datos del cliente. Antes también se cargaban todos sus turnos y cancelaciones
        // (una consulta por cliente y otra por turno) para una lista de ids que la página no usa,
        // y eso hacía tardar mucho el panel.
        UsuarioResponse dto = new UsuarioResponse();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setRol(usuario.getRol());
        dto.setEmail(usuario.getEmail());
        dto.setDni(usuario.getDni());
        return dto;
    }


}