package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.ArchivoEvidencia;
import co.edu.uniquindio.uqdelivery.dto.CierreEntregaRequest;
import co.edu.uniquindio.uqdelivery.dto.DetalleEnvioResponse;
import co.edu.uniquindio.uqdelivery.dto.ReglasValidacion;
import co.edu.uniquindio.uqdelivery.dto.TiposEvidencia;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.DatosInvalidosException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.EvidenciaEntrega;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.EvidenciaEntregaRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class CierreEntregaService {

    public static final String PNG = "image/png";
    public static final String JPEG = "image/jpeg";

    private static final byte[] CABECERA_PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] CABECERA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final int MAXIMO_NOMBRE_RECEPTOR = 100;

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvidenciaEntregaRepository evidenciaRepository;
    private final NotificationService notificationService;

    public CierreEntregaService(EnvioRepository envioRepository,
                                UsuarioRepository usuarioRepository,
                                EvidenciaEntregaRepository evidenciaRepository,
                                NotificationService notificationService) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public DetalleEnvioResponse cerrar(Long envioId, String correoRepartidor, CierreEntregaRequest solicitud) {
        Usuario repartidor = usuarioRepository
                .findByCorreoIgnoreCase(correoRepartidor.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío con id " + envioId + " no encontrado"));

        if (envio.getRepartidor() == null || !envio.getRepartidor().getId().equals(repartidor.getId())) {
            throw new AccessDeniedException("El envío no está asignado a este repartidor");
        }
        validarEstado(envio.getEstado());
        validarSolicitud(solicitud);

        EvidenciaEntrega evidencia = new EvidenciaEntrega();
        evidencia.setEnvio(envio);
        if (solicitud.foto() != null) {
            evidencia.setFoto(solicitud.foto().contenido());
            evidencia.setFotoTipo(solicitud.foto().tipo());
        }
        if (solicitud.firma() != null) {
            evidencia.setFirma(solicitud.firma().contenido());
            evidencia.setFirmaTipo(solicitud.firma().tipo());
        }
        try {
            evidenciaRepository.saveAndFlush(evidencia);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoException("La entrega de este envío ya fue cerrada");
        }

        EstadoEnvio estadoAnterior = envio.getEstado();
        envio.setEstado(EstadoEnvio.ENTREGADO);
        envio.setFechaEntrega(LocalDateTime.now());
        envio.setNombreReceptor(solicitud.nombreReceptor().trim());
        envio.setLatitudEntrega(solicitud.latitud());
        envio.setLongitudEntrega(solicitud.longitud());
        notificationService.notifyStatusChange(envio, estadoAnterior, EstadoEnvio.ENTREGADO);

        return DetalleEnvioResponse.desde(envio,
                new TiposEvidencia(evidencia.getFotoTipo(), evidencia.getFirmaTipo()));
    }

    private void validarEstado(EstadoEnvio estado) {
        if (estado == EstadoEnvio.ENTREGADO) {
            throw new ConflictoException("El envío ya fue entregado y no puede modificarse");
        }
        if (estado != EstadoEnvio.EN_RUTA) {
            throw new ConflictoException("Solo se puede cerrar la entrega de un envío en ruta");
        }
    }

    private void validarSolicitud(CierreEntregaRequest s) {
        Map<String, String> errores = new LinkedHashMap<>();
        validarReceptor(s.nombreReceptor(), errores);
        validarCoordenada("latitud", s.latitud(), 90, errores);
        validarCoordenada("longitud", s.longitud(), 180, errores);
        if (s.foto() == null && s.firma() == null) {
            errores.put("evidencia", "Debe adjuntar al menos la foto o la firma de entrega");
        }
        validarArchivo("foto", s.foto(), errores);
        validarArchivo("firma", s.firma(), errores);
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
    }

    private void validarReceptor(String nombre, Map<String, String> errores) {
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombreReceptor", "El nombre de quien recibe es obligatorio");
            return;
        }
        String limpio = nombre.trim();
        if (limpio.length() > MAXIMO_NOMBRE_RECEPTOR) {
            errores.put("nombreReceptor", "Máximo " + MAXIMO_NOMBRE_RECEPTOR + " caracteres");
        } else if (!limpio.matches(ReglasValidacion.NOMBRE_PERSONA)) {
            errores.put("nombreReceptor", ReglasValidacion.NOMBRE_PERSONA_MSG);
        }
    }

    private void validarCoordenada(String campo, Double valor, int limite, Map<String, String> errores) {
        if (valor == null) {
            errores.put(campo, "La " + campo + " es obligatoria");
        } else if (!Double.isFinite(valor) || Math.abs(valor) > limite) {
            errores.put(campo, "La " + campo + " debe estar entre -" + limite + " y " + limite);
        }
    }

    private void validarArchivo(String campo, ArchivoEvidencia archivo, Map<String, String> errores) {
        if (archivo == null) {
            return;
        }
        byte[] contenido = archivo.contenido();
        if (contenido == null || contenido.length == 0) {
            errores.put(campo, "El archivo está vacío");
        } else if (contenido.length > EvidenciaEntrega.TAMANIO_MAXIMO_BYTES) {
            errores.put(campo, "El archivo supera el máximo de 2 MB");
        } else if (!PNG.equals(archivo.tipo()) && !JPEG.equals(archivo.tipo())) {
            errores.put(campo, "Solo se aceptan imágenes PNG o JPEG");
        } else if (!tipoDetectado(contenido).equals(archivo.tipo())) {
            errores.put(campo, "El contenido del archivo no corresponde a una imagen " + (PNG.equals(archivo.tipo()) ? "PNG" : "JPEG"));
        }
    }

    private String tipoDetectado(byte[] contenido) {
        if (empiezaCon(contenido, CABECERA_PNG)) {
            return PNG;
        }
        if (empiezaCon(contenido, CABECERA_JPEG)) {
            return JPEG;
        }
        return "";
    }

    private boolean empiezaCon(byte[] contenido, byte[] cabecera) {
        if (contenido.length < cabecera.length) {
            return false;
        }
        for (int i = 0; i < cabecera.length; i++) {
            if (contenido[i] != cabecera[i]) {
                return false;
            }
        }
        return true;
    }
}
