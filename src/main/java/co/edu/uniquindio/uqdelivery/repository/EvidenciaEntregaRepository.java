package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.dto.TiposEvidencia;
import co.edu.uniquindio.uqdelivery.model.EvidenciaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EvidenciaEntregaRepository extends JpaRepository<EvidenciaEntrega, Long> {

    Optional<EvidenciaEntrega> findByEnvioId(Long envioId);

    @Query("""
            select new co.edu.uniquindio.uqdelivery.dto.TiposEvidencia(v.fotoTipo, v.firmaTipo)
            from EvidenciaEntrega v
            where v.envio.id = :envioId
            """)
    Optional<TiposEvidencia> tiposDelEnvio(@Param("envioId") Long envioId);
}
