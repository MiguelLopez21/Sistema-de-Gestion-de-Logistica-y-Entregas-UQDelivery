package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.model.AsignacionEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsignacionEnvioRepository extends JpaRepository<AsignacionEnvio, Long> {

    List<AsignacionEnvio> findByEnvioIdOrderByIdAsc(Long envioId);
}
