package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final JdbcTemplate jdbc;

    @Override
    public Map<String, Object> consultaVuelo(String codigoVuelo) {

        if (codigoVuelo == null || codigoVuelo.isBlank()) {
            throw new BusinessException("Debe ingresar el número de vuelo");
        }

        String sql = """
                SELECT
                    vo.id AS "vueloOperadoId",
                    v.codigo_vuelo AS "codigoVuelo",
                    ma.nombre AS "modeloAvion",
                    ma.fabricante AS "marcaAvion",
                    a.nombre AS "aerolinea",
                    origen.nombre AS "origen",
                    origen.ciudad AS "ciudadOrigen",
                    origen.pais AS "paisOrigen",
                    destino.nombre AS "destino",
                    destino.ciudad AS "ciudadDestino",
                    destino.pais AS "paisDestino",
                    COALESCE(sv.fecha_salida, vp.fecha_salida) AS "fechaSalida",
                    COALESCE(sv.hora_salida, vp.hora_salida) AS "horaSalida",
                    COALESCE(sv.fecha_llegada, vp.fecha_llegada) AS "fechaLlegada",
                    COALESCE(sv.hora_llegada, vp.hora_llegada) AS "horaLlegada",
                    ev.nombre AS "estadoVuelo"
                FROM vuelo_operado vo
                JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                JOIN vuelo v ON v.id = vp.vuelo_id
                JOIN aerolinea a ON a.id = v.aerolinea_id
                LEFT JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id
                LEFT JOIN segmento_operado so ON so.vuelo_operado_id = vo.id AND so.orden_segmento = 1
                LEFT JOIN segmento_vuelo sv ON sv.id = so.segmento_vuelo_id
                LEFT JOIN avion av ON av.id = so.avion_id
                LEFT JOIN modelo_avion ma ON ma.id = av.modelo_avion_id
                LEFT JOIN aeropuerto origen ON origen.id = COALESCE(sv.aeropuerto_salida_id, vp.aeropuerto_salida_id)
                LEFT JOIN aeropuerto destino ON destino.id = COALESCE(sv.aeropuerto_llegada_id, vp.aeropuerto_llegada_id)
                WHERE UPPER(v.codigo_vuelo) = UPPER(?)
                ORDER BY vo.id DESC
                LIMIT 1
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, codigoVuelo.trim());

        if (rows.isEmpty()) {
            throw new BusinessException("El número de vuelo ingresado no se encontró");
        }

        return rows.get(0);
    }

    @Override
    public List<Map<String, Object>> vuelosPorFechaHora(
            LocalDate fechaDesde,
            LocalTime horaDesde,
            LocalDate fechaHasta,
            LocalTime horaHasta
    ) {

        validarRangoFechas(fechaDesde, horaDesde, fechaHasta, horaHasta);

        StringBuilder sql = new StringBuilder("""
                SELECT
                    vo.id AS "vueloOperadoId",
                    v.codigo_vuelo AS "codigoVuelo",
                    ma.nombre AS "modeloAvion",
                    ma.fabricante AS "marcaAvion",
                    a.nombre AS "aerolinea",
                    origen.nombre AS "origen",
                    destino.nombre AS "destino",
                    COALESCE(sv.fecha_salida, vp.fecha_salida) AS "fechaSalida",
                    COALESCE(sv.hora_salida, vp.hora_salida) AS "horaSalida",
                    COALESCE(sv.fecha_llegada, vp.fecha_llegada) AS "fechaLlegada",
                    COALESCE(sv.hora_llegada, vp.hora_llegada) AS "horaLlegada",
                    ev.nombre AS "estadoVuelo"
                FROM vuelo_operado vo
                JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                JOIN vuelo v ON v.id = vp.vuelo_id
                JOIN aerolinea a ON a.id = v.aerolinea_id
                LEFT JOIN estado_vuelo ev ON ev.id = vo.estado_vuelo_id
                LEFT JOIN segmento_operado so ON so.vuelo_operado_id = vo.id AND so.orden_segmento = 1
                LEFT JOIN segmento_vuelo sv ON sv.id = so.segmento_vuelo_id
                LEFT JOIN avion av ON av.id = so.avion_id
                LEFT JOIN modelo_avion ma ON ma.id = av.modelo_avion_id
                LEFT JOIN aeropuerto origen ON origen.id = COALESCE(sv.aeropuerto_salida_id, vp.aeropuerto_salida_id)
                LEFT JOIN aeropuerto destino ON destino.id = COALESCE(sv.aeropuerto_llegada_id, vp.aeropuerto_llegada_id)
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (fechaDesde != null) {
            sql.append("""
                    AND (
                        (COALESCE(sv.fecha_salida, vp.fecha_salida) > ? 
                            OR (
                                COALESCE(sv.fecha_salida, vp.fecha_salida) = ? 
                                AND COALESCE(sv.hora_salida, vp.hora_salida) >= ?
                            )
                        )
                        AND
                        (COALESCE(sv.fecha_salida, vp.fecha_salida) < ? 
                            OR (
                                COALESCE(sv.fecha_salida, vp.fecha_salida) = ? 
                                AND COALESCE(sv.hora_salida, vp.hora_salida) <= ?
                            )
                        )
                    )
                    """);

            params.add(fechaDesde);
            params.add(fechaDesde);
            params.add(horaDesde);
            params.add(fechaHasta);
            params.add(fechaHasta);
            params.add(horaHasta);
        }

        sql.append("""
                ORDER BY 
                    COALESCE(sv.fecha_salida, vp.fecha_salida) ASC,
                    COALESCE(sv.hora_salida, vp.hora_salida) ASC,
                    v.codigo_vuelo ASC
                """);

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> pasajerosPorVuelo(String codigoVuelo) {

        if (codigoVuelo == null || codigoVuelo.isBlank()) {
            throw new BusinessException("Debe ingresar el número de vuelo");
        }

        validarExisteVuelo(codigoVuelo);

        String sql = """
                SELECT
                    vo.id AS "vueloOperadoId",
                    v.codigo_vuelo AS "codigoVuelo",
                    b.id AS "boletoId",
                    b.codigo_boleto AS "codigoBoleto",
                    b.codigo_pase_abordar AS "codigoPaseAbordar",
                    p.id AS "pasajeroId",
                    p.nombre_completo AS "nombrePasajero",
                    p.pasaporte AS "numeroPasaporte",
                    p.nacionalidad AS "nacionalidad",
                    EXTRACT(YEAR FROM AGE(CURRENT_DATE, p.fecha_nacimiento))::INT AS "edad",
                    p.telefono AS "telefono",
                    COALESCE(u.email, '-') AS "correoElectronico",
                    eb.nombre AS "estadoBoleto"
                FROM boleto b
                JOIN pasajero p ON p.id = b.pasajero_id
                LEFT JOIN users u ON u.id = p.user_id
                JOIN vuelo_operado vo ON vo.id = b.vuelo_operado_id
                JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                JOIN vuelo v ON v.id = vp.vuelo_id
                LEFT JOIN estado_boleto eb ON eb.id = b.estado_boleto_id
                WHERE UPPER(v.codigo_vuelo) = UPPER(?)
                  AND COALESCE(b.estado_id, 1) = 1
                ORDER BY p.nombre_completo ASC
                """;

        return jdbc.queryForList(sql, codigoVuelo.trim());
    }

    @Override
    public List<Map<String, Object>> equipajePorVuelo(String codigoVuelo) {

        if (codigoVuelo == null || codigoVuelo.isBlank()) {
            throw new BusinessException("Debe ingresar el número de vuelo");
        }

        validarExisteVuelo(codigoVuelo);

        String sql = """
                SELECT
                    vo.id AS "vueloOperadoId",
                    v.codigo_vuelo AS "codigoVuelo",
                    p.id AS "pasajeroId",
                    p.nombre_completo AS "nombrePasajero",
                    p.pasaporte AS "numeroPasaporte",
                    b.id AS "boletoId",
                    b.codigo_boleto AS "codigoBoleto",
                    e.id AS "equipajeId",
                    e.numero_maleta AS "maleta",
                    e.monto_recargo AS "recargo",
                    ee.nombre AS "estadoEquipaje"
                FROM equipaje e
                JOIN boleto b ON b.id = e.boleto_id
                JOIN pasajero p ON p.id = e.pasajero_id
                JOIN vuelo_operado vo ON vo.id = b.vuelo_operado_id
                JOIN vuelo_programado vp ON vp.id = vo.vuelo_programado_id
                JOIN vuelo v ON v.id = vp.vuelo_id
                LEFT JOIN estado_equipaje ee ON ee.id = e.estado_equipaje_id
                WHERE UPPER(v.codigo_vuelo) = UPPER(?)
                ORDER BY p.nombre_completo ASC, e.numero_maleta ASC
                """;

        return jdbc.queryForList(sql, codigoVuelo.trim());
    }

    @Override
    public List<Map<String, Object>> avionesPorAerolinea(Integer aerolineaId) {

        if (aerolineaId == null) {
            throw new BusinessException("Debe ingresar la aerolínea");
        }

        String sql = """
                SELECT
                    av.id AS "avionId",
                    av.codigo_avion AS "codigoAvion",
                    ma.nombre AS "modeloAvion",
                    ma.fabricante AS "marca",
                    av.anio AS "anio",
                    av.filas_configuradas AS "cantidadPasajeros",
                    COALESCE(av.cantidad_vuelos, COUNT(DISTINCT so.vuelo_operado_id)) AS "cantidadVuelos"
                FROM avion av
                JOIN modelo_avion ma ON ma.id = av.modelo_avion_id
                LEFT JOIN segmento_operado so ON so.avion_id = av.id
                WHERE av.aerolinea_id = ?
                  AND COALESCE(av.estado_id, 1) = 1
                GROUP BY
                    av.id,
                    av.codigo_avion,
                    ma.nombre,
                    ma.fabricante,
                    av.anio,
                    av.filas_configuradas,
                    av.cantidad_vuelos
                ORDER BY av.codigo_avion ASC
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, aerolineaId);

        if (rows.isEmpty()) {
            throw new BusinessException("La aerolínea consultada no tiene aviones");
        }

        return rows;
    }

    @Override
    public List<Map<String, Object>> aerolineasPorAeropuerto(Integer aeropuertoId) {

        if (aeropuertoId == null) {
            throw new BusinessException("Debe ingresar el aeropuerto");
        }

        String sql = """
                SELECT
                    a.id AS "aerolineaId",
                    a.nombre AS "nombreAerolinea",
                    COUNT(DISTINCT av.id) AS "cantidadAviones",
                    COUNT(DISTINCT da2.id) AS "destinosAutorizados"
                FROM destino_autorizado da
                JOIN aerolinea a ON a.id = da.aerolinea_id
                LEFT JOIN avion av ON av.aerolinea_id = a.id AND COALESCE(av.estado_id, 1) = 1
                LEFT JOIN destino_autorizado da2 ON da2.aerolinea_id = a.id AND COALESCE(da2.estado_id, 1) = 1
                WHERE da.aeropuerto_id = ?
                  AND COALESCE(da.estado_id, 1) = 1
                GROUP BY a.id, a.nombre
                ORDER BY a.nombre ASC
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, aeropuertoId);

        if (rows.isEmpty()) {
            throw new BusinessException("El aeropuerto consultado no tiene aerolíneas");
        }

        return rows;
    }

    @Override
    public List<Map<String, Object>> destinosPorAerolinea(Integer aerolineaId) {

        if (aerolineaId == null) {
            throw new BusinessException("Debe ingresar la aerolínea");
        }

        String sql = """
                SELECT
                    da.id AS "destinoAutorizadoId",
                    a.id AS "aerolineaId",
                    a.nombre AS "aerolinea",
                    ap.id AS "aeropuertoId",
                    ap.nombre AS "nombreAeropuerto",
                    ap.pais AS "paisAeropuerto",
                    ap.ciudad AS "ciudadAeropuerto",
                    da.fecha_autorizacion AS "fechaAutorizacion"
                FROM destino_autorizado da
                JOIN aerolinea a ON a.id = da.aerolinea_id
                JOIN aeropuerto ap ON ap.id = da.aeropuerto_id
                WHERE da.aerolinea_id = ?
                  AND COALESCE(da.estado_id, 1) = 1
                ORDER BY ap.pais ASC, ap.ciudad ASC, ap.nombre ASC
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(sql, aerolineaId);

        if (rows.isEmpty()) {
            throw new BusinessException("La aerolínea consultada no tiene destinos autorizados");
        }

        return rows;
    }

    private void validarExisteVuelo(String codigoVuelo) {

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM vuelo v
                WHERE UPPER(v.codigo_vuelo) = UPPER(?)
                """,
                Integer.class,
                codigoVuelo.trim()
        );

        if (count == null || count == 0) {
            throw new BusinessException("El número de vuelo ingresado no existe.");
        }
    }

    private void validarRangoFechas(
            LocalDate fechaDesde,
            LocalTime horaDesde,
            LocalDate fechaHasta,
            LocalTime horaHasta
    ) {

        boolean alguno = fechaDesde != null ||
                horaDesde != null ||
                fechaHasta != null ||
                horaHasta != null;

        boolean todos = fechaDesde != null &&
                horaDesde != null &&
                fechaHasta != null &&
                horaHasta != null;

        if (alguno && !todos) {
            throw new BusinessException("Si selecciona fecha desde, debe seleccionar hora desde, fecha hasta y hora hasta");
        }

        if (!todos) {
            return;
        }

        LocalDateTime desde = LocalDateTime.of(fechaDesde, horaDesde);
        LocalDateTime hasta = LocalDateTime.of(fechaHasta, horaHasta);

        if (hasta.isBefore(desde)) {
            throw new BusinessException("La fecha y hora hasta debe ser mayor a la fecha y hora desde");
        }

        long dias = ChronoUnit.DAYS.between(fechaDesde, fechaHasta);

        if (dias > 30) {
            throw new BusinessException("El rango máximo de consulta es de 30 días");
        }
    }
}