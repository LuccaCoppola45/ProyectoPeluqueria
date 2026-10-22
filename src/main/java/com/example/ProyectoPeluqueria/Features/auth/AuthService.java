package com.example.ProyectoPeluqueria.Features.auth;

import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.LoginResponseDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.LoginResquestDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioRequest;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import com.example.ProyectoPeluqueria.Exceptions.user.UserDniAlreadyInUseException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserEmailAlreadyInUseException;
import com.example.ProyectoPeluqueria.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // 1. Lógica de Registro
    public String registrar(UsuarioRequest request) {
        String dni = request.getDni().trim();
        if (usuarioRepository.existsByDni(dni)) {
            throw new UserDniAlreadyInUseException("El DNI ya está registrado");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setDni(dni);
        usuario.setEmail(normalizarEmail(request.getEmail()));
        usuario.setTelefono(request.getTelefono());

        // ¡SÚPER IMPORTANTE! Encriptamos la contraseña
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        usuarioRepository.save(usuario);

        return "Usuario registrado con éxito. Por favor, inicie sesión.";
    }

    // 2. Lógica de Inicio de Sesión (Login)
    public LoginResponseDTO login(LoginResquestDTO request) {
        String identifier = request.getDni() != null && !request.getDni().isBlank()
                ? request.getDni().trim()
                : request.getEmail().trim().toLowerCase();

        // El AuthenticationManager de Spring Security compara la contraseña encriptada por nosotros
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        identifier,
                        request.getPassword()
                )
        );

        // Si pasa la línea anterior, las credenciales están bien. Buscamos al usuario:
        UsuarioEntity usuario = usuarioRepository.findByDniOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // JwtService recibe UserDetails, no directamente UsuarioEntity.
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(identifier);
        String jwtToken = jwtService.generateToken(userDetails);

        // Devolvemos tu DTO armado
        return new LoginResponseDTO(
                true,
                "Inicio de sesión exitoso",
                jwtToken,
                usuario.getRol().name());
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new UserEmailAlreadyInUseException("El email ya está registrado");
        }
        return emailNormalizado;
    }
}
