package co.edu.uniquindio.uqdelivery.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado = EstadoEnvio.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaEntrega;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @PrePersist
    void antesDePersistir() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}
