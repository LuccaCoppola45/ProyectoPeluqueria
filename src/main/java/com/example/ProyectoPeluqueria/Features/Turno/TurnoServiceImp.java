package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.*;
import com.example.ProyectoPeluqueria.Exceptions.horarioAtencion.HorarioFueraDeAtencionException;
import com.example.ProyectoPeluqueria.Exceptions.servicio.ServicioNotFoundException;
import com.example.ProyectoPeluqueria.Exceptions.user.UserProfileNotFoundException;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.HorarioDeAtencionEntity;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.BloqueoHorarioService;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoHorarioResponse;
import com.example.ProyectoPeluqueria.Features.Servicio.HorariosFijos;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioRepository;
import com.example.ProyectoPeluqueria.Features.Turno.Mapper.TurnoMapper;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoRequest;
import com.example.ProyectoPeluqueria.Features.Turno.dtos.TurnoResponse;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TurnoServiceImp implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final TurnoMapper turnoMapper;
    private final BloqueoHorarioService bloqueoHorarioService;
    private final WhatsappService whatsappService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TurnoResponse crearTurno(TurnoRequest dto) {
        validarFechaYHora(dto);

        UsuarioEntity usuarioAutenticado = obtenerUsuarioAutenticado();
        UsuarioEntity usuario = resolverUsuarioDelTurno(dto, usuarioAutenticado);

        ServicioEntity servicio = servicioRepository.findById(dto.getServicioId())
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado"));
        validarDiaNoBloqueado(servicio.getComercio(), dto.getFecha());
        validarComercio(dto, servicio);
        validarDuracion(servicio);

        LocalTime horaInicio = dto.getHoraInicio();
        validarHoraDeInicio(servicio, horaInicio);
        LocalTime horaFinCalculada = horaInicio.plusMinutes(servicio.getDuracionDelServicio());
        validarRangoCalculado(horaInicio, horaFinCalculada);

        // Validar horario en base al comercio del servicio
        validarHorarioAtencion(servicio.getComercio(), dto.getFecha(), horaInicio, horaFinCalculada);

        validarSuperposicionUsuario(usuario.getIdUsuario(), dto.getFecha(), horaInicio, horaFinCalculada);
        validarAgendaLibre(servicio, dto.getFecha(), horaInicio, horaFinCalculada, null);

        TurnoEntity turno = turnoMapper.toEntity(dto);
        turno.setUsuario(usuario);
        turno.setServicio(servicio);
        turno.setHoraFin(horaFinCalculada);
        turno.setComercio(servicio.getComercio());
        turno.setEstado(EstadoTurno.PENDIENTE);

        TurnoEntity guardado = turnoRepository.save(turno);
        eventPublisher.publishEvent(new TurnoCreadoEvent(guardado.getIdTurno()));

        return turnoMapper.toDto(guardado);
    }

    private UsuarioEntity resolverUsuarioDelTurno(
            TurnoRequest dto,
            UsuarioEntity usuarioAutenticado) {
        if (esAdmin(usuarioAutenticado)) {
            if (dto.getUsuarioId() == null) {
                throw new IllegalArgumentException(
                        "El usuarioId es obligatorio cuando un administrador crea un turno");
            }
            return usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new UserProfileNotFoundException(
                            "Usuario no encontrado con ID: " + dto.getUsuarioId()));
        }

        if (dto.getUsuarioId() != null
                && !dto.getUsuarioId().equals(usuarioAutenticado.getIdUsuario())) {
            throw new UnauthorizedTurnoAccessException(
                    "No puedes crear un turno para otro usuario");
        }
        return usuarioAutenticado;
    }

    @Override
    public List<TurnoResponse> obtenerTodos() {
        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        List<TurnoEntity> turnos = esAdmin(usuario)
                ? turnoRepository.findAll()
                : turnoRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
        return turnos
                .stream()
                .map(turnoMapper::toDto)
                .toList();
    }

    @Override
    public TurnoResponse obtenerPorId(Long id) {
        TurnoEntity turno = turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));
        UsuarioEntity usuario = obtenerUsuarioAutenticado();
        if (!esAdmin(usuario) && !turno.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new UnauthorizedTurnoAccessException("No tienes permisos para consultar este turno");
        }
        return turnoMapper.toDto(turno);
    }

    @Override
    public TurnoResponse actualizarTurno(Long id, TurnoRequest dto) {
        TurnoEntity turno = turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new TurnoAlreadyCancelledException("No se puede modificar un turno cancelado");
        }
        if (turno.getEstado() == EstadoTurno.FINALIZADO) {
            throw new TurnFinishedException("No se puede modificar un turno finalizado");
        }

        validarFechaYHora(dto);

        // Obtener usuario autenticado
        UsuarioEntity usuarioAutenticado = obtenerUsuarioAutenticado();

        // Verificar permisos: solo el propietario o ADMIN pueden modificar
        boolean esAdmin = esAdmin(usuarioAutenticado);

        if (!turno.getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario()) && !esAdmin) {
            throw new UnauthorizedTurnoAccessException(
                    "No tienes permisos para modificar este turno"
            );
        }

        ServicioEntity servicio = servicioRepository.findById(dto.getServicioId())
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado"));
        validarDiaNoBloqueado(servicio.getComercio(), dto.getFecha());
        validarComercio(dto, servicio);
        validarDuracion(servicio);

        LocalTime horaInicio = dto.getHoraInicio();
        validarHoraDeInicio(servicio, horaInicio);
        LocalTime horaFinCalculada = horaInicio.plusMinutes(servicio.getDuracionDelServicio());
        validarRangoCalculado(horaInicio, horaFinCalculada);

        validarHorarioAtencion(servicio.getComercio(), dto.getFecha(), horaInicio, horaFinCalculada);

        validarSuperposicionUsuarioExcluyendoActual(turno.getIdTurno(),
                turno.getUsuario().getIdUsuario(), dto.getFecha(), horaInicio, horaFinCalculada);
        validarAgendaLibre(servicio, dto.getFecha(), horaInicio, horaFinCalculada, turno.getIdTurno());

        turno.setFecha(dto.getFecha());
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFinCalculada);
        turno.setServicio(servicio);
        turno.setComercio(servicio.getComercio());

        TurnoEntity actualizado = turnoRepository.save(turno);
        return turnoMapper.toDto(actualizado);
    }

    @Override
    public void eliminarUnTurno(Long id) {
        TurnoEntity turno = turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));

        if (turno.getEstado() == EstadoTurno.FINALIZADO) {
            throw new TurnFinishedException("No se puede eliminar un turno finalizado");
        }

        UsuarioEntity usuarioAutenticado = obtenerUsuarioAutenticado();

        // Verificar permisos: solo el propietario o ADMIN pueden eliminar
        if (!turno.getUsuario().getIdUsuario().equals(usuarioAutenticado.getIdUsuario())
                && !esAdmin(usuarioAutenticado)) {
            throw new UnauthorizedTurnoAccessException("No tienes permisos para eliminar este turno");
        }

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new TurnoAlreadyCancelledException("El turno ya se encuentra cancelado");
        }
        turno.setEstado(EstadoTurno.CANCELADO);
        turnoRepository.save(turno);

    }

    @Override
    public List<LocalTime> obtenerDisponibilidad(LocalDate fecha, Long servicioId) {
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            throw new TurnoPastDateException("La fecha debe ser hoy o una fecha futura");
        }
        if (servicioId == null || servicioId <= 0) {
            throw new IllegalArgumentException("El ID del servicio debe ser positivo");
        }
        ServicioEntity servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException("Servicio no encontrado"));
        if (servicio.getComercio() == null || servicio.getComercio().getHorarios() == null) {
            throw new HorarioFueraDeAtencionException("El comercio no tiene horarios configurados");
        }
        validarDiaNoBloqueado(servicio.getComercio(), fecha);
        validarDuracion(servicio);
        HorarioDeAtencionEntity horario = horarioDelDia(servicio.getComercio(), fecha);
        List<TurnoEntity> ocupados = turnosActivosDelDia(servicio.getComercio().getIdComercio(), fecha, null);
        List<LocalTime> horariosFijos = HorariosFijos.leer(servicio.getHorariosFijos());
        List<LocalTime> disponibles = new ArrayList<>();
        LocalTime inicio = redondearAlIntervalo(horario.getHoraApertura());
        while (!inicio.plusMinutes(servicio.getDuracionDelServicio()).isAfter(horario.getHoraCierre())) {
            LocalTime inicioCandidato = inicio;
            LocalTime finCandidato = inicioCandidato.plusMinutes(servicio.getDuracionDelServicio());
            boolean noPaso = !fecha.equals(LocalDate.now()) || !inicioCandidato.isBefore(LocalTime.now());
            boolean horaPermitida = horariosFijos.isEmpty() || horariosFijos.contains(inicioCandidato);
            if (noPaso && horaPermitida && puedeAtenderAyudanteSiCorresponde(servicio, fecha, finCandidato)
                    && agendaLibre(servicio, inicioCandidato, finCandidato, ocupados)) {
                disponibles.add(inicioCandidato);
            }
            inicio = inicio.plusMinutes(INTERVALO_TURNOS_MINUTOS);
        }
        return disponibles;
    }

    @Override
    public void enviarRecordatorioPrueba(Long turnoId) {
        TurnoEntity turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));
        boolean enviado;
        try {
            enviado = whatsappService.enviarRecordatorio(turno);
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            throw new IllegalStateException("Meta rechazó el mensaje: " + ex.getResponseBodyAsString());
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new IllegalStateException("No se pudo conectar con WhatsApp: " + ex.getMessage());
        }
        if (!enviado) {
            throw new IllegalStateException(
                    "WhatsApp está desactivado. Configurá WHATSAPP_ENABLED=true para probar el envío.");
        }
    }

    private void validarDiaNoBloqueado(ComercioEntity comercio, LocalDate fecha) {
        if (bloqueoHorarioService.estaBloqueado(comercio.getIdComercio(), fecha)) {
            throw new HorarioFueraDeAtencionException(mensajeDeCierre(comercio, fecha));
        }
    }

    // ===== Días cerrados =====

    private static final Map<DiaSemana, String> NOMBRES_DIAS = Map.of(
            DiaSemana.LUNES, "lunes", DiaSemana.MARTES, "martes", DiaSemana.MIERCOLES, "miércoles",
            DiaSemana.JUEVES, "jueves", DiaSemana.VIERNES, "viernes", DiaSemana.SABADO, "sábado",
            DiaSemana.DOMINGO, "domingo");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("d/M");
    /** Hasta dónde se mira para armar el período cerrado (vacaciones, licencias). */
    private static final int DIAS_MAXIMOS_DE_CIERRE = 120;

    private HorarioDeAtencionEntity horarioDelDia(ComercioEntity comercio, LocalDate fecha) {
        DiaSemana dia = mapearDiaSemana(fecha.getDayOfWeek());
        return comercio.getHorarios().stream()
                .filter(h -> h.getDiaSemana() == dia)
                .findFirst()
                .orElseThrow(() -> new HorarioFueraDeAtencionException(
                        "El negocio no atiende los " + plural(dia) + ". Podés sacar turno " + diasDeAtencion(comercio) + "."));
    }

    // Un bloqueo suelto se informa como día cerrado; varios seguidos (vacaciones) como un período, con fecha de
    // inicio y fin. Los días que el negocio no abre igual (domingo y lunes) no cortan el período.
    private String mensajeDeCierre(ComercioEntity comercio, LocalDate fecha) {
        Set<LocalDate> bloqueados = bloqueoHorarioService.listar(comercio.getIdComercio(),
                        fecha.minusDays(DIAS_MAXIMOS_DE_CIERRE), fecha.plusDays(DIAS_MAXIMOS_DE_CIERRE)).stream()
                .map(BloqueoHorarioResponse::getFecha)
                .collect(Collectors.toSet());
        Set<DiaSemana> diasQueAbre = comercio.getHorarios().stream()
                .map(HorarioDeAtencionEntity::getDiaSemana)
                .collect(Collectors.toSet());
        LocalDate desde = extremoDelCierre(fecha, -1, bloqueados, diasQueAbre);
        LocalDate hasta = extremoDelCierre(fecha, 1, bloqueados, diasQueAbre);
        if (desde.equals(hasta)) {
            return "El negocio está cerrado el " + NOMBRES_DIAS.get(mapearDiaSemana(fecha.getDayOfWeek())) + " "
                    + fecha.format(FORMATO_DIA) + " y no toma turnos ese día. Elegí otra fecha.";
        }
        return "El negocio está cerrado del " + desde.format(FORMATO_DIA) + " al " + hasta.format(FORMATO_DIA)
                + " y no toma turnos en esas fechas. Elegí otra fecha.";
    }

    private LocalDate extremoDelCierre(LocalDate fecha, int paso, Set<LocalDate> bloqueados, Set<DiaSemana> diasQueAbre) {
        LocalDate extremo = fecha;
        LocalDate dia = fecha.plusDays(paso);
        for (int i = 0; i < DIAS_MAXIMOS_DE_CIERRE; i++, dia = dia.plusDays(paso)) {
            if (bloqueados.contains(dia)) {
                extremo = dia;
            } else if (diasQueAbre.contains(mapearDiaSemana(dia.getDayOfWeek()))) {
                break;
            }
        }
        return extremo;
    }

    // "de martes a sábado" si son seguidos; si no, "los martes, jueves y sábados"
    private String diasDeAtencion(ComercioEntity comercio) {
        List<DiaSemana> dias = comercio.getHorarios().stream()
                .map(HorarioDeAtencionEntity::getDiaSemana)
                .distinct()
                .sorted()
                .toList();
        if (dias.isEmpty()) {
            return "otro día";
        }
        boolean seguidos = dias.get(dias.size() - 1).ordinal() - dias.get(0).ordinal() == dias.size() - 1;
        if (dias.size() > 2 && seguidos) {
            return "de " + NOMBRES_DIAS.get(dias.get(0)) + " a " + NOMBRES_DIAS.get(dias.get(dias.size() - 1));
        }
        List<String> nombres = dias.stream().map(this::plural).toList();
        String lista = nombres.size() == 1 ? nombres.get(0)
                : String.join(", ", nombres.subList(0, nombres.size() - 1)) + " y " + nombres.get(nombres.size() - 1);
        return "los " + lista;
    }

    // "sábado" -> "sábados", "domingo" -> "domingos"; los demás no cambian ("los lunes")
    private String plural(DiaSemana dia) {
        String nombre = NOMBRES_DIAS.get(dia);
        return nombre.endsWith("s") ? nombre : nombre + "s";
    }

    private void validarFechaYHora(TurnoRequest dto) {
        if (dto == null || dto.getFecha() == null || dto.getHoraInicio() == null) {
            throw new IllegalArgumentException("La fecha y hora de inicio son obligatorias");
        }
        if (dto.getFecha().isBefore(LocalDate.now())) {
            throw new TurnoPastDateException("No se puede crear un turno en una fecha pasada");
        }

        if (dto.getFecha().equals(LocalDate.now())
                && dto.getHoraInicio().isBefore(LocalTime.now())) {
            throw new TurnoPastDateException("No se puede crear un turno en una hora pasada");
        }
    }

    private void validarSuperposicionUsuario(Long usuarioId, java.time.LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        boolean existeSuperposicion = turnoRepository.findByUsuario_IdUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .anyMatch(t -> haySuperposicion(t, horaInicio, horaFin));

        if (existeSuperposicion) {
            throw new TurnoDuplicadoException("Ya tenés un turno en este horario. No es posible tener 2 turnos al mismo tiempo");
        }
    }

    private void validarSuperposicionUsuarioExcluyendoActual(Long idTurno, Long usuarioId, java.time.LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        boolean existeSuperposicion = turnoRepository.findByUsuario_IdUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .filter(t -> !t.getIdTurno().equals(idTurno))
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .anyMatch(t -> haySuperposicion(t, horaInicio, horaFin));

        if (existeSuperposicion) {
            throw new TurnoDuplicadoException("Ya tenés un turno en este horario. No es posible tener 2 turnos al mismo tiempo");
        }
    }

    // ----- Agenda -----
    // Turnos cada 30 minutos. Los servicios con horarios fijos (reflejos, base permanente) solo se sacan en
    // esas horas, y esos horarios no se ofrecen para ningún otro servicio del titular. La segunda peluquera
    // tiene su propio servicio de color y su propia agenda: martes a viernes hasta las 13 y sábados todo el día.
    private static final int INTERVALO_TURNOS_MINUTOS = 30;
    private static final LocalTime FIN_AYUDANTE_ENTRE_SEMANA = LocalTime.of(13, 0);
    private static final List<DiaSemana> DIAS_AYUDANTE_MANIANA = List.of(
            DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES);

    private boolean esDeAyudante(ServicioEntity servicio) {
        return servicio != null && Boolean.TRUE.equals(servicio.getDeAyudante());
    }

    private void validarHoraDeInicio(ServicioEntity servicio, LocalTime horaInicio) {
        if (horaInicio.getMinute() % INTERVALO_TURNOS_MINUTOS != 0 || horaInicio.getSecond() != 0) {
            throw new IllegalArgumentException("Los turnos se toman cada 30 minutos (por ejemplo 9:00 o 9:30)");
        }
        List<LocalTime> horariosFijos = HorariosFijos.leer(servicio.getHorariosFijos());
        if (!horariosFijos.isEmpty() && !horariosFijos.contains(horaInicio)) {
            String horas = horariosFijos.stream().map(LocalTime::toString).collect(Collectors.joining(" o "));
            throw new IllegalArgumentException(servicio.getNombre() + " solo se puede sacar a las " + horas);
        }
    }

    private LocalTime redondearAlIntervalo(LocalTime hora) {
        int resto = hora.getMinute() % INTERVALO_TURNOS_MINUTOS;
        LocalTime sinSegundos = hora.withSecond(0).withNano(0);
        return resto == 0 ? sinSegundos : sinSegundos.plusMinutes(INTERVALO_TURNOS_MINUTOS - resto);
    }

    private void validarAgendaLibre(ServicioEntity servicio, LocalDate fecha, LocalTime horaInicio,
                                    LocalTime horaFin, Long idTurnoExcluido) {
        if (!puedeAtenderAyudanteSiCorresponde(servicio, fecha, horaFin)) {
            throw new HorarioFueraDeAtencionException(
                    "La segunda peluquera atiende de martes a viernes hasta las 13 y los sábados todo el día");
        }
        Long comercioId = servicio.getComercio().getIdComercio();
        if (!agendaLibre(servicio, horaInicio, horaFin,
                turnosActivosDelDia(comercioId, fecha, idTurnoExcluido))) {
            throw new TurnoDuplicadoException("El horario seleccionado ya está ocupado");
        }
    }

    private boolean puedeAtenderAyudanteSiCorresponde(ServicioEntity servicio, LocalDate fecha, LocalTime horaFin) {
        if (!esDeAyudante(servicio)) {
            return true;
        }
        DiaSemana dia = mapearDiaSemana(fecha.getDayOfWeek());
        return dia == DiaSemana.SABADO
                || (DIAS_AYUDANTE_MANIANA.contains(dia) && !horaFin.isAfter(FIN_AYUDANTE_ENTRE_SEMANA));
    }

    // Cada peluquero tiene su agenda: solo se superponen turnos del mismo peluquero. Los horarios fijos
    // (reflejos y base a las 9 y a las 13) no se reservan: si nadie los sacó, otro servicio puede usar esa hora.
    private boolean agendaLibre(ServicioEntity servicio, LocalTime horaInicio, LocalTime horaFin,
                                List<TurnoEntity> ocupados) {
        boolean deAyudante = esDeAyudante(servicio);
        return ocupados.stream()
                .filter(t -> esDeAyudante(t.getServicio()) == deAyudante)
                .noneMatch(t -> haySuperposicion(t, horaInicio, horaFin));
    }

    private List<TurnoEntity> turnosActivosDelDia(Long comercioId, LocalDate fecha, Long idTurnoExcluido) {
        return turnoRepository.findByComercio_IdComercioAndFecha(comercioId, fecha)
                .stream()
                .filter(t -> idTurnoExcluido == null || !t.getIdTurno().equals(idTurnoExcluido))
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .toList();
    }

    private boolean haySuperposicion(TurnoEntity turno, LocalTime horaInicio, LocalTime horaFin) {
        LocalTime inicioExistente = turno.getHoraInicio();
        LocalTime finExistente = inicioExistente.plusMinutes(turno.getServicio().getDuracionDelServicio());
        return horaInicio.isBefore(finExistente) && horaFin.isAfter(inicioExistente);
    }

    private void validarHorarioAtencion(ComercioEntity comercio, java.time.LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (comercio == null || comercio.getHorarios() == null) {
            throw new HorarioFueraDeAtencionException("El comercio no tiene horarios configurados");
        }
        HorarioDeAtencionEntity horario = horarioDelDia(comercio, fecha);

        if (horaInicio.isBefore(horario.getHoraApertura())) {
            throw new HorarioFueraDeAtencionException("El horario seleccionado es anterior a la apertura del comercio");
        }

        if (horaFin.isAfter(horario.getHoraCierre())) {
            throw new HorarioFueraDeAtencionException("El horario seleccionado excede el horario de cierre del comercio");
        }
    }

    private DiaSemana mapearDiaSemana(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private UsuarioEntity obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UserProfileNotFoundException("No hay usuario autenticado");
        }
        return usuarioRepository.findByDniOrEmail(authentication.getName(), authentication.getName())
                .orElseThrow(() -> new UserProfileNotFoundException("Usuario autenticado no encontrado"));
    }

    private boolean esAdmin(UsuarioEntity usuario) {
        return usuario.getRol() == Rol.ADMIN;
    }

    private void validarComercio(TurnoRequest dto, ServicioEntity servicio) {
        if (dto.getIdComercio() == null || servicio.getComercio() == null
                || !dto.getIdComercio().equals(servicio.getComercio().getIdComercio())) {
            throw new IllegalArgumentException("El servicio no pertenece al comercio indicado");
        }
    }

    private void validarDuracion(ServicioEntity servicio) {
        if (servicio.getDuracionDelServicio() == null || servicio.getDuracionDelServicio() <= 0
                || servicio.getDuracionDelServicio() > 24 * 60) {
            throw new IllegalArgumentException("La duración del servicio debe ser positiva");
        }
    }

    private void validarRangoCalculado(LocalTime inicio, LocalTime fin) {
        if (!fin.isAfter(inicio)) {
            throw new HorarioFueraDeAtencionException("El turno excede el límite del día");
        }
    }

}