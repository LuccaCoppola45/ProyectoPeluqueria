package com.example.ProyectoPeluqueria.Exceptions.auth;

public class PasswordMissMatchException extends RuntimeException {
    public PasswordMissMatchException(String message) {
        super(message);
    }
}
