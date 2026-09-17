package com.example.ProyectoPeluqueria.Features.Servicio.Mapper;


import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioRequest;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ServicioMapper {

    // saque el atributo global 'modelMapper' si ya no lo usamos en los métodos.
    public ServicioMapper(ModelMapper modelMapper) {
        // Dejamos el constructor configurando el objeto localmente para evitar el warning.
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    }


    public ServicioEntity toEntity(ServicioRequest request) {

        if (request == null) {
            return null;
        }

        ServicioEntity entity = new ServicioEntity();

        // Asignamos a mano y cruzamos los nombres que son distintos
        entity.setNombre(request.getNombreServicio());
        entity.setPrecio(request.getPrecio());
        entity.setDuracionDelServicio(request.getDuracionDelServicio());

        return entity;
    }

    public ServicioResponse toDto(ServicioEntity entity) {
        if (entity == null) {
            return null;
        }

        ServicioResponse response = new ServicioResponse();

        // Mapeo de propiedades básicas
        response.setIdServicio(entity.getIdServicio());
        response.setNombreServicio(entity.getNombre());
        response.setPrecio(entity.getPrecio());
        response.setDuracionDelServicio(entity.getDuracionDelServicio());

        // Mapeo de comercio ID
        if (entity.getComercio() != null) {
            response.setIdComercio(entity.getComercio().getIdComercio());
        }


        return response;
    }
}
