package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.ReglasValidacion;
import co.edu.uniquindio.uqdelivery.dto.SolicitudEnvioRequest;
import co.edu.uniquindio.uqdelivery.dto.SolicitudEnvioResponse;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.exception.SolicitudInvalidaException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.Cotizacion;
import co.edu.uniquindio.uqdelivery.service.CotizacionService.SolicitudCotizacion;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

/** HU-03: solicitud de envío con pago simulado por adelantado y código de seguimiento único. */
@Service
public class EnvioService {

    private static final String PREFIJO_CODIGO = "UQD-";
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin 0/O/1/I
    private static final int LONGITUD_CODIGO = 10;
    private static final int MAX_INTENTOS_CODIGO = 10;

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CotizacionService cotizacionService;
    private final PagoSimuladoService pagoService;
    private final SecureRandom random = new SecureRandom();

    public EnvioService(EnvioRepository envioRepository,
                        UsuarioRepository usuarioRepository,
                        CotizacionService cotizacionService,
                        PagoSimuladoService pagoService) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
        this.cotizacionService = cotizacionService;
        this.pagoService = pagoService;
    }

    /**
     * Orden deliberado: (1) cotiza con la distancia calculada por el servidor, (2) persiste y hace flush
     * del envío —así colisiones de código o de Idempotency-Key fallan ANTES de cobrar—, (3) cobra.
     * Si el pago se rechaza se lanza {@code PagoRechazadoException} y la transacción hace rollback (no
     * queda envío). Si tras cobrar la transacción termina en rollback, el cobro se anula.
     *
     * @param claveIdempotencia opcional; si ya existe un envío del cliente con esa clave se devuelve ese
     *                          (sin crear ni cobrar de nuevo).
     */
    @Transactional
    public ResultadoSolicitud solicitar(SolicitudEnvioRequest r, String correoCliente, String claveIdempotencia) {
        validarClave(claveIdempotencia);
        Usuario cliente = usuarioRepository.findByCorreoIgnoreCase(correoCliente.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (claveIdempotencia != null) {
            Optional<Envio> previo = envioRepository.findByClienteIdAndClaveIdempotencia(cliente.getId(), claveIdempotencia);
            if (previo.isPresent()) {
                return new ResultadoSolicitud(SolicitudEnvioResponse.desde(previo.get()), false);
            }
        }

        double distanciaKm = r.zonaOrigen().distanciaKmA(r.zonaDestino());
        Cotizacion cotizacion;
        try {
            cotizacion = cotizacionService.cotizar(new SolicitudCotizacion(
                    distanciaKm, r.pesoKg(), r.largoCm(), r.anchoCm(), r.altoCm(), r.tipoServicio()));
        } catch (IllegalArgumentException ex) {
            throw new SolicitudInvalidaException(ex.getMessage());
        }

        Envio envio = new Envio();
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setCliente(cliente);
        envio.setClaveIdempotencia(claveIdempotencia);
        envio.setCodigoSeguimiento(generarCodigoUnico());
        envio.setZonaOrigen(r.zonaOrigen());
        envio.setZonaDestino(r.zonaDestino());
        envio.setDireccionOrigen(r.direccionOrigen().trim());
        envio.setDireccionDestino(r.direccionDestino().trim());
        envio.setDestinatarioNombre(r.destinatarioNombre().trim());
        envio.setDestinatarioTelefono(r.destinatarioTelefono());
        envio.setPesoKg(r.pesoKg());
        envio.setLargoCm(r.largoCm());
        envio.setAnchoCm(r.anchoCm());
        envio.setAltoCm(r.altoCm());
        envio.setDistanciaKm(distanciaKm);
        envio.setDescripcion(r.descripcion().trim());
        envio.setTipoServicio(r.tipoServicio());
        envio.setValorCotizado(cotizacion.total());
        envio.setValorTotal(cotizacion.total()); // base de las métricas del dashboard (HU-15)
        try {
            envioRepository.saveAndFlush(envio);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoException("No se pudo registrar la solicitud (duplicada o en proceso); intente de nuevo");
        }

        String referenciaPago = pagoService.cobrar(r.pago().numeroTarjeta(), cotizacion.total());
        anularPagoSiHayRollback(referenciaPago);
        envio.setReferenciaPago(referenciaPago);
        return new ResultadoSolicitud(SolicitudEnvioResponse.desde(envio), true);
    }

    private void validarClave(String clave) {
        if (clave != null && !clave.matches(ReglasValidacion.CLAVE_IDEMPOTENCIA)) {
            throw new SolicitudInvalidaException("Idempotency-Key inválida: use 8 a 64 caracteres alfanuméricos o guion");
        }
    }

    /** Compensación: si la transacción no se confirma después de cobrar, se anula el pago. */
    private void anularPagoSiHayRollback(String referenciaPago) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    pagoService.anular(referenciaPago);
                }
            }
        });
    }

    String generarCodigoUnico() {
        for (int i = 0; i < MAX_INTENTOS_CODIGO; i++) {
            StringBuilder sb = new StringBuilder(PREFIJO_CODIGO);
            for (int j = 0; j < LONGITUD_CODIGO; j++) {
                sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
            }
            String codigo = sb.toString();
            if (!envioRepository.existsByCodigoSeguimiento(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("No fue posible generar un código de seguimiento único");
    }
}
