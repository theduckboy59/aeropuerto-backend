package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ClienteVueloDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteVueloSegmentoDisponibleResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                aeropuertoLlegadaId,
                fechaSalida
        );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId)
                .addValue("aeropuertoLlegadaId", aeropuertoLlegadaId)
                .addValue("fechaSalida", fechaSalida);

        List<ClienteVueloDisponibleResponse> vuelos = jdbc.query(
                sqlVuelosDisponibles(),
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

    private void validarFiltros(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida
    ) {

        if (aeropuertoSalidaId == null) {
            throw new BusinessException("Debe seleccionar aeropuerto de salida");
        }

        if (aeropuertoLlegadaId == null) {
            throw new BusinessException("Debe seleccionar aeropuerto de llegada");
        }

        if (fechaSalida == null) {
            throw new BusinessException("Debe seleccionar fecha de salida");
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

    private String sqlVuelosDisponibles() {
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
                  AND vp.fecha_salida = :fechaSalida
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
                """;
    }

    private String sqlSegmentosDisponibles() {
        return """
                SELECT
                    so.id AS segmento_operado_id,
                    sv.id AS segmento_vuelo_id,
                    so.orden_segmento AS orden_segmento,

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

                    avn.id AS avion_id,
                    avn.codigo_avion AS codigo_avion,

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

                FROM segmento_operado so
                INNER JOIN segmento_vuelo sv ON sv.id = so.segmento_vuelo_id
                INNER JOIN aeropuerto aps ON aps.id = sv.aeropuerto_salida_id
                INNER JOIN aeropuerto apl ON apl.id = sv.aeropuerto_llegada_id
                INNER JOIN avion avn ON avn.id = so.avion_id

                LEFT JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id
                LEFT JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id
                LEFT JOIN asiento_ubi au
                       ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema
                      AND au.avion_id = so.avion_id
                LEFT JOIN clase_vuelo cvas ON cvas.id = au.clase_vuelo_id

                WHERE so.vuelo_operado_id = :vueloOperadoId

                GROUP BY
                    so.id,
                    sv.id,
                    so.orden_segmento,
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
                    avn.id,
                    avn.codigo_avion

                ORDER BY so.orden_segmento ASC
                """;
    }

    private RowMapper<ClienteVueloDisponibleResponse> vueloMapper() {
        return (rs, rowNum) -> {
            ClienteVueloDisponibleResponse response = new ClienteVueloDisponibleResponse();

            response.setVueloOperadoId(rs.getInt("vuelo_operado_id"));
            response.setVueloProgramadoId(rs.getInt("vuelo_programado_id"));
            response.setVueloId(rs.getInt("vuelo_id"));
            response.setCodigoVuelo(rs.getString("codigo_vuelo"));

            response.setAerolineaId(rs.getInt("aerolinea_id"));
            response.setAerolineaNombre(rs.getString("aerolinea_nombre"));

            response.setAeropuertoSalidaId(rs.getInt("aeropuerto_salida_id"));
            response.setAeropuertoSalidaNombre(rs.getString("aeropuerto_salida_nombre"));
            response.setAeropuertoSalidaCodigoIata(rs.getString("aeropuerto_salida_codigo_iata"));

            response.setAeropuertoLlegadaId(rs.getInt("aeropuerto_llegada_id"));
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

            response.setTipoSegmentoVueloId(rs.getInt("tipo_segmento_vuelo_id"));
            response.setTipoSegmentoVueloNombre(rs.getString("tipo_segmento_vuelo_nombre"));
            response.setRequiereNuevoAsiento(rs.getBoolean("requiere_nuevo_asiento"));

            response.setCantidadSegmentos(rs.getInt("cantidad_segmentos"));
            response.setTuvoEscala(rs.getBoolean("tuvo_escala"));

            response.setAsientosDisponiblesTotal(rs.getInt("asientos_disponibles_total"));
            response.setAsientosDisponiblesEconomica(rs.getInt("asientos_disponibles_economica"));
            response.setAsientosDisponiblesEjecutiva(rs.getInt("asientos_disponibles_ejecutiva"));

            return response;
        };
    }

    private RowMapper<ClienteVueloSegmentoDisponibleResponse> segmentoMapper() {
        return (rs, rowNum) -> {
            ClienteVueloSegmentoDisponibleResponse response = new ClienteVueloSegmentoDisponibleResponse();

            response.setSegmentoOperadoId(rs.getInt("segmento_operado_id"));
            response.setSegmentoVueloId(rs.getInt("segmento_vuelo_id"));
            response.setOrdenSegmento(rs.getInt("orden_segmento"));

            response.setAeropuertoSalidaId(rs.getInt("aeropuerto_salida_id"));
            response.setAeropuertoSalidaNombre(rs.getString("aeropuerto_salida_nombre"));
            response.setAeropuertoSalidaCodigoIata(rs.getString("aeropuerto_salida_codigo_iata"));

            response.setAeropuertoLlegadaId(rs.getInt("aeropuerto_llegada_id"));
            response.setAeropuertoLlegadaNombre(rs.getString("aeropuerto_llegada_nombre"));
            response.setAeropuertoLlegadaCodigoIata(rs.getString("aeropuerto_llegada_codigo_iata"));

            response.setFechaSalida(getLocalDate(rs, "fecha_salida"));
            response.setHoraSalida(getLocalTime(rs, "hora_salida"));
            response.setFechaLlegada(getLocalDate(rs, "fecha_llegada"));
            response.setHoraLlegada(getLocalTime(rs, "hora_llegada"));

            response.setAvionId(rs.getInt("avion_id"));
            response.setCodigoAvion(rs.getString("codigo_avion"));

            response.setAsientosDisponiblesTotal(rs.getInt("asientos_disponibles_total"));
            response.setAsientosDisponiblesEconomica(rs.getInt("asientos_disponibles_economica"));
            response.setAsientosDisponiblesEjecutiva(rs.getInt("asientos_disponibles_ejecutiva"));

            return response;
        };
    }

    private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column);
        return value != null ? value.toLocalDate() : null;
    }

    private LocalTime getLocalTime(ResultSet rs, String column) throws SQLException {
        Time value = rs.getTime(column);
        return value != null ? value.toLocalTime() : null;
    }

    private Long getLong(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(value.toString());
    }
}