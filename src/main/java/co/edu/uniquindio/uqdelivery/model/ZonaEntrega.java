package co.edu.uniquindio.uqdelivery.model;

/**
 * Catálogo de zonas de cobertura. El servidor calcula la distancia a partir de estas coordenadas,
 * de modo que el cliente no puede manipular la distancia (y por tanto el precio).
 */
public enum ZonaEntrega {
    ARMENIA_CENTRO(4.5339, -75.6811),
    ARMENIA_NORTE(4.5600, -75.6700),
    ARMENIA_SUR(4.5000, -75.6850),
    ARMENIA_ORIENTE(4.5400, -75.6500),
    ARMENIA_OCCIDENTE(4.5300, -75.7100),
    CALARCA(4.5290, -75.6430),
    MONTENEGRO(4.5670, -75.7510),
    LA_TEBAIDA(4.4530, -75.7890);

    private static final double RADIO_TIERRA_KM = 6371.0;
    /** La ruta por calle es más larga que la línea recta. */
    private static final double FACTOR_RUTA = 1.3;
    private static final double DISTANCIA_MINIMA_KM = 1.0;

    private final double latitud;
    private final double longitud;

    ZonaEntrega(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /** Distancia estimada por ruta en km (haversine × factor), redondeada a 1 decimal y mínimo 1 km. */
    public double distanciaKmA(ZonaEntrega otra) {
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.pow(Math.sin(dLon / 2), 2);
        double recta = 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(a));
        double ruta = Math.round(recta * FACTOR_RUTA * 10.0) / 10.0;
        return Math.max(DISTANCIA_MINIMA_KM, ruta);
    }
}
