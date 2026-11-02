package com.example.ProyectoPeluqueria.Config;

import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioRepository;
import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.HorarioDeAtencionEntity;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.HorarioDeAtencionRepository;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioRepository;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Crea el comercio y el usuario administrador al arrancar la app, si todavía no existen.
 * Los datos salen de variables de entorno (en Render: Environment), así no quedan
 * contraseñas en el repositorio. Si falta alguna variable obligatoria, ese paso se saltea.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ComercioRepository comercioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicioRepository servicioRepository;
    private final HorarioDeAtencionRepository horarioRepository;

    // Días y horario de atención: martes a sábado de 9 a 17 hs
    private static final List<DiaSemana> DIAS_ATENCION = List.of(
            DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO);
    private static final LocalTime APERTURA = LocalTime.of(9, 0);
    private static final LocalTime CIERRE = LocalTime.of(17, 0);

    // Reflejos y base permanente comparten estos horarios. Los valores anteriores por defecto se reemplazan
    // solos; si el admin puso otros horarios desde el panel, se respetan.
    private static final String HORARIOS_REFLEJOS_Y_BASE = "09:00,13:00";
    private static final List<String> HORARIOS_FIJOS_ANTERIORES = List.of("09:00,14:00", "11:00,16:00");

    @Value("${seed.comercio.nombre:}")
    private String comercioNombre;
    @Value("${seed.comercio.direccion:}")
    private String comercioDireccion;
    @Value("${seed.comercio.telefono:}")
    private String comercioTelefono;
    @Value("${seed.comercio.descripcion:}")
    private String comercioDescripcion;

    @Value("${seed.admin.email:}")
    private String adminEmail;
    @Value("${seed.admin.password:}")
    private String adminPassword;
    @Value("${seed.admin.nombre:Admin}")
    private String adminNombre;
    @Value("${seed.admin.apellido:Peluqueria}")
    private String adminApellido;
    @Value("${seed.admin.dni:}")
    private String adminDni;
    @Value("${seed.admin.telefono:}")
    private String adminTelefono;

    // Cada paso va por separado y nunca tira abajo la app: si algo falla (por ejemplo, una tabla
    // que no existe en la base), se registra en el log y la app arranca igual.
    @Override
    public void run(String... args) {
        ComercioEntity comercio = intentar("crear el comercio", this::crearComercio);
        if (comercio != null) {
            intentar("crear los horarios de atención", () -> { crearHorarios(comercio); return null; });
            intentar("crear los servicios", () -> {
                cargarServicio(comercio, "Corte de cabello", 30, 0, null);
                cargarServicio(comercio, "Color", 60, 35000, null);
                cargarServicio(comercio, "Reflejos / Balayage", 60, 95000, HORARIOS_REFLEJOS_Y_BASE);
                cargarServicio(comercio, "Brushing", 30, 20000, null);
                cargarServicio(comercio, "Peinado de fiesta", 40, 60000, null);
                cargarServicio(comercio, "Base permanente", 60, 95000, HORARIOS_REFLEJOS_Y_BASE);
                cargarServicio(comercio, "Hidratación", 40, 25000, null);
                crearServicioDeAyudante(comercio, "Color con segunda peluquera", 60, 35000);
                return null;
            });
        }
        intentar("crear el usuario admin", () -> { crearAdmin(); return null; });
    }

    private <T> T intentar(String paso, java.util.function.Supplier<T> accion) {
        try {
            return accion.get();
        } catch (RuntimeException e) {
            log.error("Seed: no se pudo {}: {}", paso, e.toString());
            return null;
        }
    }

    private ComercioEntity crearComercio() {
        if (vacio(comercioNombre) || vacio(comercioDireccion)
                || vacio(comercioTelefono) || vacio(comercioDescripcion)) {
            log.info("Seed: faltan variables SEED_COMERCIO_*, no se crea el comercio");
            return null;
        }
        ComercioEntity existente = comercioRepository
                .findByNombreIgnoreCaseAndDireccionIgnoreCase(comercioNombre, comercioDireccion)
                .orElse(null);
        if (existente != null) {
            return existente;
        }
        ComercioEntity comercio = new ComercioEntity();
        comercio.setNombre(comercioNombre);
        comercio.setDireccion(comercioDireccion);
        comercio.setTelefono(comercioTelefono);
        comercio.setDescripcion(comercioDescripcion);
        comercio = comercioRepository.save(comercio);
        log.info("Seed: comercio '{}' creado", comercioNombre);
        return comercio;
    }

    private void crearHorarios(ComercioEntity comercio) {
        for (DiaSemana dia : DIAS_ATENCION) {
            if (!horarioRepository.findByComercio_IdComercioAndDiaSemana(comercio.getIdComercio(), dia).isEmpty()) {
                continue;
            }
            HorarioDeAtencionEntity horario = new HorarioDeAtencionEntity();
            horario.setComercio(comercio);
            horario.setDiaSemana(dia);
            horario.setHoraApertura(APERTURA);
            horario.setHoraCierre(CIERRE);
            horarioRepository.save(horario);
            log.info("Seed: horario {} {}-{} creado", dia, APERTURA, CIERRE);
        }
    }

    // Crea el servicio si no existe. Si ya existe, solo completa lo que el admin nunca cargó (precio en 0,
    // horarios fijos sin definir): lo que se edita desde el panel no se pisa en deploys posteriores.
    private void cargarServicio(ComercioEntity comercio, String nombre, int minutos, double precio,
                                String horariosFijos) {
        ServicioEntity servicio = servicioRepository.findByComercio_IdComercio(comercio.getIdComercio()).stream()
                .filter(sv -> nombre.equalsIgnoreCase(sv.getNombre()))
                .findFirst()
                .orElse(null);
        if (servicio == null) {
            servicio = new ServicioEntity();
            servicio.setNombre(nombre);
            servicio.setPrecio(0.0);
            servicio.setComercio(comercio);
            log.info("Seed: servicio '{}' creado", nombre);
        }
        boolean sinPrecio = servicio.getPrecio() == null || servicio.getPrecio() <= 0;
        if (sinPrecio) {
            servicio.setPrecio(precio);
            servicio.setDuracionDelServicio(minutos);
        }
        boolean horariosSinTocar = servicio.getHorariosFijos() == null
                || HORARIOS_FIJOS_ANTERIORES.contains(servicio.getHorariosFijos());
        if (horariosSinTocar && horariosFijos != null) {
            servicio.setHorariosFijos(horariosFijos);
        }
        servicioRepository.save(servicio);
    }

    // Se busca por la marca y no por el nombre, así el admin lo puede renombrar sin que se cree otro.
    private void crearServicioDeAyudante(ComercioEntity comercio, String nombre, int minutos, double precio) {
        boolean existe = servicioRepository.findByComercio_IdComercio(comercio.getIdComercio()).stream()
                .anyMatch(sv -> Boolean.TRUE.equals(sv.getDeAyudante()));
        if (existe) {
            return;
        }
        ServicioEntity servicio = new ServicioEntity();
        servicio.setNombre(nombre);
        servicio.setDuracionDelServicio(minutos);
        servicio.setPrecio(precio);
        servicio.setDeAyudante(true);
        servicio.setComercio(comercio);
        servicioRepository.save(servicio);
        log.info("Seed: servicio de la segunda peluquera '{}' ({} min) creado", nombre, minutos);
    }

    private void crearAdmin() {
        if (vacio(adminEmail) || vacio(adminPassword)) {
            log.info("Seed: faltan SEED_ADMIN_EMAIL o SEED_ADMIN_PASSWORD, no se crea el admin");
            return;
        }
        String email = adminEmail.trim().toLowerCase();
        String dni = vacio(adminDni) ? null : adminDni.trim();

        // Si el admin ya existe, solo se completa el DNI (el login es por DNI) y se asegura el rol
        UsuarioEntity existente = usuarioRepository.findByEmail(email).orElse(null);
        if (existente != null) {
            boolean cambio = false;
            if (dni != null && !dni.equals(existente.getDni())) {
                if (usuarioRepository.existsByDni(dni)) {
                    log.warn("Seed: el DNI {} ya lo usa otro usuario, no se asigna al admin", dni);
                } else {
                    existente.setDni(dni);
                    cambio = true;
                }
            }
            if (existente.getRol() != Rol.ADMIN) {
                existente.setRol(Rol.ADMIN);
                cambio = true;
            }
            if (cambio) {
                usuarioRepository.save(existente);
                log.info("Seed: usuario admin '{}' actualizado", email);
            }
            return;
        }
        if (dni == null) {
            log.warn("Seed: falta SEED_ADMIN_DNI; el admin se crea pero no va a poder iniciar sesión (el login es por DNI)");
        }
        UsuarioEntity admin = new UsuarioEntity();
        admin.setNombre(adminNombre);
        admin.setApellido(adminApellido);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol(Rol.ADMIN);
        if (dni != null) admin.setDni(dni);
        if (!vacio(adminTelefono)) admin.setTelefono(adminTelefono);
        usuarioRepository.save(admin);
        log.info("Seed: usuario admin '{}' creado", email);
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
