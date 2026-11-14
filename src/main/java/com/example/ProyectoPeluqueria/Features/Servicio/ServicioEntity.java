package com.example.ProyectoPeluqueria.Features.Servicio;


import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
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

    // Precio máximo aproximado; null si solo se muestra "desde"
    @Column(name = "precio_hasta")
    private Double precioHasta;

    // Únicas horas en que se puede sacar el servicio, ej. "09:00,14:00" (ver HorariosFijos)
    @Column(name = "horarios_fijos", length = 100)
    private String horariosFijos;

    // true si lo hace la segunda peluquera (color en paralelo); null/false si lo hace el titular
    @Column(name = "de_ayudante")
    private Boolean deAyudante;


    // Relación con Comercio: Muchos servicios pertenecen a un Comercio
    @ManyToOne
    @JoinColumn(name = "id_comercio",
    foreignKey = @ForeignKey(name = "fk_servicio_comercio"))
    @JsonIgnore
    private ComercioEntity comercio;

    // RELACIÓN CON TURNOS (Tabla intermedia con datos)
    // Un servicio puede estar presente en muchos turnos agendados
    @OneToMany(mappedBy = "servicio")
    private List<TurnoEntity> turnos;


}
