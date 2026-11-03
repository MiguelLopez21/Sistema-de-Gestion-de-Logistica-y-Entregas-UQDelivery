package co.edu.uniquindio.uqdelivery.dto;

import co.edu.uniquindio.uqdelivery.model.ZonaEntrega;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudEnvioRequestTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrar() {
        fabrica.close();
    }

    private static SolicitudEnvioRequest base(String origen, String nombre, String descripcion, String tarjeta) {
        return new SolicitudEnvioRequest(ZonaEntrega.ARMENIA_CENTRO, origen, ZonaEntrega.ARMENIA_NORTE, "Carrera 15 # 8-30", nombre, "3001234567",
                2.0, 30.0, 20.0, 10.0, descripcion, TipoServicio.ESTANDAR,
                new DatosPagoRequest(tarjeta, "Juan Cliente"));
    }

    private static boolean valido(SolicitudEnvioRequest r) {
        return validador.validate(r).isEmpty();
    }

    @Test
    void solicitudCompletaEsValida() {
        assertTrue(valido(base("Calle 10 # 5-20", "Ana Pérez", "Libros de texto (frágil)", "4242424242424242")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<script>alert(1)</script>", "<img src=x onerror=alert(1)>", "\"><svg/onload=alert(1)>",
            "'; DROP TABLE envios;--", "a' OR '1'='1", "Calle 1 & Calle 2", "javascript:alert(1)<", "x'--"})
    void rechazaContenidoMaliciosoEnTodosLosCamposDeTexto(String malicioso) {
        assertFalse(valido(base(malicioso, "Ana", "Caja", "4242424242424242")), "origen: " + malicioso);
        assertFalse(valido(base("Calle 1", malicioso, "Caja", "4242424242424242")), "nombre: " + malicioso);
        assertFalse(valido(base("Calle 1", "Ana", malicioso, "4242424242424242")), "descripción: " + malicioso);
    }

    @Test
    void rechazaCamposVaciosONulos() {
        assertFalse(valido(base("", "Ana", "Caja", "4242424242424242")));
        assertFalse(valido(base("Calle 1", "  ", "Caja", "4242424242424242")));
        assertFalse(valido(base("Calle 1", "Ana", null, "4242424242424242")));
        assertFalse(valido(new SolicitudEnvioRequest(null, null, null, null, null, null, null, null, null,
                null, null, null, null)));
    }

    @Test
    void rechazaTarjetaMalFormada() {
        assertFalse(valido(base("Calle 1", "Ana", "Caja", "1234")));
        assertFalse(valido(base("Calle 1", "Ana", "Caja", "4242-4242-4242-4242")));
    }

    private static SolicitudEnvioRequest conMedidas(double peso, double largo) {
        return new SolicitudEnvioRequest(ZonaEntrega.CALARCA, "Calle 1", ZonaEntrega.MONTENEGRO, "Calle 2",
                "Ana", "3001234567", peso, largo, 20.0, 10.0, "Caja", TipoServicio.ESTANDAR,
                new DatosPagoRequest("4242424242424242", "Juan"));
    }

    @Test
    void rechazaPesoYDimensionesFueraDeRango() {
        assertTrue(valido(conMedidas(2.0, 30.0)));
        assertFalse(valido(conMedidas(31.0, 30.0)));
        assertFalse(valido(conMedidas(2.0, -1.0)));
        assertFalse(valido(conMedidas(2.0, 201.0)));
    }

    @Test
    void rechazaNombresDePersonaConPuntuacionSospechosa() {
        for (String n : new String[]{"x'--", "a--b", "O''Brien", "Ana1", "Ana'", "-Ana", "Ana  Perez", "'; DROP"}) {
            assertFalse(valido(base("Calle 1", n, "Caja", "4242424242424242")), n);
        }
        for (String n : new String[]{"Ana Pérez", "O'Brien", "Jean-Luc Picard", "J. Pérez", "María José"}) {
            assertTrue(valido(base("Calle 1", n, "Caja", "4242424242424242")), n);
        }
    }

    @Test
    void rechazaTitularConPuntuacionSospechosa() {
        SolicitudEnvioRequest r = new SolicitudEnvioRequest(ZonaEntrega.CALARCA, "Calle 1", ZonaEntrega.MONTENEGRO,
                "Calle 2", "Ana", "3001234567", 2.0, 30.0, 20.0, 10.0, "Caja", TipoServicio.ESTANDAR,
                new DatosPagoRequest("4242424242424242", "x'--"));
        assertFalse(valido(r));
    }

    @Test
    void toStringDeDatosPagoNoExponeLaTarjeta() {
        assertFalse(new DatosPagoRequest("4242424242424242", "Juan").toString().contains("4242424242424242"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Calle 1\n<b>x</b>", "Calle 1\r\nSet-Cookie: x", "Calle\t1",
            "Calle 1\u202E", "Calle\u200B1", "Calle 1\u0000"})
    void rechazaSaltosDeLineaYCaracteresUnicodeDeControl(String texto) {
        assertFalse(valido(base(texto, "Ana", "Caja", "4242424242424242")), "origen");
        assertFalse(valido(base("Calle 1", "Ana", texto, "4242424242424242")), "descripción");
    }
}
