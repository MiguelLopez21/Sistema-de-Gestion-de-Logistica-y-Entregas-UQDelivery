package co.edu.uniquindio.uqdelivery.controller;

import co.edu.uniquindio.uqdelivery.dto.PerfilResponse;
import co.edu.uniquindio.uqdelivery.dto.UsuarioResponse;
import co.edu.uniquindio.uqdelivery.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil del usuario autenticado y las opciones que su rol le permite ver y usar. */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public PerfilResponse miPerfil(Authentication authentication) {
        UsuarioResponse usuario = usuarioService.obtenerPorCorreo(authentication.getName());
        return new PerfilResponse(usuario, usuario.rol().getDescripcion(), usuario.rol().getOpciones());
    }
}
