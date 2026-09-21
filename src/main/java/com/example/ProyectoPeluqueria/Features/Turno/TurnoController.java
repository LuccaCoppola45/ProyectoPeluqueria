package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoRequest;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping
    public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(turnoService.crearTurno(request));
    }

    @GetMapping
    public ResponseEntity<List<TurnoResponse>> listar() {
        return ResponseEntity.ok(turnoService.obtenerTodos());
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Long servicioId) {
        return ResponseEntity.ok(turnoService.obtenerDisponibilidad(fecha, servicioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.ok(turnoService.actualizarTurno(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        turnoService.eliminarUnTurno(id);
        return ResponseEntity.noContent().build();
    }
}
