package com.example.ProyectoPeluqueria.Exceptions.catalog;

public class ComercioNotFoundException extends RuntimeException {
    public ComercioNotFoundException(String message) {
        super(message);
    }
}
