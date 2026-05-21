package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.VueloOperado;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VueloOperadoSpecification {

    public static Specification<VueloOperado> filters(
            Integer vueloProgramadoId,
            Integer avionId,
            Integer tripulacionId,
            Integer estadoVueloId,
            LocalDate fechaSalidaReal,
            LocalDate fechaLlegadaReal
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (vueloProgramadoId != null) {
                predicates.add(
                        cb.equal(root.get("vueloProgramadoId"), vueloProgramadoId)
                );
            }

            if (avionId != null) {
                predicates.add(
                        cb.equal(root.get("avionId"), avionId)
                );
            }

            if (tripulacionId != null) {
                predicates.add(
                        cb.equal(root.get("tripulacionId"), tripulacionId)
                );
            }

            if (estadoVueloId != null) {
                predicates.add(
                        cb.equal(root.get("estadoVueloId"), estadoVueloId)
                );
            }

            if (fechaSalidaReal != null) {
                predicates.add(
                        cb.equal(root.get("fechaSalidaReal"), fechaSalidaReal)
                );
            }

            if (fechaLlegadaReal != null) {
                predicates.add(
                        cb.equal(root.get("fechaLlegadaReal"), fechaLlegadaReal)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}