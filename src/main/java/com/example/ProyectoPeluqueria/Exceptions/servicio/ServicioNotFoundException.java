package com.example.ProyectoPeluqueria.Exceptions.servicio;

public class ServicioNotFoundException extends RuntimeException
{
    public ServicioNotFoundException(String message)
    {
        super(message);
    }
}
