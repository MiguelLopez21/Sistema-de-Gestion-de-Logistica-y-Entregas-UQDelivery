package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    long countByRolAndActivoTrue(Rol rol);

    // Consulta parametrizada (no concatena strings): protección contra inyección SQL.
    @Query("""
            select u from Usuario u
            where (:rol is null or u.rol = :rol)
              and (:activo is null or u.activo = :activo)
            """)
    Page<Usuario> buscar(@Param("rol") Rol rol, @Param("activo") Boolean activo, Pageable pageable);
}
