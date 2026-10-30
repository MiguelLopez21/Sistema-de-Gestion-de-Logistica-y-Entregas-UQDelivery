package co.edu.uniquindio.uqdelivery.dto;

/** Reglas de validación compartidas por los DTO (validación del lado del servidor). */
public final class ReglasValidacion {

    private ReglasValidacion() {
    }

    /** Letras, números, espacios y . , ' - ; rechaza < > " & / etc. (defensa contra XSS). */
    public static final String NOMBRE = "^[\\p{L}\\p{N} .,'-]+$";
    public static final String NOMBRE_MSG = "El nombre solo puede contener letras, números, espacios y . , ' -";

    public static final String TELEFONO = "^\\+?[0-9]{7,15}$";
    public static final String TELEFONO_MSG = "El teléfono debe tener entre 7 y 15 dígitos (puede iniciar con +)";

    /** Misma política que HU-01: mínimo 8 caracteres, con una mayúscula y un número. */
    public static final String PASSWORD = "^(?=.*[A-Z])(?=.*\\d).+$";
    public static final String PASSWORD_MSG = "La contraseña debe tener al menos una mayúscula y un número";

    public static final String MOTIVO = "^[\\p{L}\\p{N} .,;:'()¿?¡!%/-]*$";
    public static final String MOTIVO_MSG = "El motivo solo puede contener letras, números, espacios y signos de puntuación comunes";
}
