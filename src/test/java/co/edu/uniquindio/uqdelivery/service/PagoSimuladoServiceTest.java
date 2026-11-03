package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.exception.PagoRechazadoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagoSimuladoServiceTest {

    private final PagoSimuladoService service = new PagoSimuladoService();
    private static final BigDecimal MONTO = new BigDecimal("13804");

    @Test
    void apruebaTarjetaNormalYDevuelveReferenciaUnica() {
        String r1 = service.cobrar("4242424242424242", MONTO);
        String r2 = service.cobrar("4242424242424242", MONTO);
        assertTrue(r1.startsWith("PAG-"));
        assertNotEquals(r1, r2);
    }

    @Test
    void rechazaTarjetaRechazadaPorEmisor() {
        PagoRechazadoException ex = assertThrows(PagoRechazadoException.class,
                () -> service.cobrar("4000000000000000", MONTO));
        assertTrue(ex.getMessage().contains("rechazada"));
    }

    @Test
    void rechazaPorFondosInsuficientes() {
        PagoRechazadoException ex = assertThrows(PagoRechazadoException.class,
                () -> service.cobrar("4000000000001111", MONTO));
        assertTrue(ex.getMessage().contains("fondos insuficientes"));
    }

    @Test
    void rechazaMontoInvalido() {
        assertThrows(PagoRechazadoException.class, () -> service.cobrar("4242424242424242", BigDecimal.ZERO));
        assertThrows(PagoRechazadoException.class, () -> service.cobrar("4242424242424242", null));
    }

    @Test
    void rechazaTarjetaNulaOMalFormadaSinExcepcionesInternas() {
        assertThrows(PagoRechazadoException.class, () -> service.cobrar(null, MONTO));
        assertThrows(PagoRechazadoException.class, () -> service.cobrar("12", MONTO));
        assertThrows(PagoRechazadoException.class, () -> service.cobrar("abcdabcdabcdabcd", MONTO));
    }
}
