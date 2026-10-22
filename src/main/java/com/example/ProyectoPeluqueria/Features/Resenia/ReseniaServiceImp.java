package com.example.ProyectoPeluqueria.Features.Resenia;

import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserProfileNotFoundException;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioRepository;
import com.example.ProyectoPeluqueria.Features.Resenia.dtos.ReseniaRequest;
import com.example.ProyectoPeluqueria.Features.Resenia.dtos.ReseniaResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ReseniaServiceImp implements ReseniaService {

    private final ReseniaRepository reseniaRepository;
    private final ComercioRepository comercioRepository;
    private final UsuarioRepository usuarioRepository;

    public ReseniaServiceImp(
            ReseniaRepository reseniaRepository,
            ComercioRepository comercioRepository,
            UsuarioRepository usuarioRepository) {
        this.reseniaRepository = reseniaRepository;
        this.comercioRepository = comercioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<ReseniaResponse> listar(Long comercioId) {
        List<ReseniaEntity> resenias = comercioId == null
                ? reseniaRepository.findAll()
                : reseniaRepository.findByComercio_IdComercioOrderByFechaCreacionDesc(comercioId);
        return resenias.stream().map(this::toResponse).toList();
    }

    @Override
    public ReseniaResponse crear(ReseniaRequest request) {
        ComercioEntity comercio = comercioRepository.findById(request.getComercioId())
                .orElseThrow(() -> new ComercioNotFoundException(
                        "Comercio no encontrado con ID: " + request.getComercioId()));

        ReseniaEntity resenia = new ReseniaEntity();
        resenia.setValoracion(request.getValoracion());
        resenia.setComentario(request.getComentario());
        resenia.setComercio(comercio);
        resenia.setUsuario(obtenerUsuarioAutenticado());
        return toResponse(reseniaRepository.save(resenia));
    }

    @Override
    public void eliminar(Long id) {
        ReseniaEntity resenia = reseniaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));
        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        if (!usuario.getRol().name().equals("ADMIN")
                && !resenia.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new IllegalArgumentException("No tenés permisos para eliminar esta reseña");
        }
        reseniaRepository.delete(resenia);
    }

    private UsuarioEntity obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UserProfileNotFoundException("No hay usuario autenticado");
        }
        return usuarioRepository.findByDniOrEmail(authentication.getName(), authentication.getName())
                .orElseThrow(() -> new UserProfileNotFoundException(
                        "Usuario autenticado no encontrado"));
    }

    private ReseniaResponse toResponse(ReseniaEntity entity) {
        ReseniaResponse response = new ReseniaResponse();
        response.setId(entity.getId());
        response.setValoracion(entity.getValoracion());
        response.setComentario(entity.getComentario());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setComercioId(entity.getComercio().getIdComercio());
        response.setUsuarioId(entity.getUsuario().getIdUsuario());
        response.setNombreUsuario(entity.getUsuario().getNombre());
        response.setApellidoUsuario(entity.getUsuario().getApellido());
        return response;
    }
}
