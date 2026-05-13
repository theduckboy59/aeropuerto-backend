package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.Tripulacion;
import org.springframework.data.jpa.domain.Specification;

public class TripulacionSpecification {

    public static Specification<Tripulacion> filters(
            String q,
            Integer aerolineaId,
            Integer estadoTripulacionId
    ) {

        return (root, query, cb) -> {

            var predicate = cb.conjunction();

            if (q != null && !q.trim().isEmpty()) {
                String search = "%" + q.trim().toLowerCase() + "%";

                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("codigo")),
                                search
                        )
                );
            }

            if (aerolineaId != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(
                                root.get("aerolineaId"),
                                aerolineaId
                        )
                );
            }

            if (estadoTripulacionId != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(
                                root.get("estadoTripulacionId"),
                                estadoTripulacionId
                        )
                );
            }

            return predicate;
        };
    }
}