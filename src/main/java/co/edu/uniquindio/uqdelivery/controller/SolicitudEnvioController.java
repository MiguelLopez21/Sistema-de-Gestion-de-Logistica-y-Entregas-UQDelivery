package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.SolicitudEnvioRequest;
import co.edu.uniquindio.uqdelivery.dto.SolicitudEnvioResponse;
import co.edu.uniquindio.uqdelivery.service.EnvioService;
import co.edu.uniquindio.uqdelivery.service.ResultadoSolicitud;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente/envios")
public class SolicitudEnvioController {

    private final EnvioService envioService;

    public SolicitudEnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    /**
     * 201 si se creó el envío; 200 si es un reintento con la misma {@code Idempotency-Key}
     * (se devuelve el envío original sin cobrar otra vez).
     */
    @PostMapping
    public ResponseEntity<SolicitudEnvioResponse> solicitar(
            @Valid @RequestBody SolicitudEnvioRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String claveIdempotencia,
            Authentication authentication) {
        ResultadoSolicitud r = envioService.solicitar(request, authentication.getName(), claveIdempotencia);
        return ResponseEntity.status(r.creado() ? HttpStatus.CREATED : HttpStatus.OK).body(r.envio());
    }
}
