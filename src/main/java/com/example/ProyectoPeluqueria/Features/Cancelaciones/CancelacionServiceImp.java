package com.example.ProyectoPeluqueria.Features.Cancelaciones;

import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.TurnoAlreadyCancelledException;
import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.TurnoNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.TurnoPastDateException;
import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.UnauthorizedTurnoAccessException;
import com.example.ProyectoPeluqueria.Exceptions.cancelacion.CancelacionNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserProfileNotFoundException;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionRequest;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionResponse;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Mapper.CancelacionMapper;
import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoRepository;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor



public class CancelacionServiceImp implements CancelacionService {

    private final CancelacionRepository cancelacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final CancelacionMapper cancelacionMapper;


    @Override
    public CancelacionResponse cancelarTurno(CancelacionRequest cancelacionRequest) {
        UsuarioEntity usuarioAutenticado = obtenerUsuarioAutenticado();

        TurnoEntity turno = turnoRepository.findById(cancelacionRequest.getIdTurno())
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado con ID: " + cancelacionRequest.getIdTurno()));

        // 1. ¿El turno le pertenece al usuario que lo quiere cancelar?
        if (!esAdmin(usuarioAutenticado)
                && !turno.getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario())) {
            throw new UnauthorizedTurnoAccessException("No tienes permiso para cancelar un turno que no te pertenece.");
        }

        // 2. Verifica que el turno no sea en el pasado
        LocalDateTime fechaHoraDelTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());

        if (fechaHoraDelTurno.isBefore(LocalDateTime.now())) {
            throw new TurnoPastDateException("No se puede cancelar un turno cuya fecha y hora ya pasaron.");
        }

        // 3. Verifica estado actual del turno usando tu Enum
        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new TurnoAlreadyCancelledException("Este turno ya se encuentra cancelado.");
        }

        if (turno.getEstado() == EstadoTurno.FINALIZADO) {
            throw new TurnoPastDateException("No se puede cancelar un turno finalizado.");
        }

        CancelacionEntity cancelacionEntity = cancelacionMapper.requestDtoToEntity(cancelacionRequest);


        // El actor que cancela siempre proviene de la identidad autenticada.
        cancelacionEntity.setUsuario(usuarioAutenticado);
        cancelacionEntity.setTurno(turno);
        cancelacionEntity.setFechaCancelacion(LocalDateTime.now());

        turno.setEstado(EstadoTurno.CANCELADO); // Acá le pasamos la constante del Enum
        turnoRepository.save(turno);
        // 4. PERSISTENCIA
        CancelacionEntity guardada = cancelacionRepository.save(cancelacionEntity);

        // 5. USO DEL MAPPER (Entidad -> ResponseDTO)
        return cancelacionMapper.entityToResponseDto(guardada);    }

    @Override
    public List<CancelacionResponse> listarCancelaciones() {
        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        List<CancelacionEntity> cancelaciones = esAdmin(usuario)
                ? cancelacionRepository.findAll()
                : cancelacionRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

        return cancelaciones.stream()
                .map(cancelacionMapper::entityToResponseDto)
                .toList();
    }

    @Override
    public CancelacionResponse obtenerCancelacionPorId(Long id) {
        CancelacionEntity cancelacion = cancelacionRepository.findById(id).
                orElseThrow(()->new CancelacionNotFoundException("Cancelacion no encontrada con ID: " + id));
        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        if (!esAdmin(usuario)
                && !cancelacion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new UnauthorizedTurnoAccessException("No tienes permiso para consultar esta cancelación.");
        }
        return cancelacionMapper.entityToResponseDto(cancelacion);
    }

    private UsuarioEntity obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedTurnoAccessException("Se requiere un usuario autenticado.");
        }
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserProfileNotFoundException("Usuario autenticado no encontrado"));
    }

    private boolean esAdmin(UsuarioEntity usuario) {
        return usuario.getRol() == Rol.ADMIN;
    }
}
