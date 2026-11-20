package com.example.ProyectoPeluqueria.Features.Usuario;

import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.CambiarContrasenaRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CambiarContrasenaTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UsuarioServiceImp servicio = new UsuarioServiceImp(
            usuarioRepository, null, null, encoder, null);

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setIdUsuario(7L);
        usuario.setDni("30111222");
        usuario.setPassword(encoder.encode("vieja123"));
        when(usuarioRepository.findByDniOrEmail("30111222", "30111222")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("30111222", null, java.util.List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void elClienteCambiaSuContrasenaConLaActualCorrecta() {
        servicio.cambiarMiContrasena(new CambiarContrasenaRequest("vieja123", "nueva456"));

        assertTrue(encoder.matches("nueva456", usuario.getPassword()));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void conLaContrasenaActualMalNoCambiaNada() {
        String antes = usuario.getPassword();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.cambiarMiContrasena(new CambiarContrasenaRequest("otra", "nueva456")));

        assertEquals("La contraseña actual no es correcta", error.getMessage());
        assertEquals(antes, usuario.getPassword());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void elAdminLePoneUnaNuevaSinSaberLaAnterior() {
        servicio.cambiarContrasenaDeUsuario(7L, new CambiarContrasenaRequest(null, "reset789"));

        assertTrue(encoder.matches("reset789", usuario.getPassword()));
        // Se guarda cifrada, nunca en texto plano
        assertTrue(usuario.getPassword().startsWith("$2"));
    }
}
