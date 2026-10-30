package co.edu.uniquindio.uqdelivery.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsignacionRequestTest {

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

    private boolean esValido(AsignacionRequest request) {
        return validador.validate(request).isEmpty();
    }

    @Test
    void aceptaRepartidorConMotivo() {
        assertTrue(esValido(new AsignacionRequest(2L, "Moto averiada, tráfico (zona norte)")));
    }

    @Test
    void aceptaMotivoNulo() {
        assertTrue(esValido(new AsignacionRequest(2L, null)));
    }

    @Test
    void rechazaRepartidorNulo() {
        assertFalse(esValido(new AsignacionRequest(null, null)));
    }

    @Test
    void rechazaRepartidorNoPositivo() {
        assertFalse(esValido(new AsignacionRequest(0L, null)));
        assertFalse(esValido(new AsignacionRequest(-3L, null)));
    }

    @Test
    void rechazaMotivoConScriptXss() {
        assertFalse(esValido(new AsignacionRequest(2L, "<script>alert(1)</script>")));
    }

    @Test
    void rechazaMotivoMayorA255Caracteres() {
        assertFalse(esValido(new AsignacionRequest(2L, "a".repeat(256))));
        assertTrue(esValido(new AsignacionRequest(2L, "a".repeat(255))));
    }
}
