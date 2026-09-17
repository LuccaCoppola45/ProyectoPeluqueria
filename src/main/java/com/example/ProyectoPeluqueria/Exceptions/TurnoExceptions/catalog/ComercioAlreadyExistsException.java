package com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.catalog;

public class ComercioAlreadyExistsException extends RuntimeException {
    public ComercioAlreadyExistsException(String message) {
        super(message);
    }
}
