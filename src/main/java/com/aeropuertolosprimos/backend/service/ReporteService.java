package com.aeropuertolosprimos.backend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface ReporteService {

    Map<String, Object> consultaVuelo(String codigoVuelo);

    List<Map<String, Object>> vuelosPorFechaHora(
            LocalDate fechaDesde,
            LocalTime horaDesde,
            LocalDate fechaHasta,
            LocalTime horaHasta
    );

    List<Map<String, Object>> pasajerosPorVuelo(String codigoVuelo);

    List<Map<String, Object>> equipajePorVuelo(String codigoVuelo);

    List<Map<String, Object>> avionesPorAerolinea(Integer aerolineaId);

    List<Map<String, Object>> aerolineasPorAeropuerto(Integer aeropuertoId);

    List<Map<String, Object>> destinosPorAerolinea(Integer aerolineaId);
}