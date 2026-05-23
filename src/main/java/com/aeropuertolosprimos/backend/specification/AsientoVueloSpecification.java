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

            if (
                    claseVueloId != null ||
                            tipoAsientoId != null ||
                            nivel != null ||
                            fila != null ||
                            columna != null ||
                            numeroAsiento != null
            ) {
                Subquery<Integer> sub = query.subquery(Integer.class);
                var asiento = sub.from(AsientoUbi.class);

                var asientoPredicates = cb.conjunction();

                asientoPredicates = cb.and(
                        asientoPredicates,
                        cb.equal(
                                asiento.get("codigoAsientoSistema"),
                                root.get("codigoAsientoSistema")
                        )
                );

                if (claseVueloId != null) {
                    asientoPredicates = cb.and(
                            asientoPredicates,
                            cb.equal(asiento.get("claseVueloId"), claseVueloId)
                    );
                }

                if (tipoAsientoId != null) {
                    asientoPredicates = cb.and(
                            asientoPredicates,
                            cb.equal(asiento.get("tipoAsientoId"), tipoAsientoId)
                    );
                }

                if (nivel != null) {
                    asientoPredicates = cb.and(
                            asientoPredicates,
                            cb.equal(asiento.get("nivel"), nivel)
                    );
                }

                if (fila != null) {
                    asientoPredicates = cb.and(
                            asientoPredicates,
                            cb.equal(asiento.get("fila"), fila)
                    );
                }

                if (columna != null && !columna.isBlank()) {
                    asientoPredicates = cb.and(
                            asientoPredicates,
                            cb.equal(
                                    cb.upper(asiento.get("columna")),
                                    columna.trim().toUpperCase()
                            )
                    );
                }

                if (numeroAsiento != null && !numeroAsiento.isBlank()) {
                    asientoPredicates = cb.and(
                            asientoPredicates,
                            cb.equal(
                                    cb.upper(asiento.get("numeroAsiento")),
                                    numeroAsiento.trim().toUpperCase()
                            )
                    );
                }

                sub.select(asiento.get("id"))
                        .where(asientoPredicates);

                predicates = cb.and(
                        predicates,
                        cb.exists(sub)
                );
            }

            return predicates;
        };
    }
}