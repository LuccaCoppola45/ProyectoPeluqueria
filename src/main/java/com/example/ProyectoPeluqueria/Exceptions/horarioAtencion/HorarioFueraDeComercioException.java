package com.example.ProyectoPeluqueria.Exceptions.horarioAtencion;

public class HorarioFueraDeComercioException extends RuntimeException {
    public HorarioFueraDeComercioException(String message) {
        super(message);
    }
}
