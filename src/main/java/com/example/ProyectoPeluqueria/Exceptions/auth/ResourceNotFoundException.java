package com.example.ProyectoPeluqueria.Exceptions.auth;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
