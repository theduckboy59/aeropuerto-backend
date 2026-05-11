package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.Avion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AvionSpecification {

    public static Specification<Avion> filters(
            String q,
            Integer aerolineaId,
            Integer estadoAvionId,
            Integer modeloAvionId,
            Integer estadoId,
            Integer anio
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {

                String search = "%" + q.toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("codigoAvion")), search),
                                cb.like(cb.lower(root.get("numeroSerie")), search)
                        )
                );
            }

            if (aerolineaId != null) {
                predicates.add(cb.equal(root.get("aerolineaId"), aerolineaId));
            }

            if (estadoAvionId != null) {
                predicates.add(cb.equal(root.get("estadoAvionId"), estadoAvionId));
            }

            if (modeloAvionId != null) {
                predicates.add(cb.equal(root.get("modeloAvionId"), modeloAvionId));
            }

            if (estadoId != null) {
                predicates.add(cb.equal(root.get("estadoId"), estadoId));
            }

            if (anio != null) {
                predicates.add(cb.equal(root.get("anio"), anio));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}