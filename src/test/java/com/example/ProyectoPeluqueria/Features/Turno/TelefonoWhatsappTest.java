package com.example.ProyectoPeluqueria.Features.Turno;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelefonoWhatsappTest {

    @Test
    void normalizaLasFormasComunesAl549() {
        assertEquals("5492235964948", TelefonoWhatsapp.normalizar("+542235964948"));
        assertEquals("5492235964948", TelefonoWhatsapp.normalizar("92235964948"));
        assertEquals("5492235964948", TelefonoWhatsapp.normalizar("0223 15 596-4948"));
        assertEquals("5492235964948", TelefonoWhatsapp.normalizar("542235964948"));
    }

    @Test
    void sinNueveSacaElNueveDeUnCelularArgentino() {
        assertEquals("542235964948", TelefonoWhatsapp.sinNueve("5492235964948"));
    }

    @Test
    void sinNueveDejaIgualLoQueNoEsCelularArgentino() {
        assertEquals("15551429706", TelefonoWhatsapp.sinNueve("15551429706"));
        assertEquals("542235964948", TelefonoWhatsapp.sinNueve("542235964948"));
    }
}
