package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Notification;
import co.edu.uniquindio.uqdelivery.model.NotificationType;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@Transactional
class PersistenceRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void buscaHistorialPorUsuarioEstadoRangoCodigoYPagina() {
        Usuario cliente = guardarUsuario("cliente@uqdelivery.com", Rol.CLIENTE_EMISOR);
        Usuario repartidor = guardarUsuario("repartidor@uqdelivery.com", Rol.REPARTIDOR);

        guardarEnvio(cliente, repartidor, "UQD-001", EstadoEnvio.ENTREGADO,
                LocalDateTime.of(2026, 10, 10, 12, 0));
        guardarEnvio(cliente, repartidor, "UQD-002", EstadoEnvio.PENDIENTE,
                LocalDateTime.of(2026, 10, 10, 13, 0));
        guardarEnvio(null, repartidor, "UQD-003", EstadoEnvio.ENTREGADO,
                LocalDateTime.of(2026, 10, 10, 14, 0));
        entityManager.flush();

        Page<Envio> resultado = envioRepository.buscarHistorial(
                cliente.getId(),
                Rol.CLIENTE_EMISOR,
                EstadoEnvio.ENTREGADO,
                LocalDateTime.of(2026, 10, 10, 0, 0),
                LocalDateTime.of(2026, 10, 11, 0, 0),
                "UQD-001",
                PageRequest.of(0, 1, Sort.by("id")));

        assertEquals(1, resultado.getTotalElements());
        assertEquals("UQD-001", resultado.getContent().getFirst().getCodigoSeguimiento());
    }

    @Test
    void buscaHistorialDelRepartidorYPermiteFiltrarSoloPorCodigo() {
        Usuario repartidor = guardarUsuario("repartidor@uqdelivery.com", Rol.REPARTIDOR);
        guardarEnvio(null, repartidor, "UQD-004", EstadoEnvio.ASIGNADO,
                LocalDateTime.of(2026, 10, 11, 12, 0));
        guardarEnvio(null, null, "UQD-005", EstadoEnvio.ASIGNADO,
                LocalDateTime.of(2026, 10, 11, 13, 0));
        entityManager.flush();

        Page<Envio> resultado = envioRepository.buscarHistorial(
                repartidor.getId(), Rol.REPARTIDOR, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(List.of("UQD-004"),
                resultado.getContent().stream().map(Envio::getCodigoSeguimiento).toList());
    }

    @Test
    void listaNotificacionesEnOrdenDescendenteYCuentaLasNoLeidas() {
        Usuario usuario = guardarUsuario("notificaciones@uqdelivery.com", Rol.CLIENTE_EMISOR);
        Notification antigua = notificacion(usuario, "Aviso anterior",
                LocalDateTime.of(2026, 10, 1, 8, 0), false);
        Notification reciente = notificacion(usuario, "Aviso reciente",
                LocalDateTime.of(2026, 10, 2, 8, 0), false);
        notificacion(usuario, "Aviso leído", LocalDateTime.of(2026, 10, 3, 8, 0), true);
        entityManager.persist(antigua);
        entityManager.persist(reciente);
        entityManager.flush();

        List<Notification> resultado = notificationRepository.findByUserIdOrderByCreatedAtDesc(usuario.getId());

        assertEquals(List.of("Aviso leído", "Aviso reciente", "Aviso anterior"),
                resultado.stream().map(Notification::getTitle).toList());
        assertEquals(2, notificationRepository.countByUserIdAndReadFalse(usuario.getId()));
    }

    private Usuario guardarUsuario(String correo, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setCorreo(correo);
        usuario.setTelefono("3001234567");
        usuario.setPassword("hash");
        usuario.setRol(rol);
        entityManager.persist(usuario);
        entityManager.flush();
        return usuario;
    }

    private void guardarEnvio(Usuario cliente, Usuario repartidor, String codigo,
                              EstadoEnvio estado, LocalDateTime fechaCreacion) {
        Envio envio = new Envio();
        envio.setCliente(cliente);
        envio.setRepartidor(repartidor);
        envio.setCodigoSeguimiento(codigo);
        envio.setEstado(estado);
        envio.setFechaCreacion(fechaCreacion);
        entityManager.persist(envio);
    }

    private Notification notificacion(Usuario usuario, String titulo, LocalDateTime fecha, boolean leida) {
        Notification notification = new Notification();
        notification.setUser(usuario);
        notification.setType(NotificationType.INFO);
        notification.setTitle(titulo);
        notification.setMessage("Mensaje de prueba");
        notification.setRead(leida);
        notification.setCreatedAt(fecha);
        return notification;
    }
}
