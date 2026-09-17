package com.example.ProyectoPeluqueria.Features.Comercio;

import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioAlreadyExistsException;
import com.example.ProyectoPeluqueria.Exceptions.catalog.ComercioNotFoundException;
import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioRequest;
import com.example.ProyectoPeluqueria.Features.Comercio.Dtos.ComercioResponse;
import com.example.ProyectoPeluqueria.Features.Comercio.Mapper.ComercioMapper;

public class ComercioServiceImp implements  ComercioService {

    private final ComercioRepository comercioRepository;
    private final ComercioMapper comercioMapper;

    public  ComercioServiceImp(ComercioRepository comercioRepository, ComercioMapper comercioMapper) {
        this.comercioRepository = comercioRepository;
        this.comercioMapper = comercioMapper;
    }

    @Override
    public ComercioResponse crearComercio(ComercioRequest request) {
        String nombreLimpio = request.getNombre().trim();
        String direccionLimpia = request.getDireccion().trim();

        // 2. Validar que no exista un comercio duplicado (mismo nombre + dirección)
        boolean existeDuplicado = comercioRepository.findByNombreIgnoreCaseAndDireccionIgnoreCase(nombreLimpio, direccionLimpia).isPresent();
        if (existeDuplicado) {
            throw new ComercioAlreadyExistsException("Ya existe un comercio registrado con ese nombre en esa dirección.");
        }

        ComercioEntity entity = comercioMapper.toEntity(request);

        entity.setNombre(nombreLimpio);
        entity.setDireccion(direccionLimpia);
        ComercioEntity saved = comercioRepository.save(entity);
        return comercioMapper.toResponse(saved);
    }

    @Override
    public ComercioResponse actualizarComercio(Long id, ComercioRequest request) {
        ComercioEntity entity = comercioRepository.findById(id)
                .orElseThrow(() -> new ComercioNotFoundException("No se puede actualizar: Comercio no encontrado con id: " + id));


        entity.setNombre(request.getNombre().trim());
        entity.setDireccion(request.getDireccion().trim());
        entity.setTelefono(request.getTelefono().trim());

        ComercioEntity updated = comercioRepository.save(entity);
        return comercioMapper.toResponse(updated);
    }

    @Override
    public ComercioResponse ObtenerInfo() {
        ComercioEntity comercio = comercioRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existe la peluquería configurada"));
        return comercioMapper.toResponse(comercio);
    }


}
