package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.SegmentoOperado;
import com.aeropuertolosprimos.backend.model.VueloOperado;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class VueloOperadoSpecification {

    private VueloOperadoSpecification() {
    }

    public static Specification<VueloOperado> filters(
            Integer vueloProgramadoId,
            Integer avionId,
            Integer tripulacionId,
            Integer estadoVueloId,
            LocalDate fechaSalidaReal,
            LocalDate fechaLlegadaReal
    ) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (vueloProgramadoId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("vueloProgramadoId"), vueloProgramadoId)
                );
            }

            if (estadoVueloId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("estadoVueloId"), estadoVueloId)
                );
            }

            if (avionId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var segmento = sub.from(SegmentoOperado.class);

                sub.select(segmento.get("id"))
                        .where(
                                cb.equal(segmento.get("vueloOperadoId"), root.get("id")),
                                cb.equal(segmento.get("avionId"), avionId)
                        );

                predicates = cb.and(predicates, cb.exists(sub));
            }

            if (tripulacionId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var segmento = sub.from(SegmentoOperado.class);

                sub.select(segmento.get("id"))
                        .where(
                                cb.equal(segmento.get("vueloOperadoId"), root.get("id")),
                                cb.equal(segmento.get("tripulacionId"), tripulacionId)
                        );

                predicates = cb.and(predicates, cb.exists(sub));
            }

            if (fechaSalidaReal != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var segmento = sub.from(SegmentoOperado.class);

                sub.select(segmento.get("id"))
                        .where(
                                cb.equal(segmento.get("vueloOperadoId"), root.get("id")),
                                cb.equal(segmento.get("fechaSalidaReal"), fechaSalidaReal)
                        );

                predicates = cb.and(predicates, cb.exists(sub));
            }

            if (fechaLlegadaReal != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var segmento = sub.from(SegmentoOperado.class);

                sub.select(segmento.get("id"))
                        .where(
                                cb.equal(segmento.get("vueloOperadoId"), root.get("id")),
                                cb.equal(segmento.get("fechaLlegadaReal"), fechaLlegadaReal)
                        );

                predicates = cb.and(predicates, cb.exists(sub));
            }

            return predicates;
        };
    }
}