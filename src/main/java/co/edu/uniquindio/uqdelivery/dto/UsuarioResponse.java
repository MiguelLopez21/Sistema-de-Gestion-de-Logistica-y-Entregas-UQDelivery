package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;

import java.time.LocalDateTime;

/** Nunca expone la contraseña (ni su hash). */
public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        Rol rol,
        boolean activo,
        LocalDateTime fechaCreacion) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                u.getRol(), u.isActivo(), u.getFechaCreacion());
    }
}
