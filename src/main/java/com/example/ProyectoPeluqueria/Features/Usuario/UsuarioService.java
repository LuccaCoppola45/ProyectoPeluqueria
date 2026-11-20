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

    // Borra el usuario junto con sus turnos, cancelaciones, recordatorios y reseñas (solo admin)
    void eliminarUsuarioConDatos(Long id);

    UpdateUserResponse actualizarUsuario(Long id, UpdateUserRequestDTO dto);

    // El usuario logueado cambia su contraseña (tiene que poner la actual)
    void cambiarMiContrasena(com.example.ProyectoPeluqueria.Features.Usuario.Dtos.CambiarContrasenaRequest dto);

    // El admin le pone una contraseña nueva a un cliente que se la olvidó
    void cambiarContrasenaDeUsuario(Long id, com.example.ProyectoPeluqueria.Features.Usuario.Dtos.CambiarContrasenaRequest dto);
}
