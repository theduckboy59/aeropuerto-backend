package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ClienteDestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.dto.ClienteFechaDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteVueloDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteVueloSegmentoDisponibleResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteVueloDisponibleServiceImpl implements ClienteVueloDisponibleService {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteVueloDisponibleResponse> listarDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida
    ) {

        validarFiltros(
                aeropuertoSalidaId,
                aeropuertoLlegadaId
        );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId)
                .addValue("aeropuertoLlegadaId", aeropuertoLlegadaId);

        boolean filtrarPorFecha = fechaSalida != null;

        if (filtrarPorFecha) {
            params.addValue("fechaSalida", fechaSalida);
        }

        List<ClienteVueloDisponibleResponse> vuelos = jdbc.query(
                sqlVuelosDisponibles(filtrarPorFecha),
                params,
                vueloMapper()
        );

        for (ClienteVueloDisponibleResponse vuelo : vuelos) {
            vuelo.setSegmentos(
                    buscarSegmentos(
                            vuelo.getVueloOperadoId()
                    )
            );
        }

        return vuelos;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteVueloDisponibleResponse obtenerDetalle(
            Integer vueloOperadoId
    ) {

        if (vueloOperadoId == null) {
            throw new BusinessException("Debe ingresar el vuelo operado");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vueloOperadoId", vueloOperadoId);

        List<ClienteVueloDisponibleResponse> vuelos = jdbc.query(
                sqlDetalleVueloDisponible(),
                params,
                vueloMapper()
        );

        if (vuelos.isEmpty()) {
            throw new BusinessException("Vuelo disponible no encontrado");
        }

        ClienteVueloDisponibleResponse vuelo = vuelos.get(0);

        vuelo.setSegmentos(
                buscarSegmentos(
                        vuelo.getVueloOperadoId()
                )
        );

        return vuelo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDestinoAutorizadoResponse> listarDestinosAutorizados(
            Integer aeropuertoSalidaId
    ) {

        if (aeropuertoSalidaId == null) {
            throw new BusinessException("Debe seleccionar aeropuerto de salida");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId);

        return jdbc.query(
                sqlDestinosAutorizadosPorOrigen(),
                params,
                destinoAutorizadoMapper()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteFechaDisponibleResponse> listarFechasDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId
    ) {

        validarFiltros(
                aeropuertoSalidaId,
                aeropuertoLlegadaId
        );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId)
                .addValue("aeropuertoLlegadaId", aeropuertoLlegadaId);

        return jdbc.query(
                sqlFechasDisponibles(),
                params,
                fechaDisponibleMapper()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteFechaDisponibleResponse> listarFechasRegresoDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida
    ) {

        validarFiltros(
                aeropuertoSalidaId,
                aeropuertoLlegadaId
        );

        if (fechaSalida == null) {
            throw new BusinessException("Debe seleccionar fecha de salida para buscar regreso");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId)
                .addValue("aeropuertoLlegadaId", aeropuertoLlegadaId)
                .addValue("fechaSalida", fechaSalida);

        return jdbc.query(
                sqlFechasRegresoDisponibles(),
                params,
                fechaDisponibleMapper()
        );
    }

    private void validarFiltros(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId
    ) {

        if (aeropuertoSalidaId == null) {
            throw new BusinessException("Debe seleccionar aeropuerto de salida");
        }

        if (aeropuertoLlegadaId == null) {
            throw new BusinessException("Debe seleccionar aeropuerto de llegada");
        }

        if (aeropuertoSalidaId.equals(aeropuertoLlegadaId)) {
            throw new BusinessException("No se puede seleccionar el mismo aeropuerto de salida y llegada");
        }
    }

    private List<ClienteVueloSegmentoDisponibleResponse> buscarSegmentos(
            Integer vueloOperadoId
    ) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vueloOperadoId", vueloOperadoId);

        return jdbc.query(
                sqlSegmentosDisponibles(),
                params,
                segmentoMapper()
        );
    }

    private String sqlVuelosDisponibles(
            boolean filtrarPorFecha
    ) {

        String filtroFecha;

        if (filtrarPorFecha) {
            filtroFecha = """
                      AND vp.fecha_salida = :fechaSalida
                    """;
        } else {
            filtroFecha = """
                      AND (
                            vp.fecha_salida > CURRENT_DATE
                            OR (
                                vp.fecha_salida = CURRENT_DATE
                                AND vp.hora_salida >= LOCALTIME
                            )
                      )
                    """;
        }

        return """
                SELECT
                    vo.id AS vuelo_operado_id,
                    vp.id AS vuelo_programado_id,
                    v.id AS vuelo_id,
                    v.codigo_vuelo AS codigo_vuelo,

                    a.id AS aerolinea_id,
                    a.nombre AS aerolinea_nombre,

                    aps.id AS aeropuerto_salida_id,
                    aps.nombre AS aeropuerto_salida_nombre,
                    aps.codigo_iata AS aeropuerto_salida_codigo_iata,

                    apl.id AS aeropuerto_llegada_id,
                    apl.nombre AS aeropuerto_llegada_nombre,
                    apl.codigo_iata AS aeropuerto_llegada_codigo_iata,

                    vp.puerta_embarque_salida AS puerta_embarque_salida,
                    vp.puerta_embarque_llegada AS puerta_embarque_llegada,

                    vp.fecha_salida AS fecha_salida,
                    vp.hora_salida AS hora_salida,
                    vp.fecha_llegada AS fecha_llegada,
                    vp.hora_llegada AS hora_llegada,

                    CAST(
                        EXTRACT(
                            EPOCH FROM (
                                (vp.fecha_llegada + vp.hora_llegada)
                                -
                                (vp.fecha_salida + vp.hora_salida)
                            )
                        ) / 60
                        AS BIGINT
                    ) AS duracion_minutos,

                    (
                        SELECT pv.precio
                        FROM precio_vuelo pv
                        INNER JOIN clase_vuelo cvp ON cvp.id = pv.clase_vuelo_id
                        WHERE pv.vuelo_programado_id = vp.id
                          AND pv.fecha_vigencia_hasta IS NULL
                          AND UPPER(cvp.nombre) LIKE 'ECON%'
                        ORDER BY pv.id DESC
                        LIMIT 1
                    ) AS precio_economica,

                    (
                        SELECT pv.precio
                        FROM precio_vuelo pv
                        INNER JOIN clase_vuelo cvp ON cvp.id = pv.clase_vuelo_id
                        WHERE pv.vuelo_programado_id = vp.id
                          AND pv.fecha_vigencia_hasta IS NULL
                          AND UPPER(cvp.nombre) LIKE 'EJEC%'
                        ORDER BY pv.id DESC
                        LIMIT 1
                    ) AS precio_ejecutiva,

                    tsv.id AS tipo_segmento_vuelo_id,
                    tsv.nombre AS tipo_segmento_vuelo_nombre,

                    CASE
                        WHEN UPPER(tsv.nombre) = 'CAMBIO_AVION' THEN TRUE
                        ELSE FALSE
                    END AS requiere_nuevo_asiento,

                    vo.cantidad_segmentos AS cantidad_segmentos,
                    vo.tuvo_escala AS tuvo_escala,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE' THEN 1
                        END
                    ) AS asientos_disponibles_total,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE'
                             AND UPPER(cvas.nombre) LIKE 'ECON%'
                            THEN 1
                        END
                    ) AS asientos_disponibles_economica,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE'
                             AND UPPER(cvas.nombre) LIKE 'EJEC%'
                            THEN 1
                        END
                    ) AS asientos_disponibles_ejecutiva

                FROM vuelo_operado vo
                INNER JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                INNER JOIN vuelo v ON v.id = vp.vuelo_id
                INNER JOIN status_catalog sc ON sc.id = v.estado_id
                INNER JOIN aerolinea a ON a.id = v.aerolinea_id
                INNER JOIN aeropuerto aps ON aps.id = vp.aeropuerto_salida_id
                INNER JOIN aeropuerto apl ON apl.id = vp.aeropuerto_llegada_id
                INNER JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id
                INNER JOIN tipo_segmento_vuelo tsv ON tsv.id = vo.tipo_segmento_vuelo_id

                LEFT JOIN segmento_operado so ON so.vuelo_operado_id = vo.id
                LEFT JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id
                LEFT JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id
                LEFT JOIN asiento_ubi au
                       ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema
                      AND au.avion_id = so.avion_id
                LEFT JOIN clase_vuelo cvas ON cvas.id = au.clase_vuelo_id

                WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId
                  AND vp.aeropuerto_llegada_id = :aeropuertoLlegadaId
                """ + filtroFecha + """
                  AND UPPER(sc.name) = 'ACTIVO'
                  AND UPPER(ev.nombre) = 'PROGRAMADO'

                GROUP BY
                    vo.id,
                    vp.id,
                    v.id,
                    v.codigo_vuelo,
                    a.id,
                    a.nombre,
                    aps.id,
                    aps.nombre,
                    aps.codigo_iata,
                    apl.id,
                    apl.nombre,
                    apl.codigo_iata,
                    vp.puerta_embarque_salida,
                    vp.puerta_embarque_llegada,
                    vp.fecha_salida,
                    vp.hora_salida,
                    vp.fecha_llegada,
                    vp.hora_llegada,
                    tsv.id,
                    tsv.nombre,
                    vo.cantidad_segmentos,
                    vo.tuvo_escala

                HAVING COUNT(
                    CASE
                        WHEN UPPER(ea.nombre) = 'DISPONIBLE' THEN 1
                    END
                ) > 0

                ORDER BY
                    vp.fecha_salida ASC,
                    vp.hora_salida ASC,
                    v.codigo_vuelo ASC
                """;
    }

    private String sqlDetalleVueloDisponible() {
        return """
                SELECT
                    vo.id AS vuelo_operado_id,
                    vp.id AS vuelo_programado_id,
                    v.id AS vuelo_id,
                    v.codigo_vuelo AS codigo_vuelo,

                    a.id AS aerolinea_id,
                    a.nombre AS aerolinea_nombre,

                    aps.id AS aeropuerto_salida_id,
                    aps.nombre AS aeropuerto_salida_nombre,
                    aps.codigo_iata AS aeropuerto_salida_codigo_iata,

                    apl.id AS aeropuerto_llegada_id,
                    apl.nombre AS aeropuerto_llegada_nombre,
                    apl.codigo_iata AS aeropuerto_llegada_codigo_iata,

                    vp.puerta_embarque_salida AS puerta_embarque_salida,
                    vp.puerta_embarque_llegada AS puerta_embarque_llegada,

                    vp.fecha_salida AS fecha_salida,
                    vp.hora_salida AS hora_salida,
                    vp.fecha_llegada AS fecha_llegada,
                    vp.hora_llegada AS hora_llegada,

                    CAST(
                        EXTRACT(
                            EPOCH FROM (
                                (vp.fecha_llegada + vp.hora_llegada)
                                -
                                (vp.fecha_salida + vp.hora_salida)
                            )
                        ) / 60
                        AS BIGINT
                    ) AS duracion_minutos,

                    (
                        SELECT pv.precio
                        FROM precio_vuelo pv
                        INNER JOIN clase_vuelo cvp ON cvp.id = pv.clase_vuelo_id
                        WHERE pv.vuelo_programado_id = vp.id
                          AND pv.fecha_vigencia_hasta IS NULL
                          AND UPPER(cvp.nombre) LIKE 'ECON%'
                        ORDER BY pv.id DESC
                        LIMIT 1
                    ) AS precio_economica,

                    (
                        SELECT pv.precio
                        FROM precio_vuelo pv
                        INNER JOIN clase_vuelo cvp ON cvp.id = pv.clase_vuelo_id
                        WHERE pv.vuelo_programado_id = vp.id
                          AND pv.fecha_vigencia_hasta IS NULL
                          AND UPPER(cvp.nombre) LIKE 'EJEC%'
                        ORDER BY pv.id DESC
                        LIMIT 1
                    ) AS precio_ejecutiva,

                    tsv.id AS tipo_segmento_vuelo_id,
                    tsv.nombre AS tipo_segmento_vuelo_nombre,

                    CASE
                        WHEN UPPER(tsv.nombre) = 'CAMBIO_AVION' THEN TRUE
                        ELSE FALSE
                    END AS requiere_nuevo_asiento,

                    vo.cantidad_segmentos AS cantidad_segmentos,
                    vo.tuvo_escala AS tuvo_escala,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE' THEN 1
                        END
                    ) AS asientos_disponibles_total,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE'
                             AND UPPER(cvas.nombre) LIKE 'ECON%'
                            THEN 1
                        END
                    ) AS asientos_disponibles_economica,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE'
                             AND UPPER(cvas.nombre) LIKE 'EJEC%'
                            THEN 1
                        END
                    ) AS asientos_disponibles_ejecutiva

                FROM vuelo_operado vo
                INNER JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                INNER JOIN vuelo v ON v.id = vp.vuelo_id
                INNER JOIN status_catalog sc ON sc.id = v.estado_id
                INNER JOIN aerolinea a ON a.id = v.aerolinea_id
                INNER JOIN aeropuerto aps ON aps.id = vp.aeropuerto_salida_id
                INNER JOIN aeropuerto apl ON apl.id = vp.aeropuerto_llegada_id
                INNER JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id
                INNER JOIN tipo_segmento_vuelo tsv ON tsv.id = vo.tipo_segmento_vuelo_id

                LEFT JOIN segmento_operado so ON so.vuelo_operado_id = vo.id
                LEFT JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id
                LEFT JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id
                LEFT JOIN asiento_ubi au
                       ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema
                      AND au.avion_id = so.avion_id
                LEFT JOIN clase_vuelo cvas ON cvas.id = au.clase_vuelo_id

                WHERE vo.id = :vueloOperadoId
                  AND UPPER(sc.name) = 'ACTIVO'
                  AND UPPER(ev.nombre) = 'PROGRAMADO'

                GROUP BY
                    vo.id,
                    vp.id,
                    v.id,
                    v.codigo_vuelo,
                    a.id,
                    a.nombre,
                    aps.id,
                    aps.nombre,
                    aps.codigo_iata,
                    apl.id,
                    apl.nombre,
                    apl.codigo_iata,
                    vp.puerta_embarque_salida,
                    vp.puerta_embarque_llegada,
                    vp.fecha_salida,
                    vp.hora_salida,
                    vp.fecha_llegada,
                    vp.hora_llegada,
                    tsv.id,
                    tsv.nombre,
                    vo.cantidad_segmentos,
                    vo.tuvo_escala

                HAVING COUNT(
                    CASE
                        WHEN UPPER(ea.nombre) = 'DISPONIBLE' THEN 1
                    END
                ) > 0

                ORDER BY
                    vp.fecha_salida ASC,
                    vp.hora_salida ASC,
                    v.codigo_vuelo ASC
                """;
    }

    private String sqlSegmentosDisponibles() {
        return """
                SELECT
                    so.id AS segmento_operado_id,
                    sv.id AS segmento_vuelo_id,
                    sv.orden_segmento AS orden_segmento,

                    aps.id AS aeropuerto_salida_id,
                    aps.nombre AS aeropuerto_salida_nombre,
                    aps.codigo_iata AS aeropuerto_salida_codigo_iata,

                    apl.id AS aeropuerto_llegada_id,
                    apl.nombre AS aeropuerto_llegada_nombre,
                    apl.codigo_iata AS aeropuerto_llegada_codigo_iata,

                    sv.fecha_salida AS fecha_salida,
                    sv.hora_salida AS hora_salida,
                    sv.fecha_llegada AS fecha_llegada,
                    sv.hora_llegada AS hora_llegada,

                    av.id AS avion_id,
                    av.codigo_avion AS codigo_avion,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE' THEN 1
                        END
                    ) AS asientos_disponibles_total,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE'
                             AND UPPER(cv.nombre) LIKE 'ECON%'
                            THEN 1
                        END
                    ) AS asientos_disponibles_economica,

                    COUNT(
                        CASE
                            WHEN UPPER(ea.nombre) = 'DISPONIBLE'
                             AND UPPER(cv.nombre) LIKE 'EJEC%'
                            THEN 1
                        END
                    ) AS asientos_disponibles_ejecutiva

                FROM segmento_operado so
                INNER JOIN segmento_vuelo sv ON sv.id = so.segmento_vuelo_id
                INNER JOIN aeropuerto aps ON aps.id = sv.aeropuerto_salida_id
                INNER JOIN aeropuerto apl ON apl.id = sv.aeropuerto_llegada_id
                INNER JOIN avion av ON av.id = so.avion_id

                LEFT JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id
                LEFT JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id
                LEFT JOIN asiento_ubi au
                       ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema
                      AND au.avion_id = so.avion_id
                LEFT JOIN clase_vuelo cv ON cv.id = au.clase_vuelo_id

                WHERE so.vuelo_operado_id = :vueloOperadoId

                GROUP BY
                    so.id,
                    sv.id,
                    sv.orden_segmento,
                    aps.id,
                    aps.nombre,
                    aps.codigo_iata,
                    apl.id,
                    apl.nombre,
                    apl.codigo_iata,
                    sv.fecha_salida,
                    sv.hora_salida,
                    sv.fecha_llegada,
                    sv.hora_llegada,
                    av.id,
                    av.codigo_avion

                ORDER BY
                    sv.orden_segmento ASC
                """;
    }

    private String sqlDestinosAutorizadosPorOrigen() {
        return """
                SELECT DISTINCT
                    ap_llegada.id AS aeropuerto_id,
                    ap_llegada.nombre AS nombre,
                    ap_llegada.codigo_iata AS codigo_iata,
                    ap_llegada.ciudad AS ciudad,
                    ap_llegada.pais AS pais

                FROM destino_autorizado da_origen
                INNER JOIN destino_autorizado da_destino
                        ON da_destino.aerolinea_id = da_origen.aerolinea_id
                INNER JOIN aeropuerto ap_origen
                        ON ap_origen.id = da_origen.aeropuerto_id
                INNER JOIN aeropuerto ap_llegada
                        ON ap_llegada.id = da_destino.aeropuerto_id
                INNER JOIN status_catalog sc_origen
                        ON sc_origen.id = da_origen.estado_id
                INNER JOIN status_catalog sc_destino
                        ON sc_destino.id = da_destino.estado_id

                WHERE ap_origen.id = :aeropuertoSalidaId
                  AND ap_llegada.id <> ap_origen.id
                  AND UPPER(sc_origen.name) = 'ACTIVO'
                  AND UPPER(sc_destino.name) = 'ACTIVO'

                ORDER BY
                    ap_llegada.pais ASC,
                    ap_llegada.ciudad ASC,
                    ap_llegada.nombre ASC
                """;
    }

    private String sqlFechasDisponibles() {
        return """
                SELECT
                    vp.fecha_salida AS fecha_salida,
                    COUNT(DISTINCT vo.id) AS vuelos_disponibles,
                    MIN(pv.precio) AS precio_minimo

                FROM vuelo_operado vo
                INNER JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                INNER JOIN vuelo v ON v.id = vp.vuelo_id
                INNER JOIN status_catalog sc ON sc.id = v.estado_id
                INNER JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id
                INNER JOIN segmento_operado so ON so.vuelo_operado_id = vo.id
                INNER JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id
                INNER JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id

                LEFT JOIN precio_vuelo pv
                       ON pv.vuelo_programado_id = vp.id
                      AND pv.fecha_vigencia_hasta IS NULL

                WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId
                  AND vp.aeropuerto_llegada_id = :aeropuertoLlegadaId
                  AND (
                        vp.fecha_salida > CURRENT_DATE
                        OR (
                            vp.fecha_salida = CURRENT_DATE
                            AND vp.hora_salida >= LOCALTIME
                        )
                  )
                  AND UPPER(sc.name) = 'ACTIVO'
                  AND UPPER(ev.nombre) = 'PROGRAMADO'
                  AND UPPER(ea.nombre) = 'DISPONIBLE'

                GROUP BY
                    vp.fecha_salida

                ORDER BY
                    vp.fecha_salida ASC
                """;
    }

    private String sqlFechasRegresoDisponibles() {
        return """
                SELECT
                    vp.fecha_salida AS fecha_salida,
                    COUNT(DISTINCT vo.id) AS vuelos_disponibles,
                    MIN(pv.precio) AS precio_minimo

                FROM vuelo_operado vo
                INNER JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                INNER JOIN vuelo v ON v.id = vp.vuelo_id
                INNER JOIN status_catalog sc ON sc.id = v.estado_id
                INNER JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id
                INNER JOIN segmento_operado so ON so.vuelo_operado_id = vo.id
                INNER JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id
                INNER JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id

                LEFT JOIN precio_vuelo pv
                       ON pv.vuelo_programado_id = vp.id
                      AND pv.fecha_vigencia_hasta IS NULL

                WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId
                  AND vp.aeropuerto_llegada_id = :aeropuertoLlegadaId
                  AND vp.fecha_salida > :fechaSalida
                  AND UPPER(sc.name) = 'ACTIVO'
                  AND UPPER(ev.nombre) = 'PROGRAMADO'
                  AND UPPER(ea.nombre) = 'DISPONIBLE'

                GROUP BY
                    vp.fecha_salida

                ORDER BY
                    vp.fecha_salida ASC
                """;
    }

    private RowMapper<ClienteVueloDisponibleResponse> vueloMapper() {
        return (rs, rowNum) -> {
            ClienteVueloDisponibleResponse response = new ClienteVueloDisponibleResponse();

            response.setVueloOperadoId(getInteger(rs, "vuelo_operado_id"));
            response.setVueloProgramadoId(getInteger(rs, "vuelo_programado_id"));
            response.setVueloId(getInteger(rs, "vuelo_id"));
            response.setCodigoVuelo(rs.getString("codigo_vuelo"));

            response.setAerolineaId(getInteger(rs, "aerolinea_id"));
            response.setAerolineaNombre(rs.getString("aerolinea_nombre"));

            response.setAeropuertoSalidaId(getInteger(rs, "aeropuerto_salida_id"));
            response.setAeropuertoSalidaNombre(rs.getString("aeropuerto_salida_nombre"));
            response.setAeropuertoSalidaCodigoIata(rs.getString("aeropuerto_salida_codigo_iata"));

            response.setAeropuertoLlegadaId(getInteger(rs, "aeropuerto_llegada_id"));
            response.setAeropuertoLlegadaNombre(rs.getString("aeropuerto_llegada_nombre"));
            response.setAeropuertoLlegadaCodigoIata(rs.getString("aeropuerto_llegada_codigo_iata"));

            response.setPuertaEmbarqueSalida(rs.getString("puerta_embarque_salida"));
            response.setPuertaEmbarqueLlegada(rs.getString("puerta_embarque_llegada"));

            response.setFechaSalida(getLocalDate(rs, "fecha_salida"));
            response.setHoraSalida(getLocalTime(rs, "hora_salida"));
            response.setFechaLlegada(getLocalDate(rs, "fecha_llegada"));
            response.setHoraLlegada(getLocalTime(rs, "hora_llegada"));

            response.setDuracionMinutos(getLong(rs, "duracion_minutos"));

            response.setPrecioEconomica(rs.getBigDecimal("precio_economica"));
            response.setPrecioEjecutiva(rs.getBigDecimal("precio_ejecutiva"));

            response.setTipoSegmentoVueloId(getInteger(rs, "tipo_segmento_vuelo_id"));
            response.setTipoSegmentoVueloNombre(rs.getString("tipo_segmento_vuelo_nombre"));

            response.setRequiereNuevoAsiento(rs.getBoolean("requiere_nuevo_asiento"));

            response.setCantidadSegmentos(getInteger(rs, "cantidad_segmentos"));
            response.setTuvoEscala(rs.getBoolean("tuvo_escala"));

            response.setAsientosDisponiblesTotal(getInteger(rs, "asientos_disponibles_total"));
            response.setAsientosDisponiblesEconomica(getInteger(rs, "asientos_disponibles_economica"));
            response.setAsientosDisponiblesEjecutiva(getInteger(rs, "asientos_disponibles_ejecutiva"));

            return response;
        };
    }

    private RowMapper<ClienteVueloSegmentoDisponibleResponse> segmentoMapper() {
        return (rs, rowNum) -> {
            ClienteVueloSegmentoDisponibleResponse response = new ClienteVueloSegmentoDisponibleResponse();

            response.setSegmentoOperadoId(getInteger(rs, "segmento_operado_id"));
            response.setSegmentoVueloId(getInteger(rs, "segmento_vuelo_id"));
            response.setOrdenSegmento(getInteger(rs, "orden_segmento"));

            response.setAeropuertoSalidaId(getInteger(rs, "aeropuerto_salida_id"));
            response.setAeropuertoSalidaNombre(rs.getString("aeropuerto_salida_nombre"));
            response.setAeropuertoSalidaCodigoIata(rs.getString("aeropuerto_salida_codigo_iata"));

            response.setAeropuertoLlegadaId(getInteger(rs, "aeropuerto_llegada_id"));
            response.setAeropuertoLlegadaNombre(rs.getString("aeropuerto_llegada_nombre"));
            response.setAeropuertoLlegadaCodigoIata(rs.getString("aeropuerto_llegada_codigo_iata"));

            response.setFechaSalida(getLocalDate(rs, "fecha_salida"));
            response.setHoraSalida(getLocalTime(rs, "hora_salida"));
            response.setFechaLlegada(getLocalDate(rs, "fecha_llegada"));
            response.setHoraLlegada(getLocalTime(rs, "hora_llegada"));

            response.setAvionId(getInteger(rs, "avion_id"));
            response.setCodigoAvion(rs.getString("codigo_avion"));

            response.setAsientosDisponiblesTotal(getInteger(rs, "asientos_disponibles_total"));
            response.setAsientosDisponiblesEconomica(getInteger(rs, "asientos_disponibles_economica"));
            response.setAsientosDisponiblesEjecutiva(getInteger(rs, "asientos_disponibles_ejecutiva"));

            return response;
        };
    }

    private RowMapper<ClienteDestinoAutorizadoResponse> destinoAutorizadoMapper() {
        return (rs, rowNum) -> {
            ClienteDestinoAutorizadoResponse response = new ClienteDestinoAutorizadoResponse();

            response.setAeropuertoId(getInteger(rs, "aeropuerto_id"));
            response.setNombre(rs.getString("nombre"));
            response.setCodigoIata(rs.getString("codigo_iata"));
            response.setCiudad(rs.getString("ciudad"));
            response.setPais(rs.getString("pais"));

            return response;
        };
    }

    private RowMapper<ClienteFechaDisponibleResponse> fechaDisponibleMapper() {
        return (rs, rowNum) -> {
            ClienteFechaDisponibleResponse response = new ClienteFechaDisponibleResponse();

            response.setFechaSalida(getLocalDate(rs, "fecha_salida"));
            response.setVuelosDisponibles(getLong(rs, "vuelos_disponibles"));

            BigDecimal precioMinimo = rs.getBigDecimal("precio_minimo");
            response.setPrecioMinimo(precioMinimo);

            return response;
        };
    }

    private Integer getInteger(
            ResultSet rs,
            String column
    ) throws SQLException {

        Object value = rs.getObject(column);

        if (value == null) {
            return null;
        }

        return ((Number) value).intValue();
    }

    private Long getLong(
            ResultSet rs,
            String column
    ) throws SQLException {

        Object value = rs.getObject(column);

        if (value == null) {
            return null;
        }

        return ((Number) value).longValue();
    }

    private LocalDate getLocalDate(
            ResultSet rs,
            String column
    ) throws SQLException {

        Date value = rs.getDate(column);

        if (value == null) {
            return null;
        }

        return value.toLocalDate();
    }

    private LocalTime getLocalTime(
            ResultSet rs,
            String column
    ) throws SQLException {

        Time value = rs.getTime(column);

        if (value == null) {
            return null;
        }

        return value.toLocalTime();
    }
}