package com.example.ProyectoPeluqueria.Features.Usuario.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResquestDTO {

    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos")
    private String dni;

    @Email(message = "El email debe cumplir con el formato estándar")
    private String email;

    @NotBlank(message = "la contraseña es obligatoria")
    @Size(min = 6)
    private String password;

    @AssertTrue(message = "Debe ingresar el DNI")
    public boolean isDniOrLegacyEmailProvided() {
        return (dni != null && !dni.isBlank()) || (email != null && !email.isBlank());
    }
}
