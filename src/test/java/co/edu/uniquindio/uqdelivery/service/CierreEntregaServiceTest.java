package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.ArchivoEvidencia;
import co.edu.uniquindio.uqdelivery.dto.CierreEntregaRequest;
import co.edu.uniquindio.uqdelivery.dto.DetalleEnvioResponse;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.DatosInvalidosException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.EvidenciaEntrega;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.EvidenciaEntregaRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CierreEntregaServiceTest {

    private static final byte[] CABECERA_PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] CABECERA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final int DOS_MB = 2 * 1024 * 1024;

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EvidenciaEntregaRepository evidenciaRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private CierreEntregaService service;

    private Usuario repartidor;

    @BeforeEach
    void prepararRepartidor() {
        repartidor = usuario(2L, "rep@a.com", Rol.REPARTIDOR);
        when(usuarioRepository.findByCorreoIgnoreCase("rep@a.com")).thenReturn(Optional.of(repartidor));
    }

    private Usuario usuario(Long id, String correo, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre("Nombre " + id);
        u.setCorreo(correo);
        u.setRol(rol);
        return u;
    }

    private Envio envio(EstadoEnvio estado, Usuario asignado) {
        Envio e = new Envio();
        e.setId(10L);
        e.setEstado(estado);
        e.setRepartidor(asignado);
        when(envioRepository.findById(10L)).thenReturn(Optional.of(e));
        return e;
    }

    private static byte[] contenido(byte[] cabecera, int tamanio) {
        byte[] datos = new byte[tamanio];
        System.arraycopy(cabecera, 0, datos, 0, cabecera.length);
        return datos;
    }

    private static ArchivoEvidencia png() {
        return new ArchivoEvidencia("image/png", contenido(CABECERA_PNG, 32));
    }

    private static CierreEntregaRequest solicitud(ArchivoEvidencia foto, ArchivoEvidencia firma) {
        return new CierreEntregaRequest("Ana Pérez", 4.5339, -75.6811, foto, firma);
    }

    private DatosInvalidosException rechazo(CierreEntregaRequest solicitud) {
        envio(EstadoEnvio.EN_RUTA, repartidor);
        return assertThrows(DatosInvalidosException.class, () -> service.cerrar(10L, "rep@a.com", solicitud));
    }

    @Test
    void cierraLaEntregaConFotoYFirmaYGuardaLaEvidencia() {
        Envio envio = envio(EstadoEnvio.EN_RUTA, repartidor);
        ArchivoEvidencia foto = png();
        ArchivoEvidencia firma = new ArchivoEvidencia("image/jpeg", contenido(CABECERA_JPEG, 16));

        DetalleEnvioResponse r = service.cerrar(10L, "rep@a.com",
                new CierreEntregaRequest("  Ana Pérez  ", 4.5339, -75.6811, foto, firma));

        assertEquals(EstadoEnvio.ENTREGADO, envio.getEstado());
        assertNotNull(envio.getFechaEntrega());
        assertEquals("Ana Pérez", envio.getNombreReceptor());
        assertEquals(4.5339, envio.getLatitudEntrega());
        assertEquals(-75.6811, envio.getLongitudEntrega());
        ArgumentCaptor<EvidenciaEntrega> captor = ArgumentCaptor.forClass(EvidenciaEntrega.class);
        verify(evidenciaRepository).saveAndFlush(captor.capture());
        assertSame(envio, captor.getValue().getEnvio());
        assertArrayEquals(foto.contenido(), captor.getValue().getFoto());
        assertEquals("image/png", captor.getValue().getFotoTipo());
        assertArrayEquals(firma.contenido(), captor.getValue().getFirma());
        assertEquals("image/jpeg", captor.getValue().getFirmaTipo());
        verify(notificationService).notifyStatusChange(envio, EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO);
        assertEquals(EstadoEnvio.ENTREGADO, r.estado());
        assertTrue(r.tieneFoto());
        assertTrue(r.tieneFirma());
    }

    @Test
    void permiteCerrarSoloConFirma() {
        envio(EstadoEnvio.EN_RUTA, repartidor);

        DetalleEnvioResponse r = service.cerrar(10L, "rep@a.com", solicitud(null, png()));

        assertFalse(r.tieneFoto());
        assertTrue(r.tieneFirma());
    }

    @Test
    void aceptaUnArchivoDeExactamenteDosMb() {
        envio(EstadoEnvio.EN_RUTA, repartidor);

        DetalleEnvioResponse r = service.cerrar(10L, "rep@a.com",
                solicitud(new ArchivoEvidencia("image/png", contenido(CABECERA_PNG, DOS_MB)), null));

        assertEquals(EstadoEnvio.ENTREGADO, r.estado());
    }

    @Test
    void rechazaCuandoElEnvioEstaAsignadoAOtroRepartidor() {
        envio(EstadoEnvio.EN_RUTA, usuario(3L, "otro@a.com", Rol.REPARTIDOR));

        assertThrows(AccessDeniedException.class, () -> service.cerrar(10L, "rep@a.com", solicitud(png(), null)));
        verifyNoInteractions(evidenciaRepository, notificationService);
    }

    @Test
    void rechazaCuandoElEnvioNoTieneRepartidor() {
        envio(EstadoEnvio.EN_RUTA, null);

        assertThrows(AccessDeniedException.class, () -> service.cerrar(10L, "rep@a.com", solicitud(png(), null)));
    }

    @Test
    void rechazaCuandoElEnvioNoEstaEnRuta() {
        Envio envio = envio(EstadoEnvio.ASIGNADO, repartidor);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> service.cerrar(10L, "rep@a.com", solicitud(png(), null)));

        assertEquals("Solo se puede cerrar la entrega de un envío en ruta", ex.getMessage());
        assertEquals(EstadoEnvio.ASIGNADO, envio.getEstado());
        verifyNoInteractions(evidenciaRepository, notificationService);
    }

    @Test
    void unEnvioEntregadoNoSePuedeModificar() {
        Envio envio = envio(EstadoEnvio.ENTREGADO, repartidor);

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> service.cerrar(10L, "rep@a.com", solicitud(png(), null)));

        assertEquals("El envío ya fue entregado y no puede modificarse", ex.getMessage());
        assertNull(envio.getNombreReceptor());
        verifyNoInteractions(evidenciaRepository, notificationService);
    }

    @Test
    void falla404SiElEnvioNoExiste() {
        when(envioRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.cerrar(10L, "rep@a.com", solicitud(png(), null)));
    }

    @Test
    void exigeAlMenosFotoOFirma() {
        DatosInvalidosException ex = rechazo(solicitud(null, null));

        assertTrue(ex.getErrores().containsKey("evidencia"));
        verifyNoInteractions(evidenciaRepository, notificationService);
    }

    @Test
    void validaNombreDelReceptorYCoordenadas() {
        DatosInvalidosException ex = rechazo(new CierreEntregaRequest(" ", 91.0, -181.0, png(), null));

        assertEquals(3, ex.getErrores().size());
        assertTrue(ex.getErrores().containsKey("nombreReceptor"));
        assertTrue(ex.getErrores().containsKey("latitud"));
        assertTrue(ex.getErrores().containsKey("longitud"));
    }

    @Test
    void exigeCoordenadas() {
        DatosInvalidosException ex = rechazo(new CierreEntregaRequest("Ana Pérez", null, null, png(), null));

        assertTrue(ex.getErrores().containsKey("latitud"));
        assertTrue(ex.getErrores().containsKey("longitud"));
    }

    @Test
    void rechazaCoordenadasNoNumericas() {
        DatosInvalidosException ex = rechazo(new CierreEntregaRequest("Ana Pérez", Double.NaN, Double.POSITIVE_INFINITY, png(), null));

        assertTrue(ex.getErrores().containsKey("latitud"));
        assertTrue(ex.getErrores().containsKey("longitud"));
    }

    @Test
    void rechazaNombreDelReceptorConMarcado() {
        DatosInvalidosException ex = rechazo(new CierreEntregaRequest("<b>Ana</b>", 4.5, -75.6, png(), null));

        assertTrue(ex.getErrores().containsKey("nombreReceptor"));
    }

    @Test
    void rechazaTiposDeArchivoDistintosDePngOJpeg() {
        DatosInvalidosException ex = rechazo(solicitud(
                new ArchivoEvidencia("image/gif", contenido(CABECERA_PNG, 32)), null));

        assertEquals("Solo se aceptan imágenes PNG o JPEG", ex.getErrores().get("foto"));
    }

    @Test
    void rechazaUnArchivoCuyoContenidoNoCoincideConSuTipo() {
        DatosInvalidosException ex = rechazo(solicitud(null,
                new ArchivoEvidencia("image/png", contenido(CABECERA_JPEG, 32))));

        assertTrue(ex.getErrores().containsKey("firma"));
    }

    @Test
    void rechazaUnArchivoQueNoEsUnaImagen() {
        DatosInvalidosException ex = rechazo(solicitud(
                new ArchivoEvidencia("image/jpeg", "<script>alert(1)</script>".getBytes()), null));

        assertTrue(ex.getErrores().containsKey("foto"));
    }

    @Test
    void rechazaArchivosMayoresADosMb() {
        DatosInvalidosException ex = rechazo(solicitud(
                new ArchivoEvidencia("image/png", contenido(CABECERA_PNG, DOS_MB + 1)), null));

        assertEquals("El archivo supera el máximo de 2 MB", ex.getErrores().get("foto"));
    }

    @Test
    void reportaConflictoSiLaEvidenciaYaExiste() {
        Envio envio = envio(EstadoEnvio.EN_RUTA, repartidor);
        when(evidenciaRepository.saveAndFlush(any(EvidenciaEntrega.class)))
                .thenThrow(new DataIntegrityViolationException("duplicado"));

        assertThrows(ConflictoException.class, () -> service.cerrar(10L, "rep@a.com", solicitud(png(), null)));
        assertEquals(EstadoEnvio.EN_RUTA, envio.getEstado());
        verifyNoInteractions(notificationService);
    }
}
