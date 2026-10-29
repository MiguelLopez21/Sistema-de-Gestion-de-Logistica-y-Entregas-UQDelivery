package co.edu.uniquindio.uqdelivery.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Control de acceso basado en roles (RBAC).
 * <p>
 * Autenticación provisional: HTTP Basic contra la tabla de usuarios, sin sesión (stateless).
 * Cuando HU-01 entregue el login definitivo (p. ej. JWT) solo hay que cambiar el mecanismo de
 * autenticación; las reglas de autorización de abajo se mantienen.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String JSON_401 =
            "{\"estado\":401,\"mensaje\":\"Credenciales inválidas o sesión no iniciada\"}";
    private static final String JSON_403 =
            "{\"estado\":403,\"mensaje\":\"Acceso denegado: su rol no tiene permiso para esta operación\"}";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API REST stateless sin cookies de sesión
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // necesario para la consola H2
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()   // SOLO desarrollo: quitar en producción
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/perfil/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(b -> b.authenticationEntryPoint(entryPoint401()))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint401())
                        .accessDeniedHandler(accessDenied403()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationEntryPoint entryPoint401() {
        return (request, response, ex) -> escribirJson(response, 401, JSON_401);
    }

    private AccessDeniedHandler accessDenied403() {
        return (request, response, ex) -> escribirJson(response, 403, JSON_403);
    }

    private void escribirJson(HttpServletResponse response, int estado, String json) throws IOException {
        response.setStatus(estado);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }
}