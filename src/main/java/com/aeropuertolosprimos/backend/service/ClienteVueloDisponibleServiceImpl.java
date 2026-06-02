package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ClienteAeropuertoDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteDestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.dto.ClienteFechaDisponibleResponse;
import com.aeropuertolosprimos.backend.dto.ClienteUbicacionDisponibleResponse;
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
    public List<ClienteUbicacionDisponibleResponse> buscarOrigenes(String q) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        boolean filtrar = tieneTexto(q);

        if (filtrar) {
            params.addValue("q", like(q));
        }

        return jdbc.query(
                sqlOrigenes(filtrar),
                params,
                ubicacionMapper()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteAeropuertoDisponibleResponse> buscarAeropuertosSalida(
            String pais,
            String ciudad,
            String q
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        boolean filtrarPais = tieneTexto(pais);
        boolean filtrarCiudad = tieneTexto(ciudad);
        boolean filtrarQ = tieneTexto(q);

        if (filtrarPais) {
            params.addValue("pais", normalizar(pais));
        }

        if (filtrarCiudad) {
            params.addValue("ciudad", normalizar(ciudad));
        }

        if (filtrarQ) {
            params.addValue("q", like(q));
        }

        return jdbc.query(
                sqlAeropuertosSalida(filtrarPais, filtrarCiudad, filtrarQ),
                params,
                aeropuertoDisponibleMapper()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteUbicacionDisponibleResponse> buscarDestinosUbicaciones(
            Integer aeropuertoSalidaId,
            String q
    ) {
        validarAeropuertoSalida(aeropuertoSalidaId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId);

        boolean filtrar = tieneTexto(q);

        if (filtrar) {
            params.addValue("q", like(q));
        }

        return jdbc.query(
                sqlDestinosUbicaciones(filtrar),
                params,
                ubicacionMapper()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteAeropuertoDisponibleResponse> buscarAeropuertosDestino(
            Integer aeropuertoSalidaId,
            String pais,
            String ciudad,
            String q
    ) {
        validarAeropuertoSalida(aeropuertoSalidaId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId);

        boolean filtrarPais = tieneTexto(pais);
        boolean filtrarCiudad = tieneTexto(ciudad);
        boolean filtrarQ = tieneTexto(q);

        if (filtrarPais) {
            params.addValue("pais", normalizar(pais));
        }

        if (filtrarCiudad) {
            params.addValue("ciudad", normalizar(ciudad));
        }

        if (filtrarQ) {
            params.addValue("q", like(q));
        }

        return jdbc.query(
                sqlAeropuertosDestino(filtrarPais, filtrarCiudad, filtrarQ),
                params,
                aeropuertoDisponibleMapper()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteVueloDisponibleResponse> listarDisponibles(
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida
    ) {
        validarFiltros(aeropuertoSalidaId, aeropuertoLlegadaId);

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
            vuelo.setSegmentos(buscarSegmentos(vuelo.getVueloOperadoId()));
        }

        return vuelos;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteVueloDisponibleResponse obtenerDetalle(Integer vueloOperadoId) {
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
        vuelo.setSegmentos(buscarSegmentos(vuelo.getVueloOperadoId()));

        return vuelo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDestinoAutorizadoResponse> listarDestinosAutorizados(Integer aeropuertoSalidaId) {
        validarAeropuertoSalida(aeropuertoSalidaId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId);

        return jdbc.query(
                sqlDestinosAutorizadosConVuelos(),
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
        validarFiltros(aeropuertoSalidaId, aeropuertoLlegadaId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId)
                .addValue("aeropuertoLlegadaId", aeropuertoLlegadaId);

        return jdbc.query(
                sqlFechasDisponibles(false),
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
        validarFiltros(aeropuertoSalidaId, aeropuertoLlegadaId);

        if (fechaSalida == null) {
            throw new BusinessException("Debe ingresar fecha de salida");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("aeropuertoSalidaId", aeropuertoSalidaId)
                .addValue("aeropuertoLlegadaId", aeropuertoLlegadaId)
                .addValue("fechaSalida", fechaSalida);

        return jdbc.query(
                sqlFechasDisponibles(true),
                params,
                fechaDisponibleMapper()
        );
    }

    private List<ClienteVueloSegmentoDisponibleResponse> buscarSegmentos(Integer vueloOperadoId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vueloOperadoId", vueloOperadoId);

        return jdbc.query(
                sqlSegmentosVueloOperado(),
                params,
                segmentoMapper()
        );
    }

    private void validarAeropuertoSalida(Integer aeropuertoSalidaId) {
        if (aeropuertoSalidaId == null) {
            throw new BusinessException("Debe seleccionar aeropuerto de salida");
        }
    }

    private void validarFiltros(Integer aeropuertoSalidaId, Integer aeropuertoLlegadaId) {
        if (aeropuertoSalidaId == null || aeropuertoLlegadaId == null) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (aeropuertoSalidaId.equals(aeropuertoLlegadaId)) {
            throw new BusinessException("No se puede seleccionar el mismo aeropuerto de salida y llegada.");
        }
    }

    private String sqlOrigenes(boolean filtrar) {
        String sql =
                "SELECT " +
                        "ap.pais AS pais, " +
                        "ap.ciudad AS ciudad, " +
                        "COUNT(DISTINCT ap.id) AS total_aeropuertos, " +
                        "COUNT(DISTINCT vo.id) AS total_vuelos " +
                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aeropuerto ap ON ap.id = vp.aeropuerto_salida_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_ap ON sc_ap.id = ap.estado_id " +
                        "WHERE UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_ap.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE " +
                        "AND EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM segmento_operado so " +
                        "    JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    WHERE so.vuelo_operado_id = vo.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") ";

        if (filtrar) {
            sql += filtroUbicacion("ap");
        }

        sql +=
                "GROUP BY ap.pais, ap.ciudad " +
                        "ORDER BY ap.pais, ap.ciudad " +
                        "LIMIT 30";

        return sql;
    }

    private String sqlAeropuertosSalida(boolean filtrarPais, boolean filtrarCiudad, boolean filtrarQ) {
        String sql =
                "SELECT " +
                        "ap.id AS aeropuerto_id, " +
                        "ap.nombre AS nombre, " +
                        "ap.codigo_iata AS codigo_iata, " +
                        "ap.codigo_icao AS codigo_icao, " +
                        "ap.pais AS pais, " +
                        "ap.ciudad AS ciudad, " +
                        "COUNT(DISTINCT vo.id) AS total_vuelos, " +
                        "COUNT(DISTINCT CASE WHEN ea.id IS NOT NULL THEN avu.id END) AS asientos_disponibles_total " +
                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aeropuerto ap ON ap.id = vp.aeropuerto_salida_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_ap ON sc_ap.id = ap.estado_id " +
                        "LEFT JOIN segmento_operado so ON so.vuelo_operado_id = vo.id " +
                        "LEFT JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "LEFT JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        "WHERE UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_ap.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE ";

        if (filtrarPais) {
            sql += "AND LOWER(ap.pais) = :pais ";
        }

        if (filtrarCiudad) {
            sql += "AND LOWER(ap.ciudad) = :ciudad ";
        }

        if (filtrarQ) {
            sql += filtroUbicacion("ap");
        }

        sql +=
                "GROUP BY ap.id, ap.nombre, ap.codigo_iata, ap.codigo_icao, ap.pais, ap.ciudad " +
                        "HAVING COUNT(DISTINCT CASE WHEN ea.id IS NOT NULL THEN avu.id END) > 0 " +
                        "ORDER BY ap.pais, ap.ciudad, ap.nombre " +
                        "LIMIT 30";

        return sql;
    }

    private String sqlDestinosUbicaciones(boolean filtrar) {
        String sql =
                "SELECT " +
                        "ap_destino.pais AS pais, " +
                        "ap_destino.ciudad AS ciudad, " +
                        "COUNT(DISTINCT ap_destino.id) AS total_aeropuertos, " +
                        "COUNT(DISTINCT vo.id) AS total_vuelos " +
                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aeropuerto ap_destino ON ap_destino.id = vp.aeropuerto_llegada_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_destino ON sc_destino.id = ap_destino.estado_id " +
                        "JOIN destino_autorizado da_origen " +
                        "ON da_origen.aerolinea_id = v.aerolinea_id " +
                        "AND da_origen.aeropuerto_id = vp.aeropuerto_salida_id " +
                        "JOIN destino_autorizado da_destino " +
                        "ON da_destino.aerolinea_id = v.aerolinea_id " +
                        "AND da_destino.aeropuerto_id = vp.aeropuerto_llegada_id " +
                        "JOIN status_catalog sc_da_origen ON sc_da_origen.id = da_origen.estado_id " +
                        "JOIN status_catalog sc_da_destino ON sc_da_destino.id = da_destino.estado_id " +
                        "WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId " +
                        "AND vp.aeropuerto_llegada_id <> :aeropuertoSalidaId " +
                        "AND UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_destino.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_origen.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_destino.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE " +
                        "AND EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM segmento_operado so " +
                        "    JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    WHERE so.vuelo_operado_id = vo.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") ";

        if (filtrar) {
            sql += filtroUbicacion("ap_destino");
        }

        sql +=
                "GROUP BY ap_destino.pais, ap_destino.ciudad " +
                        "ORDER BY ap_destino.pais, ap_destino.ciudad " +
                        "LIMIT 30";

        return sql;
    }

    private String sqlAeropuertosDestino(boolean filtrarPais, boolean filtrarCiudad, boolean filtrarQ) {
        String sql =
                "SELECT " +
                        "ap_destino.id AS aeropuerto_id, " +
                        "ap_destino.nombre AS nombre, " +
                        "ap_destino.codigo_iata AS codigo_iata, " +
                        "ap_destino.codigo_icao AS codigo_icao, " +
                        "ap_destino.pais AS pais, " +
                        "ap_destino.ciudad AS ciudad, " +
                        "COUNT(DISTINCT vo.id) AS total_vuelos, " +
                        "COUNT(DISTINCT CASE WHEN ea.id IS NOT NULL THEN avu.id END) AS asientos_disponibles_total " +
                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aeropuerto ap_destino ON ap_destino.id = vp.aeropuerto_llegada_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_destino ON sc_destino.id = ap_destino.estado_id " +
                        "JOIN destino_autorizado da_origen " +
                        "ON da_origen.aerolinea_id = v.aerolinea_id " +
                        "AND da_origen.aeropuerto_id = vp.aeropuerto_salida_id " +
                        "JOIN destino_autorizado da_destino " +
                        "ON da_destino.aerolinea_id = v.aerolinea_id " +
                        "AND da_destino.aeropuerto_id = vp.aeropuerto_llegada_id " +
                        "JOIN status_catalog sc_da_origen ON sc_da_origen.id = da_origen.estado_id " +
                        "JOIN status_catalog sc_da_destino ON sc_da_destino.id = da_destino.estado_id " +
                        "LEFT JOIN segmento_operado so ON so.vuelo_operado_id = vo.id " +
                        "LEFT JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "LEFT JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        "WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId " +
                        "AND vp.aeropuerto_llegada_id <> :aeropuertoSalidaId " +
                        "AND UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_destino.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_origen.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_destino.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE ";

        if (filtrarPais) {
            sql += "AND LOWER(ap_destino.pais) = :pais ";
        }

        if (filtrarCiudad) {
            sql += "AND LOWER(ap_destino.ciudad) = :ciudad ";
        }

        if (filtrarQ) {
            sql += filtroUbicacion("ap_destino");
        }

        sql +=
                "GROUP BY " +
                        "ap_destino.id, ap_destino.nombre, ap_destino.codigo_iata, " +
                        "ap_destino.codigo_icao, ap_destino.pais, ap_destino.ciudad " +
                        "HAVING COUNT(DISTINCT CASE WHEN ea.id IS NOT NULL THEN avu.id END) > 0 " +
                        "ORDER BY ap_destino.pais, ap_destino.ciudad, ap_destino.nombre " +
                        "LIMIT 30";

        return sql;
    }

    private String sqlDestinosAutorizadosConVuelos() {
        return
                "SELECT " +
                        "ap_destino.id AS aeropuerto_id, " +
                        "ap_destino.nombre AS nombre, " +
                        "ap_destino.codigo_iata AS codigo_iata, " +
                        "ap_destino.ciudad AS ciudad, " +
                        "ap_destino.pais AS pais " +
                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aeropuerto ap_destino ON ap_destino.id = vp.aeropuerto_llegada_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_destino ON sc_destino.id = ap_destino.estado_id " +
                        "JOIN destino_autorizado da_origen " +
                        "ON da_origen.aerolinea_id = v.aerolinea_id " +
                        "AND da_origen.aeropuerto_id = vp.aeropuerto_salida_id " +
                        "JOIN destino_autorizado da_destino " +
                        "ON da_destino.aerolinea_id = v.aerolinea_id " +
                        "AND da_destino.aeropuerto_id = vp.aeropuerto_llegada_id " +
                        "JOIN status_catalog sc_da_origen ON sc_da_origen.id = da_origen.estado_id " +
                        "JOIN status_catalog sc_da_destino ON sc_da_destino.id = da_destino.estado_id " +
                        "WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId " +
                        "AND vp.aeropuerto_llegada_id <> :aeropuertoSalidaId " +
                        "AND UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_destino.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_origen.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_destino.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE " +
                        "AND EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM segmento_operado so " +
                        "    JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    WHERE so.vuelo_operado_id = vo.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") " +
                        "GROUP BY ap_destino.id, ap_destino.nombre, ap_destino.codigo_iata, ap_destino.ciudad, ap_destino.pais " +
                        "ORDER BY ap_destino.pais, ap_destino.ciudad, ap_destino.nombre";
    }

    private String sqlVuelosDisponibles(boolean filtrarPorFecha) {
        String sql =
                "SELECT " +
                        "vo.id AS vuelo_operado_id, " +
                        "vp.id AS vuelo_programado_id, " +
                        "v.id AS vuelo_id, " +
                        "v.codigo_vuelo AS codigo_vuelo, " +
                        "al.id AS aerolinea_id, " +
                        "al.nombre AS aerolinea_nombre, " +

                        "ap_salida.id AS aeropuerto_salida_id, " +
                        "ap_salida.nombre AS aeropuerto_salida_nombre, " +
                        "ap_salida.codigo_iata AS aeropuerto_salida_codigo_iata, " +
                        "ap_salida.pais AS aeropuerto_salida_pais, " +
                        "ap_salida.ciudad AS aeropuerto_salida_ciudad, " +

                        "ap_llegada.id AS aeropuerto_llegada_id, " +
                        "ap_llegada.nombre AS aeropuerto_llegada_nombre, " +
                        "ap_llegada.codigo_iata AS aeropuerto_llegada_codigo_iata, " +
                        "ap_llegada.pais AS aeropuerto_llegada_pais, " +
                        "ap_llegada.ciudad AS aeropuerto_llegada_ciudad, " +

                        "vp.puerta_embarque_salida AS puerta_embarque_salida, " +
                        "vp.puerta_embarque_llegada AS puerta_embarque_llegada, " +

                        "vp.fecha_salida AS fecha_salida, " +
                        "vp.hora_salida AS hora_salida, " +
                        "vp.fecha_llegada AS fecha_llegada, " +
                        "vp.hora_llegada AS hora_llegada, " +

                        "CAST(EXTRACT(EPOCH FROM ((vp.fecha_llegada + vp.hora_llegada) - (vp.fecha_salida + vp.hora_salida))) / 60 AS BIGINT) AS duracion_minutos, " +

                        "COALESCE(precio_economica.precio, 0) AS precio_economica, " +
                        "COALESCE(precio_ejecutiva.precio, 0) AS precio_ejecutiva, " +

                        "tsv.id AS tipo_segmento_vuelo_id, " +
                        "tsv.nombre AS tipo_segmento_vuelo_nombre, " +
                        "tsv.requiere_nuevo_asiento AS requiere_nuevo_asiento, " +

                        "vo.cantidad_segmentos AS cantidad_segmentos, " +
                        "vo.tuvo_escala AS tuvo_escala, " +

                        "COALESCE(asientos.total, 0) AS asientos_disponibles_total, " +
                        "COALESCE(asientos.economica, 0) AS asientos_disponibles_economica, " +
                        "COALESCE(asientos.ejecutiva, 0) AS asientos_disponibles_ejecutiva " +

                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aerolinea al ON al.id = v.aerolinea_id " +
                        "JOIN aeropuerto ap_salida ON ap_salida.id = vp.aeropuerto_salida_id " +
                        "JOIN aeropuerto ap_llegada ON ap_llegada.id = vp.aeropuerto_llegada_id " +
                        "JOIN tipo_segmento_vuelo tsv ON tsv.id = vo.tipo_segmento_vuelo_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_salida ON sc_salida.id = ap_salida.estado_id " +
                        "JOIN status_catalog sc_llegada ON sc_llegada.id = ap_llegada.estado_id " +
                        "JOIN destino_autorizado da_origen ON da_origen.aerolinea_id = v.aerolinea_id AND da_origen.aeropuerto_id = vp.aeropuerto_salida_id " +
                        "JOIN destino_autorizado da_destino ON da_destino.aerolinea_id = v.aerolinea_id AND da_destino.aeropuerto_id = vp.aeropuerto_llegada_id " +
                        "JOIN status_catalog sc_da_origen ON sc_da_origen.id = da_origen.estado_id " +
                        "JOIN status_catalog sc_da_destino ON sc_da_destino.id = da_destino.estado_id " +

                        "LEFT JOIN LATERAL ( " +
                        "    SELECT pv.precio " +
                        "    FROM precio_vuelo pv " +
                        "    JOIN clase_vuelo cv ON cv.id = pv.clase_vuelo_id " +
                        "    WHERE pv.vuelo_programado_id = vp.id " +
                        "    AND UPPER(cv.nombre) = 'ECONOMICA' " +
                        "    AND (pv.fecha_vigencia_desde IS NULL OR pv.fecha_vigencia_desde <= CURRENT_DATE) " +
                        "    AND (pv.fecha_vigencia_hasta IS NULL OR pv.fecha_vigencia_hasta >= CURRENT_DATE) " +
                        "    ORDER BY pv.fecha_vigencia_desde DESC NULLS LAST, pv.id DESC " +
                        "    LIMIT 1 " +
                        ") precio_economica ON TRUE " +

                        "LEFT JOIN LATERAL ( " +
                        "    SELECT pv.precio " +
                        "    FROM precio_vuelo pv " +
                        "    JOIN clase_vuelo cv ON cv.id = pv.clase_vuelo_id " +
                        "    WHERE pv.vuelo_programado_id = vp.id " +
                        "    AND UPPER(cv.nombre) = 'EJECUTIVA' " +
                        "    AND (pv.fecha_vigencia_desde IS NULL OR pv.fecha_vigencia_desde <= CURRENT_DATE) " +
                        "    AND (pv.fecha_vigencia_hasta IS NULL OR pv.fecha_vigencia_hasta >= CURRENT_DATE) " +
                        "    ORDER BY pv.fecha_vigencia_desde DESC NULLS LAST, pv.id DESC " +
                        "    LIMIT 1 " +
                        ") precio_ejecutiva ON TRUE " +

                        "LEFT JOIN LATERAL ( " +
                        "    SELECT " +
                        "    COUNT(*) AS total, " +
                        "    COUNT(*) FILTER (WHERE UPPER(cv.nombre) = 'ECONOMICA') AS economica, " +
                        "    COUNT(*) FILTER (WHERE UPPER(cv.nombre) = 'EJECUTIVA') AS ejecutiva " +
                        "    FROM segmento_operado so " +
                        "    JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    LEFT JOIN asiento_ubi au ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema " +
                        "    LEFT JOIN clase_vuelo cv ON cv.id = au.clase_vuelo_id " +
                        "    WHERE so.vuelo_operado_id = vo.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") asientos ON TRUE " +

                        "WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId " +
                        "AND vp.aeropuerto_llegada_id = :aeropuertoLlegadaId " +
                        "AND vp.aeropuerto_salida_id <> vp.aeropuerto_llegada_id " +
                        "AND UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_salida.name) = 'ACTIVO' " +
                        "AND UPPER(sc_llegada.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_origen.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_destino.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE " +
                        "AND COALESCE(asientos.total, 0) > 0 ";

        if (filtrarPorFecha) {
            sql += "AND vp.fecha_salida = :fechaSalida ";
        }

        sql += "ORDER BY vp.fecha_salida, vp.hora_salida, v.codigo_vuelo";

        return sql;
    }

    private String sqlDetalleVueloDisponible() {
        return
                "SELECT " +
                        "vo.id AS vuelo_operado_id, " +
                        "vp.id AS vuelo_programado_id, " +
                        "v.id AS vuelo_id, " +
                        "v.codigo_vuelo AS codigo_vuelo, " +
                        "al.id AS aerolinea_id, " +
                        "al.nombre AS aerolinea_nombre, " +

                        "ap_salida.id AS aeropuerto_salida_id, " +
                        "ap_salida.nombre AS aeropuerto_salida_nombre, " +
                        "ap_salida.codigo_iata AS aeropuerto_salida_codigo_iata, " +
                        "ap_salida.pais AS aeropuerto_salida_pais, " +
                        "ap_salida.ciudad AS aeropuerto_salida_ciudad, " +

                        "ap_llegada.id AS aeropuerto_llegada_id, " +
                        "ap_llegada.nombre AS aeropuerto_llegada_nombre, " +
                        "ap_llegada.codigo_iata AS aeropuerto_llegada_codigo_iata, " +
                        "ap_llegada.pais AS aeropuerto_llegada_pais, " +
                        "ap_llegada.ciudad AS aeropuerto_llegada_ciudad, " +

                        "vp.puerta_embarque_salida AS puerta_embarque_salida, " +
                        "vp.puerta_embarque_llegada AS puerta_embarque_llegada, " +

                        "vp.fecha_salida AS fecha_salida, " +
                        "vp.hora_salida AS hora_salida, " +
                        "vp.fecha_llegada AS fecha_llegada, " +
                        "vp.hora_llegada AS hora_llegada, " +

                        "CAST(EXTRACT(EPOCH FROM ((vp.fecha_llegada + vp.hora_llegada) - (vp.fecha_salida + vp.hora_salida))) / 60 AS BIGINT) AS duracion_minutos, " +

                        "COALESCE(precio_economica.precio, 0) AS precio_economica, " +
                        "COALESCE(precio_ejecutiva.precio, 0) AS precio_ejecutiva, " +

                        "tsv.id AS tipo_segmento_vuelo_id, " +
                        "tsv.nombre AS tipo_segmento_vuelo_nombre, " +
                        "tsv.requiere_nuevo_asiento AS requiere_nuevo_asiento, " +

                        "vo.cantidad_segmentos AS cantidad_segmentos, " +
                        "vo.tuvo_escala AS tuvo_escala, " +

                        "COALESCE(asientos.total, 0) AS asientos_disponibles_total, " +
                        "COALESCE(asientos.economica, 0) AS asientos_disponibles_economica, " +
                        "COALESCE(asientos.ejecutiva, 0) AS asientos_disponibles_ejecutiva " +

                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN aerolinea al ON al.id = v.aerolinea_id " +
                        "JOIN aeropuerto ap_salida ON ap_salida.id = vp.aeropuerto_salida_id " +
                        "JOIN aeropuerto ap_llegada ON ap_llegada.id = vp.aeropuerto_llegada_id " +
                        "JOIN tipo_segmento_vuelo tsv ON tsv.id = vo.tipo_segmento_vuelo_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN status_catalog sc_salida ON sc_salida.id = ap_salida.estado_id " +
                        "JOIN status_catalog sc_llegada ON sc_llegada.id = ap_llegada.estado_id " +

                        "LEFT JOIN LATERAL ( " +
                        "    SELECT pv.precio " +
                        "    FROM precio_vuelo pv " +
                        "    JOIN clase_vuelo cv ON cv.id = pv.clase_vuelo_id " +
                        "    WHERE pv.vuelo_programado_id = vp.id " +
                        "    AND UPPER(cv.nombre) = 'ECONOMICA' " +
                        "    AND (pv.fecha_vigencia_desde IS NULL OR pv.fecha_vigencia_desde <= CURRENT_DATE) " +
                        "    AND (pv.fecha_vigencia_hasta IS NULL OR pv.fecha_vigencia_hasta >= CURRENT_DATE) " +
                        "    ORDER BY pv.fecha_vigencia_desde DESC NULLS LAST, pv.id DESC " +
                        "    LIMIT 1 " +
                        ") precio_economica ON TRUE " +

                        "LEFT JOIN LATERAL ( " +
                        "    SELECT pv.precio " +
                        "    FROM precio_vuelo pv " +
                        "    JOIN clase_vuelo cv ON cv.id = pv.clase_vuelo_id " +
                        "    WHERE pv.vuelo_programado_id = vp.id " +
                        "    AND UPPER(cv.nombre) = 'EJECUTIVA' " +
                        "    AND (pv.fecha_vigencia_desde IS NULL OR pv.fecha_vigencia_desde <= CURRENT_DATE) " +
                        "    AND (pv.fecha_vigencia_hasta IS NULL OR pv.fecha_vigencia_hasta >= CURRENT_DATE) " +
                        "    ORDER BY pv.fecha_vigencia_desde DESC NULLS LAST, pv.id DESC " +
                        "    LIMIT 1 " +
                        ") precio_ejecutiva ON TRUE " +

                        "LEFT JOIN LATERAL ( " +
                        "    SELECT " +
                        "    COUNT(*) AS total, " +
                        "    COUNT(*) FILTER (WHERE UPPER(cv.nombre) = 'ECONOMICA') AS economica, " +
                        "    COUNT(*) FILTER (WHERE UPPER(cv.nombre) = 'EJECUTIVA') AS ejecutiva " +
                        "    FROM segmento_operado so " +
                        "    JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    LEFT JOIN asiento_ubi au ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema " +
                        "    LEFT JOIN clase_vuelo cv ON cv.id = au.clase_vuelo_id " +
                        "    WHERE so.vuelo_operado_id = vo.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") asientos ON TRUE " +

                        "WHERE vo.id = :vueloOperadoId " +
                        "AND UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_salida.name) = 'ACTIVO' " +
                        "AND UPPER(sc_llegada.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO')";
    }

    private String sqlFechasDisponibles(boolean regreso) {
        String sql =
                "SELECT " +
                        "vp.fecha_salida AS fecha_salida, " +
                        "COUNT(DISTINCT vo.id) AS vuelos_disponibles, " +
                        "MIN(COALESCE(precio_min.precio, 0)) AS precio_minimo " +
                        "FROM vuelo_operado vo " +
                        "JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id " +
                        "JOIN vuelo v ON v.id = vp.vuelo_id " +
                        "JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id " +
                        "JOIN status_catalog sc_v ON sc_v.id = v.estado_id " +
                        "JOIN destino_autorizado da_origen ON da_origen.aerolinea_id = v.aerolinea_id AND da_origen.aeropuerto_id = vp.aeropuerto_salida_id " +
                        "JOIN destino_autorizado da_destino ON da_destino.aerolinea_id = v.aerolinea_id AND da_destino.aeropuerto_id = vp.aeropuerto_llegada_id " +
                        "JOIN status_catalog sc_da_origen ON sc_da_origen.id = da_origen.estado_id " +
                        "JOIN status_catalog sc_da_destino ON sc_da_destino.id = da_destino.estado_id " +
                        "LEFT JOIN LATERAL ( " +
                        "    SELECT MIN(pv.precio) AS precio " +
                        "    FROM precio_vuelo pv " +
                        "    WHERE pv.vuelo_programado_id = vp.id " +
                        "    AND (pv.fecha_vigencia_desde IS NULL OR pv.fecha_vigencia_desde <= CURRENT_DATE) " +
                        "    AND (pv.fecha_vigencia_hasta IS NULL OR pv.fecha_vigencia_hasta >= CURRENT_DATE) " +
                        ") precio_min ON TRUE " +
                        "WHERE vp.aeropuerto_salida_id = :aeropuertoSalidaId " +
                        "AND vp.aeropuerto_llegada_id = :aeropuertoLlegadaId " +
                        "AND vp.aeropuerto_salida_id <> vp.aeropuerto_llegada_id " +
                        "AND UPPER(sc_v.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_origen.name) = 'ACTIVO' " +
                        "AND UPPER(sc_da_destino.name) = 'ACTIVO' " +
                        "AND UPPER(ev.nombre) IN ('PROGRAMADO', 'ATRASADO') " +
                        "AND vp.fecha_salida >= CURRENT_DATE " +
                        "AND EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM segmento_operado so " +
                        "    JOIN asiento_vuelo avu ON avu.segmento_operado_id = so.id " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    WHERE so.vuelo_operado_id = vo.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") ";

        if (regreso) {
            sql += "AND vp.fecha_salida >= :fechaSalida ";
        }

        sql +=
                "GROUP BY vp.fecha_salida " +
                        "ORDER BY vp.fecha_salida";

        return sql;
    }

    private String sqlSegmentosVueloOperado() {
        return
                "SELECT " +
                        "so.id AS segmento_operado_id, " +
                        "sv.id AS segmento_vuelo_id, " +
                        "so.orden_segmento AS orden_segmento, " +

                        "ap_salida.id AS aeropuerto_salida_id, " +
                        "ap_salida.nombre AS aeropuerto_salida_nombre, " +
                        "ap_salida.codigo_iata AS aeropuerto_salida_codigo_iata, " +
                        "ap_salida.pais AS aeropuerto_salida_pais, " +
                        "ap_salida.ciudad AS aeropuerto_salida_ciudad, " +

                        "ap_llegada.id AS aeropuerto_llegada_id, " +
                        "ap_llegada.nombre AS aeropuerto_llegada_nombre, " +
                        "ap_llegada.codigo_iata AS aeropuerto_llegada_codigo_iata, " +
                        "ap_llegada.pais AS aeropuerto_llegada_pais, " +
                        "ap_llegada.ciudad AS aeropuerto_llegada_ciudad, " +

                        "sv.fecha_salida AS fecha_salida, " +
                        "sv.hora_salida AS hora_salida, " +
                        "sv.fecha_llegada AS fecha_llegada, " +
                        "sv.hora_llegada AS hora_llegada, " +

                        "av.id AS avion_id, " +
                        "av.codigo_avion AS codigo_avion, " +

                        "COALESCE(asientos.total, 0) AS asientos_disponibles_total, " +
                        "COALESCE(asientos.economica, 0) AS asientos_disponibles_economica, " +
                        "COALESCE(asientos.ejecutiva, 0) AS asientos_disponibles_ejecutiva " +

                        "FROM segmento_operado so " +
                        "JOIN segmento_vuelo sv ON sv.id = so.segmento_vuelo_id " +
                        "JOIN aeropuerto ap_salida ON ap_salida.id = sv.aeropuerto_salida_id " +
                        "JOIN aeropuerto ap_llegada ON ap_llegada.id = sv.aeropuerto_llegada_id " +
                        "JOIN avion av ON av.id = so.avion_id " +
                        "LEFT JOIN LATERAL ( " +
                        "    SELECT " +
                        "    COUNT(*) AS total, " +
                        "    COUNT(*) FILTER (WHERE UPPER(cv.nombre) = 'ECONOMICA') AS economica, " +
                        "    COUNT(*) FILTER (WHERE UPPER(cv.nombre) = 'EJECUTIVA') AS ejecutiva " +
                        "    FROM asiento_vuelo avu " +
                        "    JOIN estado_asiento ea ON ea.id = avu.estado_asiento_id " +
                        "    LEFT JOIN asiento_ubi au ON au.codigo_asiento_sistema = avu.codigo_asiento_sistema " +
                        "    LEFT JOIN clase_vuelo cv ON cv.id = au.clase_vuelo_id " +
                        "    WHERE avu.segmento_operado_id = so.id " +
                        "    AND UPPER(ea.nombre) = 'DISPONIBLE' " +
                        ") asientos ON TRUE " +
                        "WHERE so.vuelo_operado_id = :vueloOperadoId " +
                        "ORDER BY so.orden_segmento";
    }

    private String filtroUbicacion(String alias) {
        return
                "AND ( " +
                        "LOWER(" + alias + ".pais) LIKE :q " +
                        "OR LOWER(" + alias + ".ciudad) LIKE :q " +
                        "OR LOWER(" + alias + ".nombre) LIKE :q " +
                        "OR LOWER(COALESCE(" + alias + ".codigo_iata, '')) LIKE :q " +
                        "OR LOWER(COALESCE(" + alias + ".codigo_icao, '')) LIKE :q " +
                        ") ";
    }

    private RowMapper<ClienteUbicacionDisponibleResponse> ubicacionMapper() {
        return new RowMapper<ClienteUbicacionDisponibleResponse>() {
            @Override
            public ClienteUbicacionDisponibleResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                ClienteUbicacionDisponibleResponse response = new ClienteUbicacionDisponibleResponse();

                response.setPais(rs.getString("pais"));
                response.setCiudad(rs.getString("ciudad"));
                response.setTotalAeropuertos(getInteger(rs, "total_aeropuertos"));
                response.setTotalVuelos(getInteger(rs, "total_vuelos"));

                return response;
            }
        };
    }

    private RowMapper<ClienteAeropuertoDisponibleResponse> aeropuertoDisponibleMapper() {
        return new RowMapper<ClienteAeropuertoDisponibleResponse>() {
            @Override
            public ClienteAeropuertoDisponibleResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                ClienteAeropuertoDisponibleResponse response = new ClienteAeropuertoDisponibleResponse();

                response.setAeropuertoId(getInteger(rs, "aeropuerto_id"));
                response.setNombre(rs.getString("nombre"));
                response.setCodigoIata(rs.getString("codigo_iata"));
                response.setCodigoIcao(rs.getString("codigo_icao"));
                response.setPais(rs.getString("pais"));
                response.setCiudad(rs.getString("ciudad"));
                response.setTotalVuelos(getInteger(rs, "total_vuelos"));
                response.setAsientosDisponiblesTotal(getInteger(rs, "asientos_disponibles_total"));

                return response;
            }
        };
    }

    private RowMapper<ClienteDestinoAutorizadoResponse> destinoAutorizadoMapper() {
        return new RowMapper<ClienteDestinoAutorizadoResponse>() {
            @Override
            public ClienteDestinoAutorizadoResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                ClienteDestinoAutorizadoResponse response = new ClienteDestinoAutorizadoResponse();

                response.setAeropuertoId(getInteger(rs, "aeropuerto_id"));
                response.setNombre(rs.getString("nombre"));
                response.setCodigoIata(rs.getString("codigo_iata"));
                response.setCiudad(rs.getString("ciudad"));
                response.setPais(rs.getString("pais"));

                return response;
            }
        };
    }

    private RowMapper<ClienteFechaDisponibleResponse> fechaDisponibleMapper() {
        return new RowMapper<ClienteFechaDisponibleResponse>() {
            @Override
            public ClienteFechaDisponibleResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                ClienteFechaDisponibleResponse response = new ClienteFechaDisponibleResponse();

                Date fecha = rs.getDate("fecha_salida");

                response.setFechaSalida(fecha != null ? fecha.toLocalDate() : null);
                response.setVuelosDisponibles(getLong(rs, "vuelos_disponibles"));
                response.setPrecioMinimo(rs.getBigDecimal("precio_minimo"));

                return response;
            }
        };
    }

    private RowMapper<ClienteVueloDisponibleResponse> vueloMapper() {
        return new RowMapper<ClienteVueloDisponibleResponse>() {
            @Override
            public ClienteVueloDisponibleResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
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
                response.setAeropuertoSalidaPais(rs.getString("aeropuerto_salida_pais"));
                response.setAeropuertoSalidaCiudad(rs.getString("aeropuerto_salida_ciudad"));

                response.setAeropuertoLlegadaId(getInteger(rs, "aeropuerto_llegada_id"));
                response.setAeropuertoLlegadaNombre(rs.getString("aeropuerto_llegada_nombre"));
                response.setAeropuertoLlegadaCodigoIata(rs.getString("aeropuerto_llegada_codigo_iata"));
                response.setAeropuertoLlegadaPais(rs.getString("aeropuerto_llegada_pais"));
                response.setAeropuertoLlegadaCiudad(rs.getString("aeropuerto_llegada_ciudad"));

                response.setPuertaEmbarqueSalida(rs.getString("puerta_embarque_salida"));
                response.setPuertaEmbarqueLlegada(rs.getString("puerta_embarque_llegada"));

                response.setFechaSalida(getLocalDate(rs, "fecha_salida"));
                response.setHoraSalida(getLocalTime(rs, "hora_salida"));
                response.setFechaLlegada(getLocalDate(rs, "fecha_llegada"));
                response.setHoraLlegada(getLocalTime(rs, "hora_llegada"));

                response.setDuracionMinutos(getLong(rs, "duracion_minutos"));

                response.setPrecioEconomica(getBigDecimal(rs, "precio_economica"));
                response.setPrecioEjecutiva(getBigDecimal(rs, "precio_ejecutiva"));

                response.setTipoSegmentoVueloId(getInteger(rs, "tipo_segmento_vuelo_id"));
                response.setTipoSegmentoVueloNombre(rs.getString("tipo_segmento_vuelo_nombre"));
                response.setRequiereNuevoAsiento(rs.getBoolean("requiere_nuevo_asiento"));

                response.setCantidadSegmentos(getInteger(rs, "cantidad_segmentos"));
                response.setTuvoEscala(rs.getBoolean("tuvo_escala"));

                response.setAsientosDisponiblesTotal(getInteger(rs, "asientos_disponibles_total"));
                response.setAsientosDisponiblesEconomica(getInteger(rs, "asientos_disponibles_economica"));
                response.setAsientosDisponiblesEjecutiva(getInteger(rs, "asientos_disponibles_ejecutiva"));

                return response;
            }
        };
    }

    private RowMapper<ClienteVueloSegmentoDisponibleResponse> segmentoMapper() {
        return new RowMapper<ClienteVueloSegmentoDisponibleResponse>() {
            @Override
            public ClienteVueloSegmentoDisponibleResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                ClienteVueloSegmentoDisponibleResponse response = new ClienteVueloSegmentoDisponibleResponse();

                response.setSegmentoOperadoId(getInteger(rs, "segmento_operado_id"));
                response.setSegmentoVueloId(getInteger(rs, "segmento_vuelo_id"));
                response.setOrdenSegmento(getInteger(rs, "orden_segmento"));

                response.setAeropuertoSalidaId(getInteger(rs, "aeropuerto_salida_id"));
                response.setAeropuertoSalidaNombre(rs.getString("aeropuerto_salida_nombre"));
                response.setAeropuertoSalidaCodigoIata(rs.getString("aeropuerto_salida_codigo_iata"));
                response.setAeropuertoSalidaPais(rs.getString("aeropuerto_salida_pais"));
                response.setAeropuertoSalidaCiudad(rs.getString("aeropuerto_salida_ciudad"));

                response.setAeropuertoLlegadaId(getInteger(rs, "aeropuerto_llegada_id"));
                response.setAeropuertoLlegadaNombre(rs.getString("aeropuerto_llegada_nombre"));
                response.setAeropuertoLlegadaCodigoIata(rs.getString("aeropuerto_llegada_codigo_iata"));
                response.setAeropuertoLlegadaPais(rs.getString("aeropuerto_llegada_pais"));
                response.setAeropuertoLlegadaCiudad(rs.getString("aeropuerto_llegada_ciudad"));

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
            }
        };
    }

    private Integer getInteger(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return Integer.valueOf(value.toString());
    }

    private Long getLong(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return Long.valueOf(value.toString());
    }

    private BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value != null ? value : BigDecimal.ZERO;
    }

    private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date != null ? date.toLocalDate() : null;
    }

    private LocalTime getLocalTime(ResultSet rs, String column) throws SQLException {
        Time time = rs.getTime(column);
        return time != null ? time.toLocalTime() : null;
    }

    private boolean tieneTexto(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + normalizar(value) + "%";
    }

    private String normalizar(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}