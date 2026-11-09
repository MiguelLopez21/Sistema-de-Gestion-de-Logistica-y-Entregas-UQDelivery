package co.edu.uniquindio.uqdelivery.dto;

public record CierreEntregaRequest(
        String nombreReceptor,
        Double latitud,
        Double longitud,
        ArchivoEvidencia foto,
        ArchivoEvidencia firma) {
}
