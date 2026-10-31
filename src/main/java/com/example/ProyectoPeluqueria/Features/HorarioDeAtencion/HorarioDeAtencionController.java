package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionResponse;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoHorarioRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoHorarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoRangoRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoRangoResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioDeAtencionController {

    private final HorarioDeAtencionService horarioService;
    private final BloqueoHorarioService bloqueoService;

    @GetMapping("/bloqueos")
    public ResponseEntity<List<BloqueoHorarioResponse>> listarBloqueos(
            @RequestParam Long comercioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(bloqueoService.listar(comercioId, desde, hasta));
    }

    @PostMapping("/bloqueos")
    public ResponseEntity<BloqueoHorarioResponse> bloquearDia(
            @Valid @RequestBody BloqueoHorarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bloqueoService.bloquear(request.getComercioId(), request.getFecha()));
    }

    // Bloquea todos los días entre "desde" y "hasta" (inclusive), por ejemplo vacaciones
    @PostMapping("/bloqueos/rango")
    public ResponseEntity<BloqueoRangoResponse> bloquearRango(
            @Valid @RequestBody BloqueoRangoRequest request) {
        return ResponseEntity.ok(bloqueoService.bloquearRango(
                request.getComercioId(), request.getDesde(), request.getHasta()));
    }

    @DeleteMapping("/bloqueos/rango")
    public ResponseEntity<BloqueoRangoResponse> desbloquearRango(
            @RequestParam Long comercioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(bloqueoService.desbloquearRango(comercioId, desde, hasta));
    }

    @DeleteMapping("/bloqueos/{fecha}")
    public ResponseEntity<Void> desbloquearDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Long comercioId) {
        bloqueoService.desbloquear(comercioId, fecha);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<HorarioDeAtencionResponse> crear(
            @Valid @RequestBody HorarioDeAtencionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(horarioService.crearHorariotencion(request));
    }

    @GetMapping
    public ResponseEntity<List<HorarioDeAtencionResponse>> listar(
            @RequestParam(required = false) Long comercioId) {
        if (comercioId != null) {
            return ResponseEntity.ok(horarioService.listarHorariosPorComercio(comercioId));
        }
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
