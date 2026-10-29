package co.edu.uniquindio.uqdelivery.model;

import java.util.List;

/**
 * Roles del sistema (RBAC). El Cliente Receptor no se modela porque no requiere registro:
 * consulta por código de seguimiento (HU-08).
 */
public enum Rol {

    ADMINISTRADOR("Administrador del Sistema", List.of(
            "Gestión de usuarios",
            "Asignación de repartidores",
            "Panel de control geográfico",
            "Dashboard de métricas")),

    CLIENTE_EMISOR("Cliente Emisor", List.of(
            "Solicitar envío",
            "Cotizar envío",
            "Historial de envíos")),

    REPARTIDOR("Repartidor", List.of(
            "Mis entregas",
            "Ruta óptima",
            "Reportar novedad",
            "Cerrar entrega"));

    private final String descripcion;
    private final List<String> opciones;

    Rol(String descripcion, List<String> opciones) {
        this.descripcion = descripcion;
        this.opciones = opciones;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Opciones de menú/funcionalidades que el rol puede ver y usar. */
    public List<String> getOpciones() {
        return opciones;
    }
}
