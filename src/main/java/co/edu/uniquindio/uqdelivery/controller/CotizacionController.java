package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.CotizacionRequest;
import co.edu.uniquindio.uqdelivery.dto.CotizacionResponse;
import co.edu.uniquindio.uqdelivery.exception.SolicitudInvalidaException;
import co.edu.uniquindio.uqdelivery.service.CotizacionService;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.Cotizacion;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.SolicitudCotizacion;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/cliente/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    public CotizacionResponse cotizar(@Valid @RequestBody CotizacionRequest request) {
        double distanciaKm = request.zonaOrigen().distanciaKmA(request.zonaDestino());
        Cotizacion cotizacion;
        try {
            cotizacion = cotizacionService.cotizar(new SolicitudCotizacion(
                    distanciaKm, request.pesoKg(), request.largoCm(), request.anchoCm(),
                    request.altoCm(), request.tipoServicio()));
        } catch (IllegalArgumentException ex) {
            throw new SolicitudInvalidaException(ex.getMessage());
        }
        return CotizacionResponse.desde(request, distanciaKm, cotizacion);
    }
}
