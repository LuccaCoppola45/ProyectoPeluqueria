package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Cancelaciones.CancelacionEntity;
import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turnos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurnoEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long idTurno;

    @Column(nullable = false)
    private LocalDate fecha;


    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime horaInicio;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;

    @Column(name = "respuesta_cliente", length = 20)
    private String respuestaCliente;

    @Column(name = "fecha_respuesta", columnDefinition = "DATETIME")
    private java.time.LocalDateTime fechaRespuesta;



    // RELACIÓN: Muchos turnos pueden ser de un Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario",
            foreignKey = @ForeignKey(name = "fk_turno_usuario"))
    @JsonIgnore
    private UsuarioEntity usuario;

    // RELACIÓN: Muchos turnos pueden ser para un Servicio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio",
            foreignKey = @ForeignKey (name = "fk_turno_servicio"))
    @JsonIgnore
    private ServicioEntity servicio;

    // RELACIÓN: Muchos turnos pertenecen a un Comercio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comercio",
            foreignKey = @ForeignKey(name = "fk_turno_comercio"))
    @JsonIgnore
    private ComercioEntity comercio;

    // RELACIÓN 1 a 1: Un turno puede tener una cancelación
    // 'mappedBy' indica que la FK está en la tabla Cancelacion
    @OneToOne(mappedBy = "turno", cascade = CascadeType.ALL)
    private CancelacionEntity cancelacion;

}
