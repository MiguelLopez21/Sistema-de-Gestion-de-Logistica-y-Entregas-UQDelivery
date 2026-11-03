package co.edu.uniquindio.uqdelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZonaEntregaTest {

    @Test
    void mismaZonaCobraLaDistanciaMinima() {
        assertEquals(1.0, ZonaEntrega.ARMENIA_CENTRO.distanciaKmA(ZonaEntrega.ARMENIA_CENTRO));
    }

    @Test
    void distanciaEsSimetricaYCreceConLaLejania() {
        double cercana = ZonaEntrega.ARMENIA_CENTRO.distanciaKmA(ZonaEntrega.ARMENIA_NORTE);
        double lejana = ZonaEntrega.ARMENIA_CENTRO.distanciaKmA(ZonaEntrega.LA_TEBAIDA);
        assertEquals(cercana, ZonaEntrega.ARMENIA_NORTE.distanciaKmA(ZonaEntrega.ARMENIA_CENTRO));
        assertTrue(lejana > cercana);
        assertTrue(cercana > 1.0);
    }

    @Test
    void ningunParDeZonasExcedeElLimiteDeCobertura() {
        for (ZonaEntrega a : ZonaEntrega.values()) {
            for (ZonaEntrega b : ZonaEntrega.values()) {
                assertTrue(a.distanciaKmA(b) <= 50.0, a + " -> " + b);
            }
        }
    }
}
