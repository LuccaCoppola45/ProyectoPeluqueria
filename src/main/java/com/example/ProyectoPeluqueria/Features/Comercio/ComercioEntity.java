package com.example.ProyectoPeluqueria.Features.Comercio;

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
    private List<HorarioAtencionEntity> horarios;


    @ManyToOne
    @JoinColumn(name = "usuario_id_propietario", foreignKey = @ForeignKey(name = "fk_comercio_usuario"))
    @JsonIgnore
    private UsuarioEntity usuarioPropietario;

    @OneToMany(mappedBy = "comercio")
    @JsonIgnore
    private List<TurnoEntity> turnos;
}
