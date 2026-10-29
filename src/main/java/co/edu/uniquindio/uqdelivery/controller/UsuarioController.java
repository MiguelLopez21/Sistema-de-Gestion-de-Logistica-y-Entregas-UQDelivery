package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.CambioRolRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioActualizarRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioCrearRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioResponse;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * HU-02: gestión de usuarios. Todo /api/admin/** exige rol ADMINISTRADOR (ver SecurityConfig);
 * cualquier otro rol recibe 403.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioController {

    private static final int TAMANIO_MAXIMO = 100;

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    public PagedModel<UsuarioResponse> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int pagina = Math.max(page, 0);
        int tamanio = Math.min(Math.max(size, 1), TAMANIO_MAXIMO);
        return new PagedModel<>(usuarioService.listar(rol, activo,
                PageRequest.of(pagina, tamanio, Sort.by("id"))));
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioActualizarRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody CambioRolRequest request) {
        return usuarioService.cambiarRol(id, request.rol());
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioResponse desactivar(@PathVariable Long id, Authentication authentication) {
        return usuarioService.desactivar(id, authentication.getName());
    }

    @PatchMapping("/{id}/activar")
    public UsuarioResponse activar(@PathVariable Long id) {
        return usuarioService.activar(id);
    }
}
