package com.example.ProyectoPeluqueria.Features.Servicio;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Horarios fijos de un servicio guardados como texto ("09:00,14:00"). Un servicio con horarios fijos
 * (reflejos, base permanente) solo se puede sacar en esas horas, y nadie más ocupa esos horarios.
 */
public final class HorariosFijos {

    private HorariosFijos() {
    }

    public static List<LocalTime> leer(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return Arrays.stream(texto.split(","))
                .map(String::trim)
                .filter(parte -> !parte.isEmpty())
                .map(HorariosFijos::leerHora)
                .distinct()
                .sorted()
                .toList();
    }

    // Valida y deja el texto prolijo ("9:00, 14:00" -> "09:00,14:00"). Sin horarios devuelve "" (y no null)
    // para distinguir "el admin los sacó" de "nunca se cargaron".
    public static String normalizar(String texto, int intervaloMinutos) {
        List<LocalTime> horas = leer(texto);
        for (LocalTime hora : horas) {
            if (hora.getMinute() % intervaloMinutos != 0) {
                throw new IllegalArgumentException(
                        "Los horarios fijos tienen que ser en punto o y media (por ejemplo 9:00 o 9:30)");
            }
        }
        return horas.stream().map(LocalTime::toString).collect(Collectors.joining(","));
    }

    private static LocalTime leerHora(String texto) {
        try {
            return LocalTime.parse(texto.length() == 4 ? "0" + texto : texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Horario fijo inválido: '" + texto + "'. Usá el formato 9:00, 14:00");
        }
    }
}
