package co.edu.uniquindio.uqdelivery.config;

import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import co.edu.uniquindio.uqdelivery.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea el primer administrador (sin él nadie podría crear usuarios con la BD H2 en memoria vacía). */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearAdministradorInicial(
            UsuarioRepository repository,
            PasswordEncoder encoder,
            @Value("${uqdelivery.admin.correo}") String correo,
            @Value("${uqdelivery.admin.password}") String password) {
        return args -> {
            if (!repository.existsByCorreoIgnoreCase(correo)) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador UQDelivery");
                admin.setCorreo(correo.toLowerCase());
                admin.setTelefono("3000000000");
                admin.setPassword(encoder.encode(password));
                admin.setRol(Rol.ADMINISTRADOR);
                admin.setActivo(true);
                repository.save(admin);
            }
        };
    }
}