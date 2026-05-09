package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.ModeloAvion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ModeloAvionSpecification {

    public static Specification<ModeloAvion> filters(
            String q,
            Integer niveles,
            Integer pasillos,
            String configuracion,
            Integer totalColumnas
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {

                String search = "%" + q.toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("fabricante")), search),
                                cb.like(cb.lower(root.get("codigoModelo")), search),
                                cb.like(cb.lower(root.get("nombre")), search)
                        )
                );
            }

            if (niveles != null) {
                predicates.add(
                        cb.equal(root.get("niveles"), niveles)
                );
            }

            if (pasillos != null) {
                predicates.add(
                        cb.equal(root.get("pasillos"), pasillos)
                );
            }

            if (configuracion != null && !configuracion.isBlank()) {
                predicates.add(
                        cb.equal(root.get("configuracion"), configuracion)
                );
            }

            if (totalColumnas != null) {
                predicates.add(
                        cb.equal(root.get("totalColumnas"), totalColumnas)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}