package com.example.ProyectoPeluqueria.Features.Cancelaciones;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancelacionRepository extends JpaRepository<CancelacionEntity, Long>
{
    List<CancelacionEntity> findByUsuario_IdUsuario(Long usuarioId);
}
