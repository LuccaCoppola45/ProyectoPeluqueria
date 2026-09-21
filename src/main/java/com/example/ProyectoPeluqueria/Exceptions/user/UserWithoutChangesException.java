package com.example.ProyectoPeluqueria.Exceptions.user;

public class UserWithoutChangesException extends RuntimeException {
    public UserWithoutChangesException(String message) {
        super(message);
    }
}
