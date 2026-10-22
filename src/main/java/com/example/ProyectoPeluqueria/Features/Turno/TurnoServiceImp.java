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
import java.util.ArrayList;
import java.util.List;

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
        validarDiaNoBloqueado(servicio.getComercio().getIdComercio(), dto.getFecha());
        validarComercio(dto, servicio);
        validarDuracion(servicio);

        LocalTime horaInicio = dto.getHoraInicio();
        LocalTime horaFinCalculada = horaInicio.plusMinutes(servicio.getDuracionDelServicio());
        validarRangoCalculado(horaInicio, horaFinCalculada);

        // Validar horario en base al comercio del servicio
        validarHorarioAtencion(servicio.getComercio(), dto.getFecha(), horaInicio, horaFinCalculada);

        // Un solo profesional atiende el comercio: ningún servicio puede solaparse.
        validarSuperposicionComercio(
                servicio.getComercio().getIdComercio(),
                dto.getFecha(),
                horaInicio,
                horaFinCalculada);
        validarSuperposicionUsuario(usuario.getIdUsuario(), dto.getFecha(), horaInicio, horaFinCalculada);

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
        validarDiaNoBloqueado(servicio.getComercio().getIdComercio(), dto.getFecha());
        validarComercio(dto, servicio);
        validarDuracion(servicio);

        LocalTime horaInicio = dto.getHoraInicio();
        LocalTime horaFinCalculada = horaInicio.plusMinutes(servicio.getDuracionDelServicio());
        validarRangoCalculado(horaInicio, horaFinCalculada);

        validarHorarioAtencion(servicio.getComercio(), dto.getFecha(), horaInicio, horaFinCalculada);

        validarSuperposicionComercioExcluyendoActual(
                turno.getIdTurno(),
                servicio.getComercio().getIdComercio(),
                dto.getFecha(),
                horaInicio,
                horaFinCalculada);
        validarSuperposicionUsuarioExcluyendoActual(turno.getIdTurno(),
                turno.getUsuario().getIdUsuario(), dto.getFecha(), horaInicio, horaFinCalculada);

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
        if (servicio.getComercio() != null
                && bloqueoHorarioService.estaBloqueado(
                servicio.getComercio().getIdComercio(), fecha)) {
            return List.of();
        }
        validarDuracion(servicio);
        DiaSemana dia = mapearDiaSemana(fecha.getDayOfWeek());
        if (servicio.getComercio() == null || servicio.getComercio().getHorarios() == null) {
            throw new HorarioFueraDeAtencionException("El comercio no tiene horarios configurados");
        }
        HorarioDeAtencionEntity horario = servicio.getComercio().getHorarios().stream()
                .filter(h -> h.getDiaSemana() == dia)
                .findFirst()
                .orElseThrow(() -> new HorarioFueraDeAtencionException("El comercio no atiende los días " + dia));
        List<TurnoEntity> ocupados = turnoRepository
                .findByComercio_IdComercioAndFecha(
                        servicio.getComercio().getIdComercio(),
                        fecha)
                .stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .toList();
        List<LocalTime> disponibles = new ArrayList<>();
        LocalTime inicio = horario.getHoraApertura();
        while (!inicio.plusMinutes(servicio.getDuracionDelServicio()).isAfter(horario.getHoraCierre())) {
            LocalTime inicioCandidato = inicio;
            LocalTime finCandidato = inicioCandidato.plusMinutes(servicio.getDuracionDelServicio());
            boolean noPaso = !fecha.equals(LocalDate.now()) || !inicioCandidato.isBefore(LocalTime.now());
            if (noPaso && ocupados.stream().noneMatch(t -> haySuperposicion(t, inicioCandidato, finCandidato))) {
                disponibles.add(inicioCandidato);
            }
            inicio = inicio.plusMinutes(15);
        }
        return disponibles;
    }

    @Override
    public void enviarRecordatorioPrueba(Long turnoId) {
        TurnoEntity turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));
        if (!whatsappService.enviarRecordatorio(turno)) {
            throw new IllegalStateException(
                    "WhatsApp está desactivado. Configurá WHATSAPP_ENABLED=true para probar el envío.");
        }
    }

    private void validarDiaNoBloqueado(Long comercioId, LocalDate fecha) {
        if (bloqueoHorarioService.estaBloqueado(comercioId, fecha)) {
            throw new HorarioFueraDeAtencionException(
                    "El comercio está cerrado el día seleccionado");
        }
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

    private void validarSuperposicionComercio(
            Long comercioId,
            java.time.LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        boolean existeSuperposicion = turnoRepository.findByComercio_IdComercioAndFecha(comercioId, fecha)
                .stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .anyMatch(t -> haySuperposicion(t, horaInicio, horaFin));

        if (existeSuperposicion) {
            throw new TurnoDuplicadoException("El horario seleccionado ya está ocupado");
        }
    }

    private void validarSuperposicionUsuario(Long usuarioId, java.time.LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        boolean existeSuperposicion = turnoRepository.findByUsuario_IdUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .anyMatch(t -> haySuperposicion(t, horaInicio, horaFin));

        if (existeSuperposicion) {
            throw new TurnoDuplicadoException("El usuario ya posee un turno en ese horario");
        }
    }

    private void validarSuperposicionComercioExcluyendoActual(
            Long idTurno,
            Long comercioId,
            java.time.LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        boolean existeSuperposicion = turnoRepository.findByComercio_IdComercioAndFecha(comercioId, fecha)
                .stream()
                .filter(t -> !t.getIdTurno().equals(idTurno))
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .anyMatch(t -> haySuperposicion(t, horaInicio, horaFin));

        if (existeSuperposicion) {
            throw new TurnoDuplicadoException("El horario seleccionado ya está ocupado");
        }
    }

    private void validarSuperposicionUsuarioExcluyendoActual(Long idTurno, Long usuarioId, java.time.LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        boolean existeSuperposicion = turnoRepository.findByUsuario_IdUsuarioAndFecha(usuarioId, fecha)
                .stream()
                .filter(t -> !t.getIdTurno().equals(idTurno))
                .filter(t -> t.getEstado() != EstadoTurno.CANCELADO)
                .anyMatch(t -> haySuperposicion(t, horaInicio, horaFin));

        if (existeSuperposicion) {
            throw new TurnoDuplicadoException("El usuario ya posee un turno en ese horario");
        }
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
        DiaSemana diaTurno = mapearDiaSemana(fecha.getDayOfWeek());
        HorarioDeAtencionEntity horario = comercio.getHorarios().stream()
                .filter(h -> h.getDiaSemana() == diaTurno)
                .findFirst()
                .orElseThrow(() -> new HorarioFueraDeAtencionException("El comercio no atiende los días " + diaTurno));

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