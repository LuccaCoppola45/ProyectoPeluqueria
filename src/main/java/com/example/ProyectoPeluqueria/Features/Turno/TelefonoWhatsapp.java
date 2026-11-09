package com.example.ProyectoPeluqueria.Features.Turno;

/**
 * Normaliza teléfonos de Argentina al formato que usa WhatsApp: 549 + código de área + número
 * (sin 0 ni 15). Ej: "+541123456789", "011 15 2345-6789" y "5491123456789" -> "5491123456789".
 * El registro guarda "+54" + 10 dígitos, y Meta manda las respuestas desde "549..." (wa_id),
 * así que hay que comparar siempre con este formato.
 */
public final class TelefonoWhatsapp {

    private TelefonoWhatsapp() {
    }

    public static String normalizar(String telefono) {
        if (telefono == null) {
            return null;
        }
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.startsWith("00")) {
            digitos = digitos.substring(2);
        }
        if (digitos.startsWith("54")) {
            digitos = digitos.substring(2);
        }
        if (digitos.startsWith("9") && digitos.length() == 11) {
            digitos = digitos.substring(1);
        }
        if (digitos.startsWith("0")) {
            digitos = digitos.substring(1);
        }
        // Si quedó el 15 después del código de área (2, 3 o 4 dígitos), se saca
        if (digitos.length() == 12) {
            for (int largoArea = 2; largoArea <= 4; largoArea++) {
                if (digitos.startsWith("15", largoArea)) {
                    digitos = digitos.substring(0, largoArea) + digitos.substring(largoArea + 2);
                    break;
                }
            }
        }
        if (digitos.length() != 10) {
            // No parece un celular argentino: se manda tal cual (solo dígitos)
            return telefono.replaceAll("\\D", "");
        }
        return "549" + digitos;
    }

    /**
     * Saca el 9 de un celular de Argentina ya normalizado: "5492235964948" -> "542235964948".
     * Si no tiene ese formato, lo devuelve igual.
     */
    public static String sinNueve(String telefonoNormalizado) {
        if (telefonoNormalizado != null && telefonoNormalizado.startsWith("549")
                && telefonoNormalizado.length() == 13) {
            return "54" + telefonoNormalizado.substring(3);
        }
        return telefonoNormalizado;
    }
}
