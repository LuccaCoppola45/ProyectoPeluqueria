package com.example.ProyectoPeluqueria.Exceptions.horarioAtencion;

public class HorarioFueraDeAtencionException extends RuntimeException
{
    public HorarioFueraDeAtencionException(String message) {
        super(message);
    }
}
