package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.dto.ConteoEstadoFila;
import co.edu.uniquindio.uqdelivery.dto.EntregaFila;
import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    long countByClienteCorreoIgnoreCase(String correo);

    Optional<Envio> findByClienteIdAndClaveIdempotencia(Long clienteId, String claveIdempotencia);

    @Query("""
            select e from Envio e
            where (:estado is null or e.estado = :estado)
            """)
    Page<Envio> buscar(@Param("estado") EstadoEnvio estado, Pageable pageable);

    List<Envio> findByRepartidorCorreoIgnoreCaseAndEstadoInOrderByIdAsc(String correo, Collection<EstadoEnvio> estados);

    @Query("""
            select new co.edu.uniquindio.uqdelivery.dto.ConteoEstadoFila(e.estado, count(e))
            from Envio e
            where e.fechaCreacion >= :inicio and e.fechaCreacion < :fin
              and (:repartidorId is null or e.repartidor.id = :repartidorId)
            group by e.estado
            """)
    List<ConteoEstadoFila> contarPorEstado(@Param("inicio") LocalDateTime inicio,
                                           @Param("fin") LocalDateTime fin,
                                           @Param("repartidorId") Long repartidorId);

    @Query("""
            select new co.edu.uniquindio.uqdelivery.dto.EntregaFila(
                r.id, r.nombre, e.fechaCreacion, e.fechaEntrega, e.valorTotal)
            from Envio e left join e.repartidor r
            where e.estado = :estado
              and e.fechaEntrega >= :inicio and e.fechaEntrega < :fin
              and (:repartidorId is null or r.id = :repartidorId)
            """)
    List<EntregaFila> listarEntregas(@Param("estado") EstadoEnvio estado,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin,
                                     @Param("repartidorId") Long repartidorId);
}
