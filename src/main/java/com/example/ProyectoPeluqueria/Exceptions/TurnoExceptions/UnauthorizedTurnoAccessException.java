package com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions;

public class UnauthorizedTurnoAccessException extends RuntimeException {
    public UnauthorizedTurnoAccessException(String message) {
        super(message);
    }
}
