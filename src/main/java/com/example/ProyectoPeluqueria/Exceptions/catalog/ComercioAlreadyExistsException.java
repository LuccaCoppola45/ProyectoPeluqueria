package com.example.ProyectoPeluqueria.Exceptions.catalog;

public class ComercioAlreadyExistsException extends RuntimeException {
    public ComercioAlreadyExistsException(String message) {
        super(message);
    }
}
