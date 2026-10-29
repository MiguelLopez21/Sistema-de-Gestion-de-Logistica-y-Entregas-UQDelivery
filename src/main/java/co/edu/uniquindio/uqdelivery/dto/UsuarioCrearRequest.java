package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Rol;
import jakarta.validation.constraints.*;

public record UsuarioCrearRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        @Pattern(regexp = ReglasValidacion.NOMBRE, message = ReglasValidacion.NOMBRE_MSG)
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = ReglasValidacion.TELEFONO, message = ReglasValidacion.TELEFONO_MSG)
        String telefono,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        @Pattern(regexp = ReglasValidacion.PASSWORD, message = ReglasValidacion.PASSWORD_MSG)
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}
