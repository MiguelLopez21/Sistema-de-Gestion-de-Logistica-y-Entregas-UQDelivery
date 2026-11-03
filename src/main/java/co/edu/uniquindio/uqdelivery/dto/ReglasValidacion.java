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

    /** Direcciones: sin < > " ' ; & (XSS / inyección); permite # . , - / º °. */
    public static final String DIRECCION = "^[\\p{L}\\p{N} .,#/º°-]+$";
    public static final String DIRECCION_MSG = "La dirección solo puede contener letras, números, espacios y . , # / - º °";

    /** Descripción libre del paquete: sin < > " ' ; & ni caracteres de control. */
    public static final String DESCRIPCION = "^[\\p{L}\\p{N} .,:()¿?¡!%/-]+$";
    public static final String DESCRIPCION_MSG = "La descripción solo puede contener letras, números, espacios y signos de puntuación comunes (sin HTML ni scripts)";

    /**
     * Nombre de persona (destinatario, titular): palabras de letras, opcionalmente con punto final,
     * separadas por un único espacio, apóstrofo o guion entre letras. Rechaza "x'--", "a--b", etc.
     */
    public static final String NOMBRE_PERSONA = "^\\p{L}+\\.?(?:(?: |['-])\\p{L}+\\.?)*$";
    public static final String NOMBRE_PERSONA_MSG = "El nombre solo puede contener letras separadas por espacio, apóstrofo o guion";

    /** Clave de idempotencia: 8 a 64 caracteres alfanuméricos o guion. */
    public static final String CLAVE_IDEMPOTENCIA = "^[A-Za-z0-9-]{8,64}$";
}
