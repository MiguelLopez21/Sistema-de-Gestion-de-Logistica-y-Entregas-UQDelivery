package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Rol;
import jakarta.validation.constraints.*;

/** La contraseña no se edita aquí; su recuperación corresponde a HU-03. */
public record UsuarioActualizarRequest(

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

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}