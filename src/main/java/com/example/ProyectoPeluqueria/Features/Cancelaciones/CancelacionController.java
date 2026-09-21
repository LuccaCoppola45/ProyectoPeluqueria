package com.example.ProyectoPeluqueria.Features.Cancelaciones;

import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionRequest;
import com.example.ProyectoPeluqueria.Features.Cancelaciones.Dtos.CancelacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancelaciones")
@RequiredArgsConstructor
public class CancelacionController {

    private final CancelacionService cancelacionService;

    @PostMapping
    public ResponseEntity<CancelacionResponse> cancelar(
            @Valid @RequestBody CancelacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cancelacionService.cancelarTurno(request));
    }

    @GetMapping
    public ResponseEntity<List<CancelacionResponse>> listar() {
        return ResponseEntity.ok(cancelacionService.listarCancelaciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CancelacionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cancelacionService.obtenerCancelacionPorId(id));
    }
}
