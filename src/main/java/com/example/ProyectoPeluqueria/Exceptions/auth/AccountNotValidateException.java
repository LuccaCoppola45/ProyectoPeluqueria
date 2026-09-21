package com.example.ProyectoPeluqueria.Exceptions.auth;

public class AccountNotValidateException extends RuntimeException {
    public AccountNotValidateException(String message) {
        super(message);
    }
}
