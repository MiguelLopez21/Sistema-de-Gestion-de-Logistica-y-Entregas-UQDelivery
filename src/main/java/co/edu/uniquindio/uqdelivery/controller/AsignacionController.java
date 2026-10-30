package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.AsignacionRequest;
import co.edu.uniquindio.uqdelivery.dto.AsignacionResponse;
import co.edu.uniquindio.uqdelivery.dto.EnvioResponse;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/envios")
public class AsignacionController {

    private static final int TAMANIO_MAXIMO = 100;

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping
    public PagedModel<EnvioResponse> listar(
            @RequestParam(required = false) EstadoEnvio estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int pagina = Math.max(page, 0);
        int tamanio = Math.min(Math.max(size, 1), TAMANIO_MAXIMO);
        return new PagedModel<>(asignacionService.listarEnvios(estado,
                PageRequest.of(pagina, tamanio, Sort.by("id"))));
    }

    @PostMapping("/{id}/asignacion")
    public EnvioResponse asignar(@PathVariable Long id,
                                 @Valid @RequestBody AsignacionRequest request,
                                 Authentication authentication) {
        return asignacionService.asignar(id, request, authentication.getName());
    }

    @PutMapping("/{id}/asignacion")
    public EnvioResponse reasignar(@PathVariable Long id,
                                   @Valid @RequestBody AsignacionRequest request,
                                   Authentication authentication) {
        return asignacionService.reasignar(id, request, authentication.getName());
    }

    @GetMapping("/{id}/asignaciones")
    public List<AsignacionResponse> historial(@PathVariable Long id) {
        return asignacionService.historial(id);
    }
}
