package com.example.ProyectoPeluqueria.Exceptions.user;

public class UserDniAlreadyInUseException extends RuntimeException {
    public UserDniAlreadyInUseException(String message) {
        super(message);
    }
}
