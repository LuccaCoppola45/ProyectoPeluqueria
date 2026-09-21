package com.example.ProyectoPeluqueria.Features.auth;

import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.LoginResponseDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.LoginResquestDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registrar(@Valid @RequestBody UsuarioRequest request) {
        // Status 201 (Created) es la buena práctica para cuando creamos algo nuevo
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginResquestDTO request) {
        // Status 200 (OK)
        return ResponseEntity.ok(authService.login(request));
    }
}
