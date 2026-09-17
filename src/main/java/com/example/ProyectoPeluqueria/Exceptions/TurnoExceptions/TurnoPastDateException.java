package com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions;

public class TurnoPastDateException extends RuntimeException {
    public TurnoPastDateException(String message) {
        super(message);
    }
}
