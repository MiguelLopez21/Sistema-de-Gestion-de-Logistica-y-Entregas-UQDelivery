package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.EnvioResponse;
import co.edu.uniquindio.uqdelivery.service.AsignacionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repartidor/entregas")
public class EntregaController {

    private final AsignacionService asignacionService;

    public EntregaController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping
    public List<EnvioResponse> misEntregas(Authentication authentication) {
        return asignacionService.entregasDelRepartidor(authentication.getName());
    }
}
