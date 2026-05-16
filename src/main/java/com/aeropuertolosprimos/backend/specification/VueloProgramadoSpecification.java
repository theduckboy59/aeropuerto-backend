package com.aeropuertolosprimos.backend.specification;

import com.aeropuertolosprimos.backend.model.Aeropuerto;
import com.aeropuertolosprimos.backend.model.Vuelo;
import com.aeropuertolosprimos.backend.model.VueloProgramado;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class VueloProgramadoSpecification {

    public static Specification<VueloProgramado> filters(
            String q,
            String buscarSalida,
            String buscarLlegada,
            Integer aerolineaId,
            Integer estadoActivoId,
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida,
            LocalTime horaSalida,
            LocalDate fechaLlegada,
            LocalTime horaLlegada
    ) {

        return (root, query, cb) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {

                String search = likeValue(q);

                Predicate codigoVueloMatch = root.get("vueloId").in(
                        subqueryVuelosPorCodigo(
                                query,
                                cb,
                                search
                        )
                );

                Predicate aeropuertoSalidaMatch = root.get("aeropuertoSalidaId").in(
                        subqueryAeropuertosPorTexto(
                                query,
                                cb,
                                search
                        )
                );

                Predicate aeropuertoLlegadaMatch = root.get("aeropuertoLlegadaId").in(
                        subqueryAeropuertosPorTexto(
                                query,
                                cb,
                                search
                        )
                );

                predicates.add(
                        cb.or(
                                codigoVueloMatch,
                                aeropuertoSalidaMatch,
                                aeropuertoLlegadaMatch
                        )
                );
            }

            if (buscarSalida != null && !buscarSalida.isBlank()) {

                predicates.add(
                        root.get("aeropuertoSalidaId").in(
                                subqueryAeropuertosPorTexto(
                                        query,
                                        cb,
                                        likeValue(buscarSalida)
                                )
                        )
                );
            }

            if (buscarLlegada != null && !buscarLlegada.isBlank()) {

                predicates.add(
                        root.get("aeropuertoLlegadaId").in(
                                subqueryAeropuertosPorTexto(
                                        query,
                                        cb,
                                        likeValue(buscarLlegada)
                                )
                        )
                );
            }

            /*
             * Siempre se filtra por vuelos ACTIVOS.
             * El estado ya no viene desde el frontend.
             */
            List<Predicate> vueloPredicates = new ArrayList<>();

            Subquery<Integer> vueloSubquery = query.subquery(Integer.class);
            Root<Vuelo> vueloRoot = vueloSubquery.from(Vuelo.class);

            vueloPredicates.add(
                    cb.equal(
                            vueloRoot.get("estadoId"),
                            estadoActivoId
                    )
            );

            if (aerolineaId != null) {

                vueloPredicates.add(
                        cb.equal(
                                vueloRoot.get("aerolineaId"),
                                aerolineaId
                        )
                );
            }

            vueloSubquery
                    .select(vueloRoot.get("id"))
                    .where(
                            cb.and(
                                    vueloPredicates.toArray(new Predicate[0])
                            )
                    );

            predicates.add(
                    root.get("vueloId").in(vueloSubquery)
            );

            if (aeropuertoSalidaId != null) {

                predicates.add(
                        cb.equal(
                                root.get("aeropuertoSalidaId"),
                                aeropuertoSalidaId
                        )
                );
            }

            if (aeropuertoLlegadaId != null) {

                predicates.add(
                        cb.equal(
                                root.get("aeropuertoLlegadaId"),
                                aeropuertoLlegadaId
                        )
                );
            }

            if (fechaSalida != null) {

                predicates.add(
                        cb.equal(
                                root.get("fechaSalida"),
                                fechaSalida
                        )
                );
            }

            if (horaSalida != null) {

                predicates.add(
                        cb.equal(
                                root.get("horaSalida"),
                                horaSalida
                        )
                );
            }

            if (fechaLlegada != null) {

                predicates.add(
                        cb.equal(
                                root.get("fechaLlegada"),
                                fechaLlegada
                        )
                );
            }

            if (horaLlegada != null) {

                predicates.add(
                        cb.equal(
                                root.get("horaLlegada"),
                                horaLlegada
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

    private static Subquery<Integer> subqueryVuelosPorCodigo(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String search
    ) {

        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Vuelo> vueloRoot = subquery.from(Vuelo.class);

        subquery
                .select(vueloRoot.get("id"))
                .where(
                        cb.like(
                                cb.lower(
                                        vueloRoot.get("codigoVuelo").as(String.class)
                                ),
                                search
                        )
                );

        return subquery;
    }

    private static Subquery<Integer> subqueryAeropuertosPorTexto(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String search
    ) {

        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<Aeropuerto> aeropuertoRoot = subquery.from(Aeropuerto.class);

        subquery
                .select(aeropuertoRoot.get("id"))
                .where(
                        cb.or(
                                cb.like(
                                        cb.lower(aeropuertoRoot.get("nombre").as(String.class)),
                                        search
                                ),
                                cb.like(
                                        cb.lower(aeropuertoRoot.get("codigoIata").as(String.class)),
                                        search
                                ),
                                cb.like(
                                        cb.lower(aeropuertoRoot.get("codigoIcao").as(String.class)),
                                        search
                                ),
                                cb.like(
                                        cb.lower(aeropuertoRoot.get("ciudad").as(String.class)),
                                        search
                                ),
                                cb.like(
                                        cb.lower(aeropuertoRoot.get("pais").as(String.class)),
                                        search
                                )
                        )
                );

        return subquery;
    }

    private static String likeValue(
            String value
    ) {

        return "%" + value.trim().toLowerCase() + "%";
    }
}