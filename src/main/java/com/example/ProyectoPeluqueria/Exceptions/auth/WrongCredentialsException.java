package com.example.ProyectoPeluqueria.Exceptions.auth;

public class WrongCredentialsException extends RuntimeException {
    public WrongCredentialsException(String message) {
        super(message);
    }
}
