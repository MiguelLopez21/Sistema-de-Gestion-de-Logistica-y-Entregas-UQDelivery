package co.edu.uniquindio.uqdelivery.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones_envio")
public class AsignacionEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_anterior_id")
    private Usuario repartidorAnterior;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repartidor_nuevo_id", nullable = false)
    private Usuario repartidorNuevo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private Usuario administrador;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(length = 255)
    private String motivo;

    @PrePersist
    void antesDePersistir() {
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }
    public Usuario getRepartidorAnterior() { return repartidorAnterior; }
    public void setRepartidorAnterior(Usuario repartidorAnterior) { this.repartidorAnterior = repartidorAnterior; }
    public Usuario getRepartidorNuevo() { return repartidorNuevo; }
    public void setRepartidorNuevo(Usuario repartidorNuevo) { this.repartidorNuevo = repartidorNuevo; }
    public Usuario getAdministrador() { return administrador; }
    public void setAdministrador(Usuario administrador) { this.administrador = administrador; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
