package com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions;

public class TurnoAlreadyCancelledException extends RuntimeException {
    public TurnoAlreadyCancelledException(String message) {
        super(message);
    }
}
