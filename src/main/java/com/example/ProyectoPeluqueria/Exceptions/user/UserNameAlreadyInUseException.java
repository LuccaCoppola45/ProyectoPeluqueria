package com.example.ProyectoPeluqueria.Exceptions.user;

public class UserNameAlreadyInUseException extends RuntimeException {
    public UserNameAlreadyInUseException(String message) {
        super(message);
    }
}
