package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioDeAtencionController {

    private final HorarioDeAtencionService horarioService;

    @PostMapping
    public ResponseEntity<HorarioDeAtencionResponse> crear(
            @Valid @RequestBody HorarioDeAtencionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(horarioService.crearHorariotencion(request));
    }

    @GetMapping
    public ResponseEntity<List<HorarioDeAtencionResponse>> listar() {
        return ResponseEntity.ok(horarioService.ListarHorarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioDeAtencionResponse> obtenerPorId(@PathVariable long id) {
        return ResponseEntity.ok(horarioService.ObtenerHorarioPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioDeAtencionResponse> actualizar(
            @PathVariable long id,
            @Valid @RequestBody HorarioDeAtencionRequest request) {
        return ResponseEntity.ok(
                horarioService.ActualizarHorarioAtencion(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        horarioService.EliminarHorarioAtencion(id);
        return ResponseEntity.noContent().build();
    }
}
