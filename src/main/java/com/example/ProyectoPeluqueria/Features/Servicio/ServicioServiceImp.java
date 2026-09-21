package com.example.ProyectoPeluqueria.Features.Servicio;

import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.servicio.ServicioNotFoundException;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioRepository;
import com.example.ProyectoPeluqueria.Features.Servicio.Mapper.ServicioMapper;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioRequest;
import com.example.ProyectoPeluqueria.Features.Servicio.dtos.ServicioResponse;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioServiceImp implements ServicioService{


    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;
    private final ComercioRepository comercioRepository;
    private final TurnoRepository turnoRepository;

    public ServicioServiceImp(
            ServicioRepository servicioRepository,
            ServicioMapper servicioMapper,
            ComercioRepository comercioRepository,
            TurnoRepository turnoRepository) {
        this.servicioRepository = servicioRepository;
        this.servicioMapper = servicioMapper;
        this.comercioRepository = comercioRepository;
        this.turnoRepository = turnoRepository;
    }

    @Override
    public ServicioResponse crearServicio(ServicioRequest request) {
        validarDatos(request);
        ComercioEntity comercio = comercioRepository.findById(request.getIdComercio())
                .orElseThrow(() -> new ComercioNotFoundException(
                        "No se encontró el comercio con ID: " + request.getIdComercio()));

        // Validar que no exista un servicio con el mismo nombre para este comercio
        boolean existeNombre = servicioRepository
                .existsByNombreServicioAndIdComercio(
                        request.getNombreServicio(),
                        request.getIdComercio());

        if (existeNombre) {
            throw new IllegalStateException(
                    "Ya existe un servicio con el mismo nombre para este comercio");
        }

        ServicioEntity servicioEntity = servicioMapper.toEntity(request);
        servicioEntity.setIdServicio(null);
        servicioEntity.setComercio(comercio);
        // NO asignamos horario porque ya no existe esa relación

        ServicioEntity guardado = servicioRepository.save(servicioEntity);
        return servicioMapper.toDto(guardado);
    }

    @Override
    public List<ServicioResponse> listarServicios() {
        return servicioRepository.findAll()
                .stream()
                .map(servicioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ServicioResponse BuscarporId(long id) {
            ServicioEntity servicioEntity = servicioRepository.findById(id)
                    .orElseThrow(() -> new ServicioNotFoundException(
                            "No se encontró el servicio con ID: " + id));
            return servicioMapper.toDto(servicioEntity);
    }

    @Override
    public void eliminarServicio(long id) {
        ServicioEntity servicioEntity = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(
                        "No se encontró el servicio con ID: " + id));

        // Validar que no tenga turnos asociados antes de eliminar
        if (turnoRepository.existsByServicio_IdServicio(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el servicio porque tiene turnos asociados");
        }

        servicioRepository.delete(servicioEntity);
    }

    @Override
    public ServicioResponse ActualizarServicio(long id, ServicioRequest request) {
        validarDatos(request);
        ServicioEntity servicioEntity = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(
                        "No se encontró el servicio con ID: " + id));

        ComercioEntity comercio = comercioRepository.findById(request.getIdComercio())
                .orElseThrow(() -> new ComercioNotFoundException(
                        "No se encontró el comercio con ID: " + request.getIdComercio()));

        boolean existeNombre = servicioRepository.findByComercio_IdComercio(comercio.getIdComercio())
                .stream()
                .anyMatch(s -> !s.getIdServicio().equals(id)
                        && s.getNombre().equalsIgnoreCase(request.getNombreServicio().trim()));
        if (existeNombre) {
            throw new IllegalStateException("Ya existe un servicio con el mismo nombre para este comercio");
        }

        servicioEntity.setNombre(request.getNombreServicio());
        servicioEntity.setPrecio(request.getPrecio());
        servicioEntity.setDuracionDelServicio(request.getDuracionDelServicio());
        servicioEntity.setComercio(comercio);
        // NO asignamos horario porque ya no existe esa relación

        ServicioEntity actualizado = servicioRepository.save(servicioEntity);
        return servicioMapper.toDto(actualizado);
    }

    private void validarDatos(ServicioRequest request) {
        if (request == null || request.getNombreServicio() == null
                || request.getNombreServicio().isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio");
        }
        if (request.getPrecio() <= 0 || !Double.isFinite(request.getPrecio())) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (request.getDuracionDelServicio() == null || request.getDuracionDelServicio() <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        }
        if (request.getIdComercio() == null || request.getIdComercio() <= 0) {
            throw new IllegalArgumentException("El ID del comercio debe ser positivo");
        }
        request.setNombreServicio(request.getNombreServicio().trim());
    }
}
