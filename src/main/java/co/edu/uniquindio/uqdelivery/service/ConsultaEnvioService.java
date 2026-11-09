package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.ArchivoEvidencia;
import co.edu.uniquindio.uqdelivery.dto.DetalleEnvioResponse;
import co.edu.uniquindio.uqdelivery.dto.TiposEvidencia;
import co.edu.uniquindio.uqdelivery.exception.ParametroInvalidoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EvidenciaEntrega;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.EnvioRepository;
import co.edu.uniquindio.uqdelivery.repository.EvidenciaEntregaRepository;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ConsultaEnvioService {

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvidenciaEntregaRepository evidenciaRepository;

    public ConsultaEnvioService(EnvioRepository envioRepository,
                                UsuarioRepository usuarioRepository,
                                EvidenciaEntregaRepository evidenciaRepository) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
        this.evidenciaRepository = evidenciaRepository;
    }

    @Transactional(readOnly = true)
    public DetalleEnvioResponse detalle(Long envioId, String correo) {
        Envio envio = buscarAutorizado(envioId, correo);
        TiposEvidencia tipos = evidenciaRepository.tiposDelEnvio(envioId).orElse(TiposEvidencia.VACIA);
        return DetalleEnvioResponse.desde(envio, tipos);
    }

    @Transactional(readOnly = true)
    public ArchivoEvidencia evidencia(Long envioId, String tipo, String correo) {
        boolean foto = esFoto(tipo);
        buscarAutorizado(envioId, correo);
        EvidenciaEntrega evidencia = evidenciaRepository.findByEnvioId(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El envío no tiene evidencia de entrega"));
        byte[] contenido = foto ? evidencia.getFoto() : evidencia.getFirma();
        if (contenido == null) {
            throw new RecursoNoEncontradoException("El envío no tiene " + (foto ? "foto" : "firma") + " de entrega");
        }
        return new ArchivoEvidencia(foto ? evidencia.getFotoTipo() : evidencia.getFirmaTipo(), contenido);
    }

    private boolean esFoto(String tipo) {
        if ("foto".equalsIgnoreCase(tipo)) {
            return true;
        }
        if ("firma".equalsIgnoreCase(tipo)) {
            return false;
        }
        throw new ParametroInvalidoException("Tipo de evidencia inválido: use foto o firma");
    }

    private Envio buscarAutorizado(Long envioId, String correo) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío con id " + envioId + " no encontrado"));
        boolean administrador = usuario.getRol() == Rol.ADMINISTRADOR;
        boolean propietario = envio.getCliente() != null && envio.getCliente().getId().equals(usuario.getId());
        if (!administrador && !propietario) {
            throw new AccessDeniedException("No tiene permiso para consultar este envío");
        }
        return envio;
    }
}
