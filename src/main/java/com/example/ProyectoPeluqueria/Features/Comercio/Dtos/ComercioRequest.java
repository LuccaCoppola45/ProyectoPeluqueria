package com.example.ProyectoPeluqueria.Features.Comercio.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ComercioRequest {

    @NotBlank(message = "El nombre del comercio no puede estar vacío")
    private String nombre;

    @NotBlank(message = "La dirección del comercio no puede estar vacía")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\- ]+$", message = "El teléfono contiene caracteres inválidos")
    private String telefono;

    @NotEmpty(message = "Debe seleccionar al menos una categoría para el comercio")
    private List<Long> listaCategoriasIds;



}
