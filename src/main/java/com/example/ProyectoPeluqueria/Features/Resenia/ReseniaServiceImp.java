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
import java.util.Optional;

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
        Long usuarioActualId = obtenerUsuarioAutenticadoSiHay().map(UsuarioEntity::getIdUsuario).orElse(null);
        return resenias.stream().map(resenia -> toResponse(resenia, usuarioActualId)).toList();
    }

    @Override
    public ReseniaResponse crear(ReseniaRequest request) {
        ComercioEntity comercio = comercioRepository.findById(request.getComercioId())
                .orElseThrow(() -> new ComercioNotFoundException(
                        "Comercio no encontrado con ID: " + request.getComercioId()));

        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        // Una sola reseña por cliente; si quiere cambiarla, borra la suya y deja otra
        if (reseniaRepository.existsByUsuario_IdUsuarioAndComercio_IdComercio(
                usuario.getIdUsuario(), comercio.getIdComercio())) {
            throw new IllegalStateException("Ya dejaste tu reseña. Solo se puede dejar una por persona.");
        }

        ReseniaEntity resenia = new ReseniaEntity();
        resenia.setValoracion(request.getValoracion());
        resenia.setComentario(request.getComentario());
        resenia.setComercio(comercio);
        resenia.setUsuario(usuario);
        return toResponse(reseniaRepository.save(resenia), usuario.getIdUsuario());
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
        return obtenerUsuarioAutenticadoSiHay()
                .orElseThrow(() -> new UserProfileNotFoundException("No hay usuario autenticado"));
    }

    // Las reseñas se listan sin sesión, así que acá no tener usuario no es un error
    private Optional<UsuarioEntity> obtenerUsuarioAutenticadoSiHay() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        return usuarioRepository.findByDniOrEmail(authentication.getName(), authentication.getName());
    }

    private ReseniaResponse toResponse(ReseniaEntity entity, Long usuarioActualId) {
        ReseniaResponse response = new ReseniaResponse();
        response.setId(entity.getId());
        response.setValoracion(entity.getValoracion());
        response.setComentario(entity.getComentario());
        response.setFechaCreacion(entity.getFechaCreacion());
        response.setComercioId(entity.getComercio().getIdComercio());
        response.setUsuarioId(entity.getUsuario().getIdUsuario());
        response.setNombreUsuario(entity.getUsuario().getNombre());
        response.setApellidoUsuario(entity.getUsuario().getApellido());
        response.setPropia(entity.getUsuario().getIdUsuario().equals(usuarioActualId));
        return response;
    }
}
