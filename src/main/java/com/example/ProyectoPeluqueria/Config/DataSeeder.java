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
import org.springframework.transaction.annotation.Transactional;

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

    // Precios opcionales (si no se cargan quedan en 0 y se pueden completar después)
    @Value("${SEED_PRECIO_CORTE:}")
    private String precioCorte;
    @Value("${SEED_PRECIO_COLOR:}")
    private String precioColor;
    @Value("${SEED_PRECIO_BALAYAGE:}")
    private String precioBalayage;

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

    @Override
    @Transactional
    public void run(String... args) {
        ComercioEntity comercio = crearComercio();
        if (comercio != null) {
            crearHorarios(comercio);
            crearServicio(comercio, "Corte de cabello", 30, precioCorte);
            crearServicio(comercio, "Color", 60, precioColor);
            crearServicio(comercio, "Reflejos / Balayage", 45, precioBalayage);
        }
        crearAdmin();
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

    private void crearServicio(ComercioEntity comercio, String nombre, int minutos, String precioTexto) {
        Double precio = parsearPrecio(precioTexto);
        ServicioEntity existente = servicioRepository.findByComercio_IdComercio(comercio.getIdComercio()).stream()
                .filter(sv -> nombre.equalsIgnoreCase(sv.getNombre()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            // Si se cargó un precio nuevo en las variables, se actualiza
            if (precio != null && !precio.equals(existente.getPrecio())) {
                existente.setPrecio(precio);
                servicioRepository.save(existente);
                log.info("Seed: precio de '{}' actualizado a {}", nombre, precio);
            }
            return;
        }
        ServicioEntity servicio = new ServicioEntity();
        servicio.setNombre(nombre);
        servicio.setDuracionDelServicio(minutos);
        servicio.setPrecio(precio != null ? precio : 0.0);
        servicio.setComercio(comercio);
        servicioRepository.save(servicio);
        log.info("Seed: servicio '{}' ({} min) creado", nombre, minutos);
    }

    private static Double parsearPrecio(String texto) {
        if (vacio(texto)) return null;
        try {
            return Double.parseDouble(texto.trim().replace("$", "").replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            log.warn("Seed: precio inválido '{}', se ignora", texto);
            return null;
        }
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
