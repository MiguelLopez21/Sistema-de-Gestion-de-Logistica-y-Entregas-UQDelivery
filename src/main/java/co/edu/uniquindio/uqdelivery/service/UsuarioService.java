package co.edu.uniquindio.uqdelivery.service;

import co.edu.uniquindio.uqdelivery.dto.UsuarioActualizarRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioCrearRequest;
import co.edu.uniquindio.uqdelivery.dto.UsuarioResponse;
import co.edu.uniquindio.uqdelivery.exception.ConflictoException;
import co.edu.uniquindio.uqdelivery.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * HU-02 Gestión de perfiles y roles: crear, editar, desactivar usuarios y asignarles un rol.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        String correo = normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictoException("Ya existe un usuario registrado con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono().trim());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Rol rol, Boolean activo, Pageable pageable) {
        return usuarioRepository.buscar(rol, activo, pageable).map(UsuarioResponse::desde);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(normalizarCorreo(correo))
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioActualizarRequest request) {
        Usuario usuario = buscarPorId(id);
        String correo = normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new ConflictoException("Ya existe otro usuario registrado con ese correo");
        }
        validarCambioDeRol(usuario, request.rol());

        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(request.telefono().trim());
        usuario.setRol(request.rol());
        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol nuevoRol) {
        Usuario usuario = buscarPorId(id);
        validarCambioDeRol(usuario, nuevoRol);
        usuario.setRol(nuevoRol);
        return UsuarioResponse.desde(usuario);
    }

    /**
     * Desactiva (borrado lógico) al usuario: ya no puede iniciar sesión.
     *
     * @param correoSolicitante correo del administrador que ejecuta la acción
     */
    @Transactional
    public UsuarioResponse desactivar(Long id, String correoSolicitante) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoSolicitante)) {
            throw new ConflictoException("No puede desactivar su propia cuenta");
        }
        if (usuario.isActivo()) {
            if (usuario.getRol() == Rol.ADMINISTRADOR) {
                validarQueNoSeaUltimoAdministrador();
            }
            usuario.setActivo(false);
        }
        return UsuarioResponse.desde(usuario);
    }

    @Transactional
    public UsuarioResponse activar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(true);
        return UsuarioResponse.desde(usuario);
    }

    // ---------- reglas internas ----------

    private void validarCambioDeRol(Usuario usuario, Rol nuevoRol) {
        boolean dejaDeSerAdmin = usuario.getRol() == Rol.ADMINISTRADOR && nuevoRol != Rol.ADMINISTRADOR;
        if (dejaDeSerAdmin && usuario.isActivo()) {
            validarQueNoSeaUltimoAdministrador();
        }
    }

    /** Evita dejar el sistema sin ningún administrador activo. */
    private void validarQueNoSeaUltimoAdministrador() {
        if (usuarioRepository.countByRolAndActivoTrue(Rol.ADMINISTRADOR) <= 1) {
            throw new ConflictoException("Debe existir al menos un administrador activo en el sistema");
        }
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con id " + id + " no encontrado"));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
