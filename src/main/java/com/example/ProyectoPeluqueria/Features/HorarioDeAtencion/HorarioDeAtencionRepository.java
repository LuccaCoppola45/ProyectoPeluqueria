package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioDeAtencionRepository extends JpaRepository<HorarioDeAtencionEntity, Long>
{
    List<HorarioDeAtencionEntity> findByComercio_IdComercioAndDiaSemana(Long comercioId, DiaSemana diaSemana);
    List<HorarioDeAtencionEntity> findByComercio_IdComercio(Long comercioId);
//    HorarioAtencionResponse findByIdHorarioAtencion(Long id);
//    List<HorarioAtencionEntity> findByComercio_IdComercio(Long comercioId);
//    List<HorarioAtencionEntity> findByServicio_IdServicio(Long servicioId);
//    boolean existsByComercio_IdComercioAndDiaSemanaAndHoraAperturaAndHoraCierre(Long comercioId, Diasemana diaSemana, LocalTime horaApertura, LocalTime horaCierre);


}
