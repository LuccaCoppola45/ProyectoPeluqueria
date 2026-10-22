package com.example.ProyectoPeluqueria.Features.Usuario;

import com.example.ProyectoPeluqueria.Features.Cancelaciones.CancelacionEntity;
import com.example.ProyectoPeluqueria.Features.Enums.Rol;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Turno.TurnoEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;


@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UsuarioEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;


    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.CLIENTE;

    @Column(unique = true)
    private String email;

    @Column(unique = true, length = 8)
    private String dni;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String telefono;


    @ManyToMany
    @JoinTable(
            name = "usuarios_servicios", // Nombre de la tabla intermedia en MySQL
            joinColumns = @JoinColumn(name = "id_usuario", // FK de esta clase
                    foreignKey = @ForeignKey(name = "FK_usuario_servicio_usuario")
            ), // FK hacia la tabla Padre (Usuario)
            inverseJoinColumns = @JoinColumn(name = "id_servicio",
                    foreignKey = @ForeignKey(name = "FK_usuario_servicio_servicio") // FK hacia la tabla Hija (Servicio)// FK de la otra clase

            )
    )

    @JsonIgnore
    private List<ServicioEntity> serviciosVinculados;


    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<TurnoEntity> turnosVinculados;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<CancelacionEntity> cancelacionEntitiesVinculados;

}
