package com.example.ProyectoPeluqueria.Features.Comercio;

import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioRequest;
import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComercioController {

    private final ComercioService comercioService;

    @PostMapping
    public ResponseEntity<ComercioResponse> crear(@Valid @RequestBody ComercioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comercioService.crearComercio(request));
    }

    @GetMapping
    public ResponseEntity<ComercioResponse> obtenerInfo() {
        return ResponseEntity.ok(comercioService.ObtenerInfo());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComercioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComercioRequest request) {
        return ResponseEntity.ok(comercioService.actualizarComercio(id, request));
    }
}
