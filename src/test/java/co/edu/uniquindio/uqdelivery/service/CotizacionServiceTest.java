package co.edu.uniquindio.uqdelivery.service;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CotizacionServiceTest {

    private final CotizacionService service = new CotizacionService();

    @Test
    void cotizaEnvioEstandarConPesoReal() {
        // 4000 + 5*1200 + 2*800 = 11600; IVA 2204; total 13804
        Cotizacion c = service.cotizar(
                new SolicitudCotizacion(5, 2, 30, 20, 10, TipoServicio.ESTANDAR));
        assertEquals(new BigDecimal("11600"), c.subtotal());
        assertEquals(new BigDecimal("13804"), c.total());
    }

    @Test
    void usaPesoVolumetricoCuandoEsMayor() {
        // 50*40*30 / 5000 = 12 kg
        Cotizacion c = service.cotizar(
                new SolicitudCotizacion(5, 2, 50, 40, 30, TipoServicio.EXPRESS));
        assertEquals(12.0, c.pesoFacturableKg());
        assertEquals(new BigDecimal("31360"), c.subtotal());
        assertEquals(new BigDecimal("37318"), c.total());
    }

    @Test
    void rechazaDistanciaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> service.cotizar(
                new SolicitudCotizacion(0, 2, 10, 10, 10, TipoServicio.ESTANDAR)));
    }

    @Test
    void rechazaDistanciaMayorAlMaximo() {
        assertThrows(IllegalArgumentException.class, () -> service.cotizar(
                new SolicitudCotizacion(60, 2, 10, 10, 10, TipoServicio.ESTANDAR)));
    }

    @Test
    void rechazaPesoExcesivo() {
        assertThrows(IllegalArgumentException.class, () -> service.cotizar(
                new SolicitudCotizacion(5, 40, 10, 10, 10, TipoServicio.ESTANDAR)));
    }

    @Test
    void rechazaSolicitudNula() {
        assertThrows(IllegalArgumentException.class, () -> service.cotizar(null));
    }
}