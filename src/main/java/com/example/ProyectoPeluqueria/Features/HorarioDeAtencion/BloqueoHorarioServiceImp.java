package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoRangoResponse;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoRepository;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
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

    // Tope para no generar miles de filas por un error de tipeo en el año
    private static final long MAXIMO_DIAS_POR_RANGO = 366;

    private final BloqueoHorarioRepository repository;
    private final ComercioRepository comercioRepository;
    private final TurnoRepository turnoRepository;

    public BloqueoHorarioServiceImp(
            BloqueoHorarioRepository repository,
            ComercioRepository comercioRepository,
            TurnoRepository turnoRepository) {
        this.repository = repository;
        this.comercioRepository = comercioRepository;
        this.turnoRepository = turnoRepository;
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

    @Override
    @Transactional
    public BloqueoRangoResponse bloquearRango(Long comercioId, LocalDate desde, LocalDate hasta) {
        ComercioEntity comercio = validarComercio(comercioId);
        validarRango(desde, hasta);
        Set<LocalDate> yaBloqueados = repository
                .findByComercio_IdComercioAndFechaBetweenOrderByFechaAsc(comercioId, desde, hasta)
                .stream()
                .map(BloqueoHorarioEntity::getFecha)
                .collect(Collectors.toSet());
        List<BloqueoHorarioEntity> nuevos = new ArrayList<>();
        for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
            if (!yaBloqueados.contains(fecha)) {
                BloqueoHorarioEntity bloqueo = new BloqueoHorarioEntity();
                bloqueo.setFecha(fecha);
                bloqueo.setComercio(comercio);
                nuevos.add(bloqueo);
            }
        }
        repository.saveAll(nuevos);
        int dias = (int) ChronoUnit.DAYS.between(desde, hasta) + 1;
        return new BloqueoRangoResponse(desde, hasta, dias, contarTurnosActivos(comercioId, desde, hasta));
    }

    @Override
    @Transactional
    public BloqueoRangoResponse desbloquearRango(Long comercioId, LocalDate desde, LocalDate hasta) {
        validarComercio(comercioId);
        validarRango(desde, hasta);
        List<BloqueoHorarioEntity> existentes = repository
                .findByComercio_IdComercioAndFechaBetweenOrderByFechaAsc(comercioId, desde, hasta);
        repository.deleteAll(existentes);
        return new BloqueoRangoResponse(desde, hasta, existentes.size(), 0);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas desde y hasta son obligatorias");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) + 1 > MAXIMO_DIAS_POR_RANGO) {
            throw new IllegalArgumentException("El período no puede superar un año");
        }
    }

    private long contarTurnosActivos(Long comercioId, LocalDate desde, LocalDate hasta) {
        return turnoRepository.findByComercio_IdComercioAndFechaBetween(comercioId, desde, hasta).stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO && t.getEstado() != EstadoTurno.FINALIZADO)
                .count();
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
