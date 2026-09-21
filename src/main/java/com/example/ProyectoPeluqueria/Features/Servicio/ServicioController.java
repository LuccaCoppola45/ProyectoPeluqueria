package com.example.ProyectoPeluqueria.Features.Servicio;

import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioRequest;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioService.crearServicio(request));
    }

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> listar() {
        return ResponseEntity.ok(servicioService.listarServicios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> obtenerPorId(@PathVariable long id) {
        return ResponseEntity.ok(servicioService.BuscarporId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizar(
            @PathVariable long id,
            @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(servicioService.ActualizarServicio(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }
}
