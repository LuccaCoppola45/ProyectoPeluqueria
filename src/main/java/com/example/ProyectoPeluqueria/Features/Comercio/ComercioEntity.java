package com.example.ProyectoPeluqueria.Features.Comercio;

import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.HorarioDeAtencionEntity;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "comercio", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comercio_nombre_direccion", columnNames = {"nombre", "direccion"})
})
public class ComercioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comercio")
    private Long idComercio;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String descripcion;


    @OneToMany(mappedBy = "comercio")
    @JsonIgnore
    private List<ServicioEntity> servicios;

    @OneToMany(
            mappedBy = "comercio",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<HorarioDeAtencionEntity> horarios;

    @OneToMany(mappedBy = "comercio")
    @JsonIgnore
    private List<TurnoEntity> turnos;
}
