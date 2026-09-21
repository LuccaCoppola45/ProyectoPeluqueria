package com.example.ProyectoPeluqueria.Exceptions.auth;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("La Cuenta esta bloqueada");
    }
}
