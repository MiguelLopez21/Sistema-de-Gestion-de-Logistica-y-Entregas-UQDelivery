package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.ArchivoEvidencia;
import co.edu.uniquindio.uqdelivery.dto.CierreEntregaRequest;
import co.edu.uniquindio.uqdelivery.dto.DetalleEnvioResponse;
import co.edu.uniquindio.uqdelivery.dto.EnvioResponse;
import co.edu.uniquindio.uqdelivery.service.AsignacionService;
import co.edu.uniquindio.uqdelivery.service.CierreEntregaService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/repartidor/entregas")
public class EntregaController {

    private final AsignacionService asignacionService;
    private final CierreEntregaService cierreEntregaService;

    public EntregaController(AsignacionService asignacionService, CierreEntregaService cierreEntregaService) {
        this.asignacionService = asignacionService;
        this.cierreEntregaService = cierreEntregaService;
    }

    @GetMapping
    public List<EnvioResponse> misEntregas(Authentication authentication) {
        return asignacionService.entregasDelRepartidor(authentication.getName());
    }

    @PostMapping(path = "/{id}/cierre", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DetalleEnvioResponse cerrar(@PathVariable Long id,
                                       @RequestParam(required = false) String nombreReceptor,
                                       @RequestParam(required = false) Double latitud,
                                       @RequestParam(required = false) Double longitud,
                                       @RequestParam(required = false) MultipartFile foto,
                                       @RequestParam(required = false) MultipartFile firma,
                                       Authentication authentication) throws IOException {
        CierreEntregaRequest solicitud = new CierreEntregaRequest(
                nombreReceptor, latitud, longitud, archivo(foto), archivo(firma));
        return cierreEntregaService.cerrar(id, authentication.getName(), solicitud);
    }

    private ArchivoEvidencia archivo(MultipartFile parte) throws IOException {
        if (parte == null || parte.isEmpty()) {
            return null;
        }
        String tipo = parte.getContentType() == null
                ? null
                : parte.getContentType().split(";")[0].trim().toLowerCase(Locale.ROOT);
        return new ArchivoEvidencia(tipo, parte.getBytes());
    }
}
