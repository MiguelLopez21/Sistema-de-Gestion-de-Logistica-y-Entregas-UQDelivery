package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.ArchivoEvidencia;
import co.edu.uniquindio.uqdelivery.dto.DetalleEnvioResponse;
import co.edu.uniquindio.uqdelivery.service.ConsultaEnvioService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/cliente/envios", "/api/admin/envios"})
public class DetalleEnvioController {

    private final ConsultaEnvioService consultaEnvioService;

    public DetalleEnvioController(ConsultaEnvioService consultaEnvioService) {
        this.consultaEnvioService = consultaEnvioService;
    }

    @GetMapping("/{id}")
    public DetalleEnvioResponse detalle(@PathVariable Long id, Authentication authentication) {
        return consultaEnvioService.detalle(id, authentication.getName());
    }

    @GetMapping("/{id}/evidencia/{tipo}")
    public ResponseEntity<byte[]> evidencia(@PathVariable Long id,
                                            @PathVariable String tipo,
                                            Authentication authentication) {
        ArchivoEvidencia archivo = consultaEnvioService.evidencia(id, tipo, authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipo()))
                .cacheControl(CacheControl.noStore())
                .body(archivo.contenido());
    }
}
