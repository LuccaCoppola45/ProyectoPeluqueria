package com.example.ProyectoPeluqueria.Exceptions.user;

public class UserHasAssociatedTurnosException extends RuntimeException
{
    public UserHasAssociatedTurnosException(String message) {
        super(message);
    }
}
