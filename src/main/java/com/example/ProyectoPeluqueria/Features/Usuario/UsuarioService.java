package com.example.ProyectoPeluqueria.Features.Usuario;

import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UpdateUserRequestDTO;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UpdateUserResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioRequest;
import com.example.ProyectoPeluqueria.Features.Usuario.Dtos.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    UsuarioResponse crearUsuario(UsuarioRequest dto);

    List<UsuarioResponse> obtenerTodos();
    UsuarioResponse obtenerPorId (Long id);
    void eliminarUsuario (Long id);

    UpdateUserResponse actualizarUsuario(Long id, UpdateUserRequestDTO dto);
}
