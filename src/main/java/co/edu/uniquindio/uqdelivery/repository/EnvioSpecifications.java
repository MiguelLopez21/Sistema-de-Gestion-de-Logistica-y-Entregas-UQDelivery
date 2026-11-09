package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class EnvioSpecifications {

    private EnvioSpecifications() {
    }

    public static Specification<Envio> historial(
            Long userId,
            EstadoEnvio status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String trackingNumber) {
        Specification<Envio> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (userId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> {
                Join<Envio, Usuario> client = root.join("cliente", JoinType.LEFT);
                Join<Envio, Usuario> courier = root.join("repartidor", JoinType.LEFT);
                return criteriaBuilder.or(
                        criteriaBuilder.equal(client.get("id"), userId),
                        criteriaBuilder.equal(courier.get("id"), userId));
            });
        }

        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("estado"), status));
        }

        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCreacion"), startDate));
        }

        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("fechaCreacion"), endDate));
        }

        if (trackingNumber != null && !trackingNumber.isBlank()) {
            String normalizedTrackingNumber = trackingNumber.trim();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("codigoSeguimiento"), normalizedTrackingNumber));
        }

        return specification;
    }
}
