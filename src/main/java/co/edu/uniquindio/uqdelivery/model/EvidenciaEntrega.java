package co.edu.uniquindio.uqdelivery.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencias_entrega",
        uniqueConstraints = @UniqueConstraint(name = "uk_evidencias_envio", columnNames = "envio_id"))
public class EvidenciaEntrega {

    public static final int TAMANIO_MAXIMO_BYTES = 2 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @Column(length = TAMANIO_MAXIMO_BYTES)
    private byte[] foto;

    @Column(length = 20)
    private String fotoTipo;

    @Column(length = TAMANIO_MAXIMO_BYTES)
    private byte[] firma;

    @Column(length = 20)
    private String firmaTipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }
    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
    public String getFotoTipo() { return fotoTipo; }
    public void setFotoTipo(String fotoTipo) { this.fotoTipo = fotoTipo; }
    public byte[] getFirma() { return firma; }
    public void setFirma(byte[] firma) { this.firma = firma; }
    public String getFirmaTipo() { return firmaTipo; }
    public void setFirmaTipo(String firmaTipo) { this.firmaTipo = firmaTipo; }
}
