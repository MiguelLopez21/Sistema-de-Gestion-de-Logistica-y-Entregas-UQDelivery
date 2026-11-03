package co.edu.uniquindio.uqdelivery.model;

import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Las columnas de la solicitud (HU-03) son nullables a nivel de BD para no romper envíos creados
 * por otros flujos; su obligatoriedad se exige en {@code SolicitudEnvioRequest}.
 */
@Entity
@Table(name = "envios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_envios_codigo", columnNames = "codigoSeguimiento"),
                @UniqueConstraint(name = "uk_envios_cliente_idem", columnNames = {"cliente_id", "claveIdempotencia"})})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @Column(length = 20)
    private String codigoSeguimiento;

    @Column(length = 200)
    private String direccionOrigen;
    @Column(length = 200)
    private String direccionDestino;

    @Column(length = 100)
    private String destinatarioNombre;
    @Column(length = 16)
    private String destinatarioTelefono;

    private Double pesoKg;
    private Double largoCm;
    private Double anchoCm;
    private Double altoCm;
    private Double distanciaKm;

    @Column(length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TipoServicio tipoServicio;

    @Column(precision = 12, scale = 0)
    private BigDecimal valorCotizado;

    @Column(length = 40)
    private String referenciaPago;

    /** Cabecera Idempotency-Key del cliente: evita duplicar envío y cobro ante reintentos. */
    @Column(length = 64)
    private String claveIdempotencia;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ZonaEntrega zonaOrigen;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ZonaEntrega zonaDestino;

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
    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }
    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }
    public String getDireccionOrigen() { return direccionOrigen; }
    public void setDireccionOrigen(String direccionOrigen) { this.direccionOrigen = direccionOrigen; }
    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }
    public String getDestinatarioNombre() { return destinatarioNombre; }
    public void setDestinatarioNombre(String destinatarioNombre) { this.destinatarioNombre = destinatarioNombre; }
    public String getDestinatarioTelefono() { return destinatarioTelefono; }
    public void setDestinatarioTelefono(String destinatarioTelefono) { this.destinatarioTelefono = destinatarioTelefono; }
    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }
    public Double getLargoCm() { return largoCm; }
    public void setLargoCm(Double largoCm) { this.largoCm = largoCm; }
    public Double getAnchoCm() { return anchoCm; }
    public void setAnchoCm(Double anchoCm) { this.anchoCm = anchoCm; }
    public Double getAltoCm() { return altoCm; }
    public void setAltoCm(Double altoCm) { this.altoCm = altoCm; }
    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(TipoServicio tipoServicio) { this.tipoServicio = tipoServicio; }
    public BigDecimal getValorCotizado() { return valorCotizado; }
    public void setValorCotizado(BigDecimal valorCotizado) { this.valorCotizado = valorCotizado; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public void setClaveIdempotencia(String claveIdempotencia) { this.claveIdempotencia = claveIdempotencia; }
    public ZonaEntrega getZonaOrigen() { return zonaOrigen; }
    public void setZonaOrigen(ZonaEntrega zonaOrigen) { this.zonaOrigen = zonaOrigen; }
    public ZonaEntrega getZonaDestino() { return zonaDestino; }
    public void setZonaDestino(ZonaEntrega zonaDestino) { this.zonaDestino = zonaDestino; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}
