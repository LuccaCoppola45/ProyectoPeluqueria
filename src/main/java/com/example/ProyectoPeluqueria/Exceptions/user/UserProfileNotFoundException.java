package com.example.ProyectoPeluqueria.Exceptions.user;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}
