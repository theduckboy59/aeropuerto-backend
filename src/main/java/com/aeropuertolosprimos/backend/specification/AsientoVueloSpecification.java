package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.AsientoUbi;
import com.aeropuertolosprimos.backend.model.AsientoVuelo;
import com.aeropuertolosprimos.backend.model.SegmentoOperado;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class AsientoVueloSpecification {

    private AsientoVueloSpecification() {
    }

    public static Specification<AsientoVuelo> filters(
            Integer vueloOperadoId,
            Integer segmentoOperadoId,
            Integer estadoAsientoId,
            Integer claseVueloId,
            Integer tipoAsientoId,
            Integer nivel,
            Integer fila,
            String columna,
            String numeroAsiento
    ) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (segmentoOperadoId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("segmentoOperadoId"), segmentoOperadoId)
                );
            }

            if (vueloOperadoId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var segmento = sub.from(SegmentoOperado.class);

                sub.select(segmento.get("id"))
                        .where(
                                cb.equal(segmento.get("id"), root.get("segmentoOperadoId")),
                                cb.equal(segmento.get("vueloOperadoId"), vueloOperadoId)
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            if (estadoAsientoId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("estadoAsientoId"), estadoAsientoId)
                );
            }

            if (claseVueloId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                sub.select(asiento.get("id"))
                        .where(
                                cb.equal(asiento.get("id"), root.get("asientoUbiId")),
                                cb.equal(asiento.get("claseVueloId"), claseVueloId)
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            if (tipoAsientoId != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                sub.select(asiento.get("id"))
                        .where(
                                cb.equal(asiento.get("id"), root.get("asientoUbiId")),
                                cb.equal(asiento.get("tipoAsientoId"), tipoAsientoId)
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            if (nivel != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                sub.select(asiento.get("id"))
                        .where(
                                cb.equal(asiento.get("id"), root.get("asientoUbiId")),
                                cb.equal(asiento.get("nivel"), nivel)
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            if (fila != null) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                sub.select(asiento.get("id"))
                        .where(
                                cb.equal(asiento.get("id"), root.get("asientoUbiId")),
                                cb.equal(asiento.get("fila"), fila)
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            if (columna != null && !columna.isBlank()) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                sub.select(asiento.get("id"))
                        .where(
                                cb.equal(asiento.get("id"), root.get("asientoUbiId")),
                                cb.equal(cb.upper(asiento.get("columna")), columna.toUpperCase())
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            if (numeroAsiento != null && !numeroAsiento.isBlank()) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                sub.select(asiento.get("id"))
                        .where(
                                cb.equal(asiento.get("id"), root.get("asientoUbiId")),
                                cb.equal(cb.upper(asiento.get("numeroAsiento")), numeroAsiento.toUpperCase())
                        );

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            return predicates;
        };
    }
}