package com.example.ProyectoPeluqueria.Features.Resenia.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReseniaRequest {

    @NotNull(message = "El comercio es obligatorio")
    @Positive(message = "El ID del comercio debe ser positivo")
    private Long comercioId;

    @NotNull(message = "La valoración es obligatoria")
    @Min(value = 1, message = "La valoración mínima es 1")
    @Max(value = 5, message = "La valoración máxima es 5")
    private Integer valoracion;

    @Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres")
    private String comentario;
}
