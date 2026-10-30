package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.AsignacionRequest;
import co.edu.uniquindio.uqdelivery.dto.AsignacionResponse;
import co.edu.uniquindio.uqdelivery.dto.EnvioResponse;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.AsignacionEnvio;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.AsignacionEnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AsignacionService {

    private static final List<EstadoEnvio> ESTADOS_EN_ENTREGA = List.of(EstadoEnvio.ASIGNADO, EstadoEnvio.EN_RUTA);

    private final EnvioRepository envioRepository;
    private final AsignacionEnvioRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;

    public AsignacionService(EnvioRepository envioRepository,
                             AsignacionEnvioRepository asignacionRepository,
                             UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.asignacionRepository = asignacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EnvioResponse asignar(Long envioId, AsignacionRequest request, String correoAdministrador) {
        Envio envio = buscarEnvio(envioId);
        if (envio.getEstado() != EstadoEnvio.PENDIENTE) {
            throw new ConflictoException("Solo se puede asignar un envío en estado Pendiente");
        }
        Usuario repartidor = buscarRepartidorAsignable(request.repartidorId());
        Usuario administrador = buscarAdministrador(correoAdministrador);

        envio.setRepartidor(repartidor);
        envio.setEstado(EstadoEnvio.ASIGNADO);
        registrar(envio, null, repartidor, administrador, request.motivo());
        return EnvioResponse.desde(envio);
    }

    @Transactional
    public EnvioResponse reasignar(Long envioId, AsignacionRequest request, String correoAdministrador) {
        Envio envio = buscarEnvio(envioId);
        validarReasignable(envio);
        Usuario repartidor = buscarRepartidorAsignable(request.repartidorId());
        Usuario anterior = envio.getRepartidor();
        if (anterior.getId().equals(repartidor.getId())) {
            throw new ConflictoException("El envío ya está asignado a ese repartidor");
        }
        Usuario administrador = buscarAdministrador(correoAdministrador);

        envio.setRepartidor(repartidor);
        envio.setEstado(EstadoEnvio.ASIGNADO);
        registrar(envio, anterior, repartidor, administrador, request.motivo());
        return EnvioResponse.desde(envio);
    }

    @Transactional(readOnly = true)
    public Page<EnvioResponse> listarEnvios(EstadoEnvio estado, Pageable pageable) {
        return envioRepository.buscar(estado, pageable).map(EnvioResponse::desde);
    }

    @Transactional(readOnly = true)
    public List<AsignacionResponse> historial(Long envioId) {
        buscarEnvio(envioId);
        return asignacionRepository.findByEnvioIdOrderByIdAsc(envioId).stream()
                .map(AsignacionResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnvioResponse> entregasDelRepartidor(String correoRepartidor) {
        return envioRepository
                .findByRepartidorCorreoIgnoreCaseAndEstadoInOrderByIdAsc(normalizarCorreo(correoRepartidor), ESTADOS_EN_ENTREGA)
                .stream()
                .map(EnvioResponse::desde)
                .toList();
    }

    private void validarReasignable(Envio envio) {
        switch (envio.getEstado()) {
            case ENTREGADO -> throw new ConflictoException("No se puede reasignar un envío ya entregado");
            case PENDIENTE -> throw new ConflictoException("El envío aún no tiene repartidor asignado");
            default -> { }
        }
    }

    private void registrar(Envio envio, Usuario anterior, Usuario nuevo, Usuario administrador, String motivo) {
        AsignacionEnvio asignacion = new AsignacionEnvio();
        asignacion.setEnvio(envio);
        asignacion.setRepartidorAnterior(anterior);
        asignacion.setRepartidorNuevo(nuevo);
        asignacion.setAdministrador(administrador);
        asignacion.setMotivo(normalizarMotivo(motivo));
        asignacionRepository.save(asignacion);
    }

    private Envio buscarEnvio(Long id) {
        return envioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío con id " + id + " no encontrado"));
    }

    private Usuario buscarRepartidorAsignable(Long id) {
        Usuario repartidor = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor con id " + id + " no encontrado"));
        if (repartidor.getRol() != Rol.REPARTIDOR) {
            throw new ConflictoException("El usuario seleccionado no tiene rol de repartidor");
        }
        if (!repartidor.isActivo()) {
            throw new ConflictoException("El repartidor seleccionado está desactivado");
        }
        return repartidor;
    }

    private Usuario buscarAdministrador(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(normalizarCorreo(correo))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        return motivo.trim();
    }
}
