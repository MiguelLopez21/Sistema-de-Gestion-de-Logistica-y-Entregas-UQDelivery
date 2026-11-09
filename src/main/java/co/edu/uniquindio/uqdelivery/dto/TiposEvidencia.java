package co.edu.uniquindio.uqdelivery.dto;

public record TiposEvidencia(String fotoTipo, String firmaTipo) {

    public static final TiposEvidencia VACIA = new TiposEvidencia(null, null);

    public boolean tieneFoto() {
        return fotoTipo != null;
    }

    public boolean tieneFirma() {
        return firmaTipo != null;
    }
}
