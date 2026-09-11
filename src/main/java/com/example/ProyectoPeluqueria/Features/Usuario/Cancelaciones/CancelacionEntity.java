package com.example.ProyectoPeluqueria.Features.Usuario.Cancelaciones;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cancelaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CancelacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_cancelacion", nullable = false, updatable = false)
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo", length = 500, nullable = false, updatable = false)
    private String motivo;






}
