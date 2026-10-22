package com.example.ProyectoPeluqueria.Features.Resenia;

import com.example.ProyectoPeluqueria.Features.Resenia.dtos.ReseniaRequest;
import com.example.ProyectoPeluqueria.Features.Resenia.dtos.ReseniaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenias")
@RequiredArgsConstructor
public class ReseniaController {

    private final ReseniaService reseniaService;

    @GetMapping
    public ResponseEntity<List<ReseniaResponse>> listar(
            @RequestParam(required = false) Long comercioId) {
        return ResponseEntity.ok(reseniaService.listar(comercioId));
    }

    @PostMapping
    public ResponseEntity<ReseniaResponse> crear(
            @Valid @RequestBody ReseniaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reseniaService.crear(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reseniaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
