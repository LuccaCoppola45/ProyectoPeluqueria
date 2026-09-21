package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.ResourcenotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.horarioAtencion.HorarioFueraDeComercioException;
import com.example.ProyectoPeluqueria.Exceptions.horarioAtencion.HorarioInvalidoException;
import com.example.ProyectoPeluqueria.Exceptions.horarioAtencion.HorarioSuperpuestoException;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioRepository;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.Mapper.HorarioAtencionMapper;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionRequest;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.HorarioDeAtencionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HorarioDeAtencionServiceImp implements HorarioDeAtencionService {

    private final HorarioDeAtencionRepository repository;
    private final HorarioAtencionMapper mapper;
    private final ComercioRepository comercioRepository;


    public HorarioDeAtencionServiceImp(HorarioDeAtencionRepository repository, HorarioAtencionMapper mapper, ComercioRepository comercioRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.comercioRepository = comercioRepository;

    }


    @Override
    public HorarioDeAtencionResponse crearHorariotencion(HorarioDeAtencionRequest request) {
        if (request.getHoraApertura().isAfter(request.getHoraCierre()) ||
                request.getHoraApertura().equals(request.getHoraCierre())) {
            throw new HorarioInvalidoException("Rango de horas inválido. Apertura debe ser anterior al cierre.");
        }

        // El comercio es OBLIGATORIO ahora (no hay alternativa con servicio)
        if (request.getComercioId() == null) {
            throw new HorarioFueraDeComercioException("Debes especificar un ID de comercio.");
        }

        ComercioEntity comercio = comercioRepository.findById(request.getComercioId())
                .orElseThrow(() -> new ComercioNotFoundException("Comercio no encontrado con ID: " + request.getComercioId()));

        // Validar solapamiento con OTRO HORARIO del mismo comercio para ese día
        boolean seSolapa = repository.findByComercio_IdComercioAndDiaSemana(comercio.getIdComercio(), request.getDiaSemana())
                .stream()
                .anyMatch(h -> request.getHoraApertura().isBefore(h.getHoraCierre()) &&
                        request.getHoraCierre().isAfter(h.getHoraApertura()));

        if (seSolapa) {
            throw new HorarioSuperpuestoException("Ya existe un horario para este comercio que se solapa en el día " + request.getDiaSemana());
        }

        // Mapeo y guardado
        HorarioDeAtencionEntity horarioEntity = mapper.toEntity(request);
        horarioEntity.setIdHorarioAtencion(null);
        horarioEntity.setComercio(comercio);

        HorarioDeAtencionEntity guardado = repository.save(horarioEntity);
        return mapper.toDto(guardado);
    }

    @Override
    public List<HorarioDeAtencionResponse> ListarHorarios() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void EliminarHorarioAtencion(long id) {
        HorarioDeAtencionEntity horarioAtencion = repository.findById(id).
                orElseThrow(() -> new ResourcenotFoundException("Horario de atención no encontrado con id: " + id));
        repository.delete(horarioAtencion);
    }

    @Override
    public HorarioDeAtencionResponse ActualizarHorarioAtencion(long id, HorarioDeAtencionRequest request) {
        HorarioDeAtencionEntity horarioAtencion = repository.findById(id)
                .orElseThrow(() -> new ResourcenotFoundException("Horario de atención no encontrado con id: " + id));

        // Validar rango horario si viene en el request
        if (request.getHoraApertura() != null && request.getHoraCierre() != null) {
            if (request.getHoraApertura().isAfter(request.getHoraCierre()) ||
                    request.getHoraApertura().equals(request.getHoraCierre())) {
                throw new HorarioInvalidoException("Rango de horas inválido. Apertura debe ser anterior al cierre.");
            }
            horarioAtencion.setHoraApertura(request.getHoraApertura());
            horarioAtencion.setHoraCierre(request.getHoraCierre());
        }

        if (request.getDiaSemana() != null) {
            horarioAtencion.setDiaSemana(request.getDiaSemana());
        }

        // Si viene comercioId: lo asignamos
        if (request.getComercioId() != null) {
            ComercioEntity comercio = comercioRepository.findById(request.getComercioId())
                    .orElseThrow(() -> new ComercioNotFoundException("Comercio no encontrado con ID: " + request.getComercioId()));
            horarioAtencion.setComercio(comercio);
        }
        boolean seSolapa = repository.findByComercio_IdComercioAndDiaSemana(
                        horarioAtencion.getComercio().getIdComercio(), horarioAtencion.getDiaSemana())
                .stream()
                .filter(h -> !h.getIdHorarioAtencion().equals(id))
                .anyMatch(h -> horarioAtencion.getHoraApertura().isBefore(h.getHoraCierre())
                        && horarioAtencion.getHoraCierre().isAfter(h.getHoraApertura()));
        if (seSolapa) {
            throw new HorarioSuperpuestoException("El horario se solapa con otro horario del comercio");
        }
        // Si no viene comercioId: dejamos el comercio actual (no tocamos)

        HorarioDeAtencionEntity actualizado = repository.save(horarioAtencion);
        return mapper.toDto(actualizado);
    }

    @Override
    public HorarioDeAtencionResponse ObtenerHorarioPorId(long id) {
        HorarioDeAtencionEntity horarioAtencion = repository.findById(id)
                .orElseThrow(() -> new ResourcenotFoundException("Horario de atención no encontrado con id: " + id));
        return mapper.toDto(horarioAtencion);
    }
}
