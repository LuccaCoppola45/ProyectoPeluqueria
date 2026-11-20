package com.example.ProyectoPeluqueria.Features.Usuario;

import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UpdateUserRequestDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UpdateUserResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioRequest;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.CambiarContrasenaRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crearUsuario(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    // El cliente logueado cambia su propia contraseña
    @PutMapping("/mi-contrasena")
    public ResponseEntity<Void> cambiarMiContrasena(@Valid @RequestBody CambiarContrasenaRequest request) {
        usuarioService.cambiarMiContrasena(request);
        return ResponseEntity.noContent().build();
    }

    // El admin le pone una contraseña nueva a un cliente (por ejemplo, si se la olvidó)
    @PutMapping("/{id}/contrasena")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> cambiarContrasenaDeUsuario(
            @PathVariable Long id,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        usuarioService.cambiarContrasenaDeUsuario(id, request);
        return ResponseEntity.noContent().build();
    }

    // Con ?conDatos=true se borran también sus turnos, cancelaciones y reseñas (usuarios de prueba)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @RequestParam(defaultValue = "false") boolean conDatos) {
        if (conDatos) {
            usuarioService.eliminarUsuarioConDatos(id);
        } else {
            usuarioService.eliminarUsuario(id);
        }
        return ResponseEntity.noContent().build();
    }
}
