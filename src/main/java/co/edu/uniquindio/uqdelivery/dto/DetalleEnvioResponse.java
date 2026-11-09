package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.ZonaEntrega;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleEnvioResponse(
        Long id,
        String codigoSeguimiento,
        EstadoEnvio estado,
        ZonaEntrega zonaOrigen,
        String direccionOrigen,
        ZonaEntrega zonaDestino,
        String direccionDestino,
        String destinatarioNombre,
        String destinatarioTelefono,
        Double pesoKg,
        Double largoCm,
        Double anchoCm,
        Double altoCm,
        Double distanciaKm,
        String descripcion,
        TipoServicio tipoServicio,
        BigDecimal valorCotizado,
        String referenciaPago,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaEntrega,
        Long repartidorId,
        String repartidorNombre,
        String nombreReceptor,
        Double latitudEntrega,
        Double longitudEntrega,
        boolean tieneFoto,
        boolean tieneFirma) {

    public static DetalleEnvioResponse desde(Envio e, TiposEvidencia evidencia) {
        boolean asignado = e.getRepartidor() != null;
        return new DetalleEnvioResponse(
                e.getId(),
                e.getCodigoSeguimiento(),
                e.getEstado(),
                e.getZonaOrigen(),
                e.getDireccionOrigen(),
                e.getZonaDestino(),
                e.getDireccionDestino(),
                e.getDestinatarioNombre(),
                e.getDestinatarioTelefono(),
                e.getPesoKg(),
                e.getLargoCm(),
                e.getAnchoCm(),
                e.getAltoCm(),
                e.getDistanciaKm(),
                e.getDescripcion(),
                e.getTipoServicio(),
                e.getValorCotizado(),
                e.getReferenciaPago(),
                e.getFechaCreacion(),
                e.getFechaEntrega(),
                asignado ? e.getRepartidor().getId() : null,
                asignado ? e.getRepartidor().getNombre() : null,
                e.getNombreReceptor(),
                e.getLatitudEntrega(),
                e.getLongitudEntrega(),
                evidencia.tieneFoto(),
                evidencia.tieneFirma());
    }
}
