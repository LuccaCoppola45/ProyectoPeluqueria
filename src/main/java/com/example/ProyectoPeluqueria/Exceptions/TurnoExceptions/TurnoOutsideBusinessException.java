package com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions;

public class TurnoOutsideBusinessException extends RuntimeException {
    public TurnoOutsideBusinessException(String message) {
        super(message);
    }
}
