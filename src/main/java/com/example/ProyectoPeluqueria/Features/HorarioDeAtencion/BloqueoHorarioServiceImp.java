package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioNotFoundException;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioRepository;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoHorarioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BloqueoHorarioServiceImp implements BloqueoHorarioService {

    private final BloqueoHorarioRepository repository;
    private final ComercioRepository comercioRepository;

    public BloqueoHorarioServiceImp(
            BloqueoHorarioRepository repository,
            ComercioRepository comercioRepository) {
        this.repository = repository;
        this.comercioRepository = comercioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloqueoHorarioResponse> listar(Long comercioId, LocalDate desde, LocalDate hasta) {
        validarComercio(comercioId);
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("El rango de fechas no es válido");
        }
        return repository.findByComercio_IdComercioAndFechaBetweenOrderByFechaAsc(
                        comercioId, desde, hasta).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BloqueoHorarioResponse bloquear(Long comercioId, LocalDate fecha) {
        ComercioEntity comercio = validarComercio(comercioId);
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        BloqueoHorarioEntity bloqueo = repository
                .findByComercio_IdComercioAndFecha(comercioId, fecha)
                .orElseGet(() -> {
                    BloqueoHorarioEntity nuevo = new BloqueoHorarioEntity();
                    nuevo.setFecha(fecha);
                    nuevo.setComercio(comercio);
                    return repository.save(nuevo);
                });
        return toResponse(bloqueo);
    }

    @Override
    @Transactional
    public void desbloquear(Long comercioId, LocalDate fecha) {
        validarComercio(comercioId);
        BloqueoHorarioEntity bloqueo = repository
                .findByComercio_IdComercioAndFecha(comercioId, fecha)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un bloqueo para la fecha " + fecha));
        repository.delete(bloqueo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaBloqueado(Long comercioId, LocalDate fecha) {
        return fecha != null && comercioId != null
                && repository.existsByComercio_IdComercioAndFecha(comercioId, fecha);
    }

    private BloqueoHorarioResponse toResponse(BloqueoHorarioEntity entity) {
        return new BloqueoHorarioResponse(
                entity.getId(),
                entity.getFecha(),
                entity.getComercio().getIdComercio());
    }

    private ComercioEntity validarComercio(Long comercioId) {
        if (comercioId == null || comercioId <= 0) {
            throw new IllegalArgumentException("El ID del comercio debe ser positivo");
        }
        return comercioRepository.findById(comercioId)
                .orElseThrow(() -> new ComercioNotFoundException(
                        "Comercio no encontrado con ID: " + comercioId));
    }
}
