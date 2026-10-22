package com.example.ProyectoPeluqueria.Features.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long>
{
    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    Optional<UsuarioEntity> findByDni(String dni);

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByDniOrEmail(String dni, String email);

    Optional<UsuarioEntity> findByTelefono(String telefono);
}
