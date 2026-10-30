package com.example.ProyectoPeluqueria.Config;

import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioRepository;
import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        crearComercio();
        crearAdmin();
    }

    private void crearComercio() {
        if (vacio(comercioNombre) || vacio(comercioDireccion)
                || vacio(comercioTelefono) || vacio(comercioDescripcion)) {
            log.info("Seed: faltan variables SEED_COMERCIO_*, no se crea el comercio");
            return;
        }
        if (comercioRepository.findByNombreIgnoreCaseAndDireccionIgnoreCase(
                comercioNombre, comercioDireccion).isPresent()) {
            return;
        }
        ComercioEntity comercio = new ComercioEntity();
        comercio.setNombre(comercioNombre);
        comercio.setDireccion(comercioDireccion);
        comercio.setTelefono(comercioTelefono);
        comercio.setDescripcion(comercioDescripcion);
        comercioRepository.save(comercio);
        log.info("Seed: comercio '{}' creado", comercioNombre);
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
