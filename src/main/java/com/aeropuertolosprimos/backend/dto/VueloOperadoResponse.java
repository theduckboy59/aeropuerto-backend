package com.aeropuertolosprimos.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class VueloOperadoResponse {

    private Integer id;

    private Integer vueloProgramadoId;

    private Integer vueloId;

    private String codigoVuelo;

    private Integer aerolineaId;

    private String aerolineaNombre;

    private Integer aeropuertoSalidaId;

    private String aeropuertoSalidaNombre;

    private String aeropuertoSalidaCodigoIata;

    private String puertaEmbarqueSalida;

    private Integer aeropuertoLlegadaId;

    private String aeropuertoLlegadaNombre;

    private String aeropuertoLlegadaCodigoIata;

    private String puertaEmbarqueLlegada;

    private LocalDate fechaSalidaProgramada;

    private LocalTime horaSalidaProgramada;

    private LocalDate fechaLlegadaProgramada;

    private LocalTime horaLlegadaProgramada;

    private Integer tipoSegmentoVueloId;

    private String tipoSegmentoVueloNombre;

    private Boolean requiereNuevoAsiento;

    private Boolean permiteEmbarque;

    private Boolean detieneFlujoSiCancela;

    private Integer estadoVueloId;

    private String estadoVueloNombre;

    private Integer cantidadSegmentos;

    private Integer segmentoActualOrden;

    private Boolean tuvoEscala;

    private Boolean puedeEditarDatos;

    private Boolean puedeCancelar;

    private Boolean puedeFinalizar;

    private List<SegmentoOperadoResponse> segmentos;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}