package co.edu.uniquindio.uqdelivery.dto;

import java.util.List;

/** Perfil del usuario autenticado + las opciones que su rol puede ver (el frontend solo pinta estas). */
public record PerfilResponse(
        UsuarioResponse usuario,
        String rolDescripcion,
        List<String> opciones) {
}