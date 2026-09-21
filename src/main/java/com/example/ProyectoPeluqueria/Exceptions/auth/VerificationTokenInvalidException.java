package com.example.ProyectoPeluqueria.Exceptions.auth;

public class VerificationTokenInvalidException extends RuntimeException {
    public VerificationTokenInvalidException(String message) {
        super(message);
    }
}
