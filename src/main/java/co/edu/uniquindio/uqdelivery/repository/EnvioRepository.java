package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    @Query("""
            select e from Envio e
            where (:estado is null or e.estado = :estado)
            """)
    Page<Envio> buscar(@Param("estado") EstadoEnvio estado, Pageable pageable);

    List<Envio> findByRepartidorCorreoIgnoreCaseAndEstadoInOrderByIdAsc(String correo, Collection<EstadoEnvio> estados);
}
