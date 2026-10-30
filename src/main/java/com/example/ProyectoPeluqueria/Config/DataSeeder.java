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
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        UsuarioEntity admin = new UsuarioEntity();
        admin.setNombre(adminNombre);
        admin.setApellido(adminApellido);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol(Rol.ADMIN);
        if (!vacio(adminDni)) admin.setDni(adminDni);
        if (!vacio(adminTelefono)) admin.setTelefono(adminTelefono);
        usuarioRepository.save(admin);
        log.info("Seed: usuario admin '{}' creado", email);
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
