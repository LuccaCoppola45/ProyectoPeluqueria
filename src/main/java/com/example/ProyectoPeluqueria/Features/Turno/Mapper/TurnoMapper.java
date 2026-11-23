package com.example.ProyectoPeluqueria.Features.Turno.Mapper;

import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoRequest;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Component
public class TurnoMapper {

    private final ModelMapper modelMapper;

    /**     * Mapea DTO a entidad.     * NOTA: usuarioId ya NO viene en el DTO, así que solo mapeamos fecha, horaInicio, servicioId.     * El servicio asigna usuario y servicio manualmente.     */
    public TurnoEntity toEntity(TurnoRequest dto) {
        TurnoEntity entity = new TurnoEntity();
        entity.setFecha(dto.getFecha());
        entity.setHoraInicio(dto.getHoraInicio());
        // NO mapeamos usuarioId porque ya no existe en el DTO
        // NO mapeamos servicioId porque el servicio lo asigna manualmente
        // NO mapeamos estado porque se asigna en el servicio (PENDIENTE)
        return entity;
    }

    /**     * Mapea entidad a DTO.     * Extrae los IDs de las relaciones (usuario, servicio, cancelacion).     */
    public TurnoResponse toDto(TurnoEntity entity) {
        // A mano (sin ModelMapper) para no recorrer relaciones de más
        TurnoResponse dto = new TurnoResponse();
        dto.setIdTurno(entity.getIdTurno());
        dto.setFecha(entity.getFecha());
        dto.setHoraInicio(entity.getHoraInicio());
        dto.setHoraFin(entity.getHoraFin());
        dto.setEstado(entity.getEstado());
        dto.setRespuestaCliente(entity.getRespuestaCliente());
        if (entity.getComercio() != null) {
            dto.setIdComercio(entity.getComercio().getIdComercio());
        }
        dto.setConAyudante(entity.getServicio() != null
                && Boolean.TRUE.equals(entity.getServicio().getDeAyudante()));

        if (entity.getUsuario() != null) {
            dto.setUsuarioId(entity.getUsuario().getIdUsuario());
            dto.setNombreUsuario(entity.getUsuario().getNombre());
            dto.setApellidoUsuario(entity.getUsuario().getApellido());
        } else {
            // Turno sin cuenta: se muestra el nombre que puso el admin
            dto.setNombreUsuario(entity.getNombreCliente());
        }

        if (entity.getServicio() != null) {
            dto.setServicioId(entity.getServicio().getIdServicio());
        }

        if (entity.getCancelacion() != null) {
            dto.setCancelacionId(entity.getCancelacion().getId());
        }

        return dto;
    }
}
