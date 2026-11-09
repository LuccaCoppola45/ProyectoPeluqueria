package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WhatsappWebhookControllerTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final RecordatorioTurnoRepository recordatorioRepository = mock(RecordatorioTurnoRepository.class);
    private final WhatsappWebhookController controller = new WhatsappWebhookController(
            "token", usuarioRepository, turnoRepository, recordatorioRepository);

    private TurnoEntity turnoDe1230;
    private TurnoEntity turnoDe1330;

    @BeforeEach
    void setUp() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setTelefono("+542235964948");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        LocalDate manana = LocalDate.now().plusDays(1);
        turnoDe1230 = turno(10L, usuario, manana, LocalTime.of(12, 30));
        turnoDe1330 = turno(11L, usuario, manana, LocalTime.of(13, 30));
        when(turnoRepository.findByUsuario_IdUsuarioAndEstadoOrderByFechaAscHoraInicioAsc(1L, EstadoTurno.PENDIENTE))
                .thenReturn(List.of(turnoDe1230, turnoDe1330));
        // Los dos turnos recibieron recordatorio
        when(recordatorioRepository.existsByTurno_IdTurno(anyLong())).thenReturn(true);
        when(recordatorioRepository.findTurnoByMensajeId(anyString())).thenReturn(Optional.empty());
        when(recordatorioRepository.findTurnoByMensajeId("wamid.1330")).thenReturn(Optional.of(turnoDe1330));
    }

    @Test
    void cancelaElTurnoDelMensajeRespondidoAunqueHayaOtroAntes() {
        controller.recibir(respuestaConBoton("Cancelar turno", "wamid.1330"));

        assertEquals(EstadoTurno.CANCELADO, turnoDe1330.getEstado());
        assertEquals(EstadoTurno.PENDIENTE, turnoDe1230.getEstado());
    }

    @Test
    void sinContextoUsaElProximoTurnoConRecordatorio() {
        controller.recibir(respuestaConBoton("NO", null));

        assertEquals(EstadoTurno.CANCELADO, turnoDe1230.getEstado());
        assertEquals(EstadoTurno.PENDIENTE, turnoDe1330.getEstado());
    }

    @Test
    void confirmaConSi() {
        controller.recibir(respuestaConBoton("SÍ", "wamid.1330"));

        assertEquals(EstadoTurno.CONFIRMADO, turnoDe1330.getEstado());
    }

    @Test
    void unAvisoDeEntregaNoCambiaNingunTurno() {
        Map<String, Object> value = Map.of("statuses", List.of(Map.of(
                "id", "wamid.1330", "status", "failed", "recipient_id", "542235964948",
                "errors", List.of(Map.of("code", 131049, "title", "No entregado")))));
        controller.recibir(Map.of("entry", List.of(Map.of("changes", List.of(Map.of("value", value))))));

        assertEquals(EstadoTurno.PENDIENTE, turnoDe1230.getEstado());
        assertEquals(EstadoTurno.PENDIENTE, turnoDe1330.getEstado());
    }

    private static TurnoEntity turno(Long id, UsuarioEntity usuario, LocalDate fecha, LocalTime hora) {
        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(id);
        turno.setUsuario(usuario);
        turno.setFecha(fecha);
        turno.setHoraInicio(hora);
        turno.setHoraFin(hora.plusHours(1));
        turno.setEstado(EstadoTurno.PENDIENTE);
        return turno;
    }

    // Forma del webhook de Meta cuando se aprieta un botón de respuesta rápida
    private static Map<String, Object> respuestaConBoton(String texto, String mensajeRespondido) {
        Map<String, Object> mensaje = new java.util.HashMap<>();
        mensaje.put("from", "5492235964948");
        mensaje.put("type", "button");
        mensaje.put("button", Map.of("text", texto, "payload", texto));
        if (mensajeRespondido != null) {
            mensaje.put("context", Map.of("from", "15551429706", "id", mensajeRespondido));
        }
        Map<String, Object> value = Map.of(
                "messaging_product", "whatsapp",
                "contacts", List.of(Map.of("wa_id", "5492235964948")),
                "messages", List.of(mensaje));
        return Map.of("object", "whatsapp_business_account",
                "entry", List.of(Map.of("changes", List.of(Map.of("field", "messages", "value", value)))));
    }
}
