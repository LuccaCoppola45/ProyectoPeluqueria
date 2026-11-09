package com.example.ProyectoPeluqueria.Features.Turno;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recordatorios_turno",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recordatorio_turno_tipo",
                columnNames = {"id_turno", "tipo"}))
@Getter
@Setter
@NoArgsConstructor
public class RecordatorioTurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno", nullable = false)
    private TurnoEntity turno;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime fechaEnvio;

    @Column(nullable = false, length = 20)
    private String estado;

    // Id que Meta le da al mensaje (wamid). La respuesta de la clienta lo trae en "context.id",
    // así se sabe a qué turno contesta aunque tenga varios pendientes.
    @Column(name = "mensaje_id", length = 128)
    private String mensajeId;
}
