package co.edu.uniquindio.uqdelivery.repository;

import co.edu.uniquindio.uqdelivery.model.Envio;
import co.edu.uniquindio.uqdelivery.model.EstadoEnvio;
import co.edu.uniquindio.uqdelivery.model.Rol;
import co.edu.uniquindio.uqdelivery.model.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Locale;

public final class EnvioSpecifications {

    private EnvioSpecifications() {
    }

    public static Specification<Envio> historial(
            Long userId,
            Rol rol,
            EstadoEnvio status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String queryText) {
        Specification<Envio> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (userId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> {
                String relation = rol == Rol.CLIENTE_EMISOR ? "cliente" : "repartidor";
                Join<Envio, Usuario> user = root.join(relation, JoinType.INNER);
                return criteriaBuilder.equal(user.get("id"), userId);
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
                    criteriaBuilder.lessThan(root.get("fechaCreacion"), endDate));
        }

        if (queryText != null && !queryText.isBlank()) {
            String normalizedQuery = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("codigoSeguimiento")),
                            normalizedQuery));
        }

        return specification;
    }
}
