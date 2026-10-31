package com.example.ProyectoPeluqueria.Features.Usuario;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Borrado completo de un usuario y todo lo que depende de él (turnos, recordatorios,
 * cancelaciones y reseñas). Lo usa el admin para limpiar usuarios de prueba.
 * Los deletes van en orden para no romper las claves foráneas.
 */
public interface UsuarioLimpiezaRepository extends Repository<UsuarioEntity, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RecordatorioTurnoEntity r where r.turno.idTurno in " +
            "(select t.idTurno from TurnoEntity t where t.usuario.idUsuario = :id)")
    int borrarRecordatorios(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CancelacionEntity c where c.usuario.idUsuario = :id or c.turno.idTurno in " +
            "(select t.idTurno from TurnoEntity t where t.usuario.idUsuario = :id)")
    int borrarCancelaciones(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from TurnoEntity t where t.usuario.idUsuario = :id")
    int borrarTurnos(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ReseniaEntity r where r.usuario.idUsuario = :id")
    int borrarResenias(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from usuarios_servicios where id_usuario = :id", nativeQuery = true)
    int borrarServiciosVinculados(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from UsuarioEntity u where u.idUsuario = :id")
    int borrarUsuario(@Param("id") Long id);
}
