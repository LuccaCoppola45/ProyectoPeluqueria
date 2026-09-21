package com.example.ProyectoPeluqueria.Features.HorarioDeAtencion;

import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "horarios_atencion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDeAtencionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_HorarioAtencion")
    private Long idHorarioAtencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;



    @ManyToOne
    @JoinColumn(name = "id_comercio",
            foreignKey = @ForeignKey(name = "FK_horario_comercio"),
            nullable = true)
    @JsonIgnore
    private ComercioEntity comercio;


}
