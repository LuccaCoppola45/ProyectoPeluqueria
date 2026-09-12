package com.example.ProyectoPeluqueria.Features.Servicio;


import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "servicios")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class ServicioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;


    @Column(name = "nombre_servicio", length = 50)
    private String nombre;

    @Column(name = "duracion_servicio", nullable = false)
    private Integer duracionDelServicio;

    @Column(nullable = false)
    private Double precio;


    // Relación con Comercio: Muchos servicios pertenecen a un Comercio
    @ManyToOne
    @JoinColumn(name = "id_comercio",
    foreignKey = @ForeignKey(name = "fk_servicio_comercio"))
    @JsonIgnore
    private ComercioEntity comercio;

    // 1. RELACIÓN MUCHOS A MUCHOS (Tabla Usuarios_Servicios)
    // Es el "espejo" de la relación que definimos en Usuario
    @ManyToMany(mappedBy = "serviciosVinculados")
    @JsonIgnore
    private List<UsuarioEntity> usuariosVinculados;

    // 2. RELACIÓN CON TURNOS (Tabla intermedia con datos)
    // Un servicio puede estar presente en muchos turnos agendados
    @OneToMany(mappedBy = "servicio")
    private List<TurnoEntity> turnos;


}
