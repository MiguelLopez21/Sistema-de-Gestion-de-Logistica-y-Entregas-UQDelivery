package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.ZonaEntrega;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * Los textos libres se validan con listas blancas de caracteres: el contenido con HTML/script
 * (&lt; &gt; comillas, etc.) se rechaza con 400, no se acepta ni se persiste.
 */
public record SolicitudEnvioRequest(
        @NotNull(message = "La zona de origen es obligatoria")
        ZonaEntrega zonaOrigen,

        @NotBlank(message = "La dirección de origen es obligatoria")
        @Size(max = 200, message = "Máximo 200 caracteres")
        @Pattern(regexp = ReglasValidacion.DIRECCION, message = ReglasValidacion.DIRECCION_MSG)
        String direccionOrigen,

        @NotNull(message = "La zona de destino es obligatoria")
        ZonaEntrega zonaDestino,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(max = 200, message = "Máximo 200 caracteres")
        @Pattern(regexp = ReglasValidacion.DIRECCION, message = ReglasValidacion.DIRECCION_MSG)
        String direccionDestino,

        @NotBlank(message = "El nombre del destinatario es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        @Pattern(regexp = ReglasValidacion.NOMBRE_PERSONA, message = ReglasValidacion.NOMBRE_PERSONA_MSG)
        String destinatarioNombre,

        @NotBlank(message = "El teléfono del destinatario es obligatorio")
        @Pattern(regexp = ReglasValidacion.TELEFONO, message = ReglasValidacion.TELEFONO_MSG)
        String destinatarioTelefono,

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor a 0")
        @DecimalMax(value = "30", message = "El peso excede el máximo de 30 kg")
        Double pesoKg,

        @NotNull(message = "El largo es obligatorio")
        @Positive(message = "El largo debe ser mayor a 0")
        @DecimalMax(value = "200", message = "El largo excede 200 cm")
        Double largoCm,

        @NotNull(message = "El ancho es obligatorio")
        @Positive(message = "El ancho debe ser mayor a 0")
        @DecimalMax(value = "200", message = "El ancho excede 200 cm")
        Double anchoCm,

        @NotNull(message = "El alto es obligatorio")
        @Positive(message = "El alto debe ser mayor a 0")
        @DecimalMax(value = "200", message = "El alto excede 200 cm")
        Double altoCm,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 300, message = "Máximo 300 caracteres")
        @Pattern(regexp = ReglasValidacion.DESCRIPCION, message = ReglasValidacion.DESCRIPCION_MSG)
        String descripcion,

        @NotNull(message = "El tipo de servicio es obligatorio")
        TipoServicio tipoServicio,

        @NotNull(message = "Los datos de pago son obligatorios")
        @Valid
        DatosPagoRequest pago) {
}
