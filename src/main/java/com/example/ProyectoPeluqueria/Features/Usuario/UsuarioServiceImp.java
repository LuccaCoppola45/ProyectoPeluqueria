package com.example.ProyectoPeluqueria.Features.Usuario;


import com.example.ProyectoPeluqueria.Exceptions.user.*;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoRepository;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UpdateUserRequestDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UpdateUserResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioRequest;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.Mapper.UsuarioMapper;
import com.example.ProyectoPeluqueria.Exceptions.user.UserDniAlreadyInUseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UsuarioServiceImp implements UsuarioService {


    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final TurnoRepository turnoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest dto) {
        String dni = dto.getDni().trim();
        if (usuarioRepository.existsByDni(dni)) {
            throw new UserDniAlreadyInUseException("Ya existe un usuario con ese DNI");
        }
        UsuarioEntity usuario = usuarioMapper.toEntity(dto);
        usuario.setDni(dni);
        String email = normalizarEmail(dto.getEmail());
        if (email != null && usuarioRepository.existsByEmail(email)) {
            throw new UserEmailAlreadyInUseException("Ya existe un usuario con ese email");
        }
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        UsuarioEntity guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponce(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toResponce)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new UserProfileNotFoundException("Usuario no encontrado"));
        return usuarioMapper.toResponce(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if(!usuarioRepository.existsById(id))
        {
            throw new UserProfileNotFoundException("Usuario no encontrado");
        }
        if(turnoRepository.existsByUsuario_IdUsuario(id))
        {
            throw new UserHasAssociatedTurnosException(
                    "No se puede eliminar un usuario con turnos asociados");
        }

        usuarioRepository.deleteById(id);

    }

    @Override
    @Transactional
    public UpdateUserResponse actualizarUsuario(Long id, UpdateUserRequestDTO dto) {
        // Obtener usuario autenticado
        String identificadorAutenticado = obtenerIdentificadorAutenticado();
        UsuarioEntity usuarioAutenticado = usuarioRepository.findByDniOrEmail(
                        identificadorAutenticado, identificadorAutenticado)
                .orElseThrow(() -> new UserProfileNotFoundException("Usuario autenticado no encontrado"));

        // VALIDACIÓN: Solo puede actualizar su propia cuenta
        if (!usuarioAutenticado.getIdUsuario().equals(id)) {
            throw new UnauthorizedUserAccessException("No puedes actualizar la información de otro usuario");
        }

        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserProfileNotFoundException("Usuario no encontrado"));

        String nuevoEmail = normalizarEmail(dto.getEmail());

        if (!Objects.equals(user.getEmail(), nuevoEmail)
                && nuevoEmail != null && usuarioRepository.existsByEmail(nuevoEmail)) {
            throw new UserEmailAlreadyInUseException("Ya existe un usuario con ese email");
        }

        if (user.getNombre().equals(dto.getNombre())
                && user.getApellido().equals(dto.getApellido())
                && Objects.equals(user.getEmail(), nuevoEmail)) {
            throw new UserWithoutChangesException("No se detectaron cambios para actualizar");
        }

        user.setNombre(dto.getNombre());
        user.setApellido(dto.getApellido());
        user.setEmail(nuevoEmail);

        UsuarioEntity usuarioActualizado = usuarioRepository.save(user);

        return UpdateUserResponse.builder()
                .success(true)
                .message("Usuario actualizado correctamente")
                .data(usuarioMapper.toResponce(usuarioActualizado))
                .build();
    }

    private String obtenerIdentificadorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UserProfileNotFoundException("No hay un usuario autenticado");
        }
        return auth.getName();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
