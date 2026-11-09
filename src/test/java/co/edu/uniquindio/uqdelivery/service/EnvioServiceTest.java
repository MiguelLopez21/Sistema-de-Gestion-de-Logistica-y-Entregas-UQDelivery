package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.DatosPagoRequest;
import co.edu.uniquindio.uqdelivery.dto.SolicitudEnvioRequest;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.PagoRechazadoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.exception.SolicitudInvalidaException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.model.ZonaEntrega;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.TipoServicio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    private static final String OK = "4242424242424242";

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PagoSimuladoService pagoMock;
    @Mock
    private NotificationService notificationService;

    private final CotizacionService cotizacion = new CotizacionService();

    private EnvioService service() {
        return new EnvioService(envioRepository, usuarioRepository, cotizacion,
                new PagoSimuladoService(), notificationService);
    }

    @AfterEach
    void limpiarSincronizacion() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static SolicitudEnvioRequest solicitud(String tarjeta, double peso) {
        // Misma zona => distancia mínima 1 km: 4000 + 1200 + peso*800
        return new SolicitudEnvioRequest(ZonaEntrega.ARMENIA_CENTRO, "Calle 10 # 5-20",
                ZonaEntrega.ARMENIA_CENTRO, "Carrera 15 # 8-30", "Ana Pérez", "3001234567",
                peso, 30.0, 20.0, 10.0, "Libros de texto", TipoServicio.ESTANDAR,
                new DatosPagoRequest(tarjeta, "Juan Cliente"));
    }

    private Usuario clienteExistente() {
        Usuario c = new Usuario();
        c.setId(7L);
        c.setCorreo("cliente@uq.com");
        c.setRol(Rol.CLIENTE_EMISOR);
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@uq.com")).thenReturn(Optional.of(c));
        return c;
    }

    @Test
    void pagoAprobadoCreaEnvioPendienteConCodigoYValorCotizado() {
        Usuario cliente = clienteExistente();
        when(envioRepository.existsByCodigoSeguimiento(any())).thenReturn(false);

        // 4000 + 1*1200 + 2*800 = 6800; IVA 1292; total 8092
        ResultadoSolicitud r = service().solicitar(solicitud(OK, 2), " Cliente@UQ.com ", null);

        ArgumentCaptor<Envio> captor = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).saveAndFlush(captor.capture());
        Envio guardado = captor.getValue();
        assertTrue(r.creado());
        assertEquals(EstadoEnvio.PENDIENTE, guardado.getEstado());
        assertSame(cliente, guardado.getCliente());
        assertEquals(1.0, guardado.getDistanciaKm());
        assertEquals(new BigDecimal("8092"), guardado.getValorCotizado());
        assertEquals(new BigDecimal("8092"), guardado.getValorTotal());
        assertTrue(guardado.getCodigoSeguimiento().matches("UQD-[A-Z2-9]{10}"));
        assertNotNull(guardado.getReferenciaPago());
        assertEquals(guardado.getCodigoSeguimiento(), r.envio().codigoSeguimiento());
    }

    @Test
    void laDistanciaLaCalculaElServidorSegunLasZonas() {
        clienteExistente();
        SolicitudEnvioRequest r = new SolicitudEnvioRequest(ZonaEntrega.ARMENIA_CENTRO, "Calle 1",
                ZonaEntrega.LA_TEBAIDA, "Calle 2", "Ana", "3001234567", 2.0, 30.0, 20.0, 10.0, "Caja",
                TipoServicio.ESTANDAR, new DatosPagoRequest(OK, "Juan"));

        service().solicitar(r, "cliente@uq.com", null);

        ArgumentCaptor<Envio> c = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).saveAndFlush(c.capture());
        assertEquals(ZonaEntrega.ARMENIA_CENTRO.distanciaKmA(ZonaEntrega.LA_TEBAIDA), c.getValue().getDistanciaKm());
        assertTrue(c.getValue().getDistanciaKm() > 1.0);
    }

    @Test
    void pagoRechazadoLanzaExcepcionParaQueLaTransaccionHagaRollback() {
        clienteExistente();

        assertThrows(PagoRechazadoException.class,
                () -> service().solicitar(solicitud("4000000000000000", 2), "cliente@uq.com", null));
    }

    @Test
    void claveRepetidaDevuelveElEnvioOriginalSinCobrarNiCrear() {
        Usuario cliente = clienteExistente();
        Envio previo = new Envio();
        previo.setCodigoSeguimiento("UQD-AAAAAAAAAA");
        previo.setValorCotizado(new BigDecimal("8092"));
        when(envioRepository.findByClienteIdAndClaveIdempotencia(cliente.getId(), "clave-12345"))
                .thenReturn(Optional.of(previo));
        EnvioService conMock = new EnvioService(envioRepository, usuarioRepository, cotizacion,
                pagoMock, notificationService);

        ResultadoSolicitud r = conMock.solicitar(solicitud(OK, 2), "cliente@uq.com", "clave-12345");

        assertFalse(r.creado());
        assertEquals("UQD-AAAAAAAAAA", r.envio().codigoSeguimiento());
        verify(envioRepository, never()).saveAndFlush(any());
        verifyNoInteractions(pagoMock);
    }

    @Test
    void claveNuevaSeGuardaConElEnvio() {
        clienteExistente();
        when(envioRepository.findByClienteIdAndClaveIdempotencia(7L, "clave-12345")).thenReturn(Optional.empty());

        service().solicitar(solicitud(OK, 2), "cliente@uq.com", "clave-12345");

        ArgumentCaptor<Envio> c = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).saveAndFlush(c.capture());
        assertEquals("clave-12345", c.getValue().getClaveIdempotencia());
    }

    @Test
    void claveMalFormadaSeRechaza() {
        assertThrows(SolicitudInvalidaException.class,
                () -> service().solicitar(solicitud(OK, 2), "cliente@uq.com", "corta"));
        assertThrows(SolicitudInvalidaException.class,
                () -> service().solicitar(solicitud(OK, 2), "cliente@uq.com", "clave con espacios y <b>"));
    }

    @Test
    void colisionAlPersistirFallaAntesDeCobrar() {
        clienteExistente();
        when(envioRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk"));
        EnvioService conMock = new EnvioService(envioRepository, usuarioRepository, cotizacion,
                pagoMock, notificationService);

        assertThrows(ConflictoException.class, () -> conMock.solicitar(solicitud(OK, 2), "cliente@uq.com", null));

        verifyNoInteractions(pagoMock);
    }

    @Test
    void siLaTransaccionHaceRollbackDespuesDeCobrarSeAnulaElPago() {
        clienteExistente();
        when(pagoMock.cobrar(any(), any())).thenReturn("PAG-REF123");
        EnvioService conMock = new EnvioService(envioRepository, usuarioRepository, cotizacion,
                pagoMock, notificationService);
        TransactionSynchronizationManager.initSynchronization();

        conMock.solicitar(solicitud(OK, 2), "cliente@uq.com", null);

        List<TransactionSynchronization> sincs = TransactionSynchronizationManager.getSynchronizations();
        sincs.forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(pagoMock).anular("PAG-REF123");
    }

    @Test
    void siLaTransaccionSeConfirmaNoSeAnulaElPago() {
        clienteExistente();
        when(pagoMock.cobrar(any(), any())).thenReturn("PAG-REF123");
        EnvioService conMock = new EnvioService(envioRepository, usuarioRepository, cotizacion,
                pagoMock, notificationService);
        TransactionSynchronizationManager.initSynchronization();

        conMock.solicitar(solicitud(OK, 2), "cliente@uq.com", null);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
        verify(pagoMock, never()).anular(any());
    }

    @Test
    void reintentaSiElCodigoGeneradoYaExiste() {
        clienteExistente();
        when(envioRepository.existsByCodigoSeguimiento(any())).thenReturn(true, false);

        service().solicitar(solicitud(OK, 2), "cliente@uq.com", null);

        verify(envioRepository, times(2)).existsByCodigoSeguimiento(any());
    }

    @Test
    void fallaSiNoSePuedeGenerarCodigoUnico() {
        when(envioRepository.existsByCodigoSeguimiento(any())).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service().generarCodigoUnico());
    }

    @Test
    void pesoVolumetricoExcesivoSeTraduceEnSolicitudInvalida() {
        clienteExistente();
        // 200*200*200/5000 = 1600 kg facturables > 30
        SolicitudEnvioRequest r = new SolicitudEnvioRequest(ZonaEntrega.CALARCA, "Calle 1", ZonaEntrega.CALARCA,
                "Calle 2", "Ana", "3001234567", 2.0, 200.0, 200.0, 200.0, "Caja", TipoServicio.ESTANDAR,
                new DatosPagoRequest(OK, "Juan"));

        assertThrows(SolicitudInvalidaException.class, () -> service().solicitar(r, "cliente@uq.com", null));
        verify(envioRepository, never()).saveAndFlush(any());
    }

    @Test
    void clienteInexistenteLanzaNoEncontrado() {
        when(usuarioRepository.findByCorreoIgnoreCase(any())).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> service().solicitar(solicitud(OK, 2), "nadie@uq.com", null));
    }
}
