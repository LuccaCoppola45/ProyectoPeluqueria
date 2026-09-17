package com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.catalog;

public class ComercioNotFoundException extends RuntimeException {
    public ComercioNotFoundException(String message) {
        super(message);
    }
}
