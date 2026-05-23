package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ReservaBoletoItemResponse;
import com.aeropuertolosprimos.backend.dto.ReservaPasajeroItemRequest;
import com.aeropuertolosprimos.backend.dto.ReservaRequest;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private static final String ESTADO_RESERVA_CONFIRMADA = "CONFIRMADA";
    private static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";

    private static final String ESTADO_BOLETO_PENDIENTE = "PENDIENTE_ABORDAR";
    private static final String ESTADO_BOLETO_CANCELADO = "CANCELADO";

    private static final String ESTADO_ASIENTO_DISPONIBLE = "DISPONIBLE";
    private static final String ESTADO_ASIENTO_RESERVADO = "RESERVADO";
    private static final String ESTADO_ASIENTO_DISPONIBLE_NOMBRE = "DISPONIBLE";

    private static final String TIPO_EQUIPAJE_MALETA = "MALETA";
    private static final String ESTADO_EQUIPAJE_REGISTRADO = "REGISTRADO";
    private static final String ESTADO_EQUIPAJE_CANCELADO = "CANCELADO";

    private final UserRepository userRepository;

    private final ReservaRepository reservaRepository;
    private final ReservaPasajeroRepository reservaPasajeroRepository;
    private final ReservaSegmentoRepository reservaSegmentoRepository;

    private final BoletoRepository boletoRepository;
    private final BoletoSegmentoRepository boletoSegmentoRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final EquipajeRepository equipajeRepository;

    private final PasajeroRepository pasajeroRepository;
    private final VueloOperadoRepository vueloOperadoRepository;
    private final SegmentoOperadoRepository segmentoOperadoRepository;
    private final SegmentoVueloRepository segmentoVueloRepository;
    private final AsientoVueloRepository asientoVueloRepository;
    private final AsientoUbiRepository asientoUbiRepository;

    private final EstadoReservaRepository estadoReservaRepository;
    private final EstadoBoletoRepository estadoBoletoRepository;
    private final EstadoAsientoRepository estadoAsientoRepository;
    private final TipoEquipajeRepository tipoEquipajeRepository;
    private final EstadoEquipajeRepository estadoEquipajeRepository;

    private final JdbcTemplate jdbc;

    @Override
    @Transactional
    public ReservaResponse crear(
            ReservaRequest request
    ) {

        validarBase(request);

        List<ReservaPasajeroItemRequest> items = normalizarItems(request);

        VueloOperado vueloOperado = vueloOperadoRepository.findById(request.getVueloOperadoId())
                .orElseThrow(() -> new BusinessException("Vuelo operado no encontrado"));

        SegmentoOperado segmentoOperado = segmentoOperadoRepository.findById(request.getSegmentoOperadoId())
                .orElseThrow(() -> new BusinessException("Segmento operado no encontrado"));

        if (!Objects.equals(segmentoOperado.getVueloOperadoId(), vueloOperado.getId())) {
            throw new BusinessException("El segmento no pertenece al vuelo operado seleccionado");
        }

        SegmentoVuelo segmentoVuelo = segmentoVueloRepository.findById(segmentoOperado.getSegmentoVueloId())
                .orElseThrow(() -> new BusinessException("Segmento de vuelo no encontrado"));

        EstadoReserva estadoReserva = estadoReservaRepository
                .findByNombreIgnoreCase(ESTADO_RESERVA_CONFIRMADA)
                .orElseThrow(() -> new BusinessException("Estado de reserva CONFIRMADA no encontrado"));

        EstadoBoleto estadoBoleto = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_PENDIENTE)
                .orElseThrow(() -> new BusinessException("Estado de boleto PENDIENTE_ABORDAR no encontrado"));

        EstadoBoleto estadoCancelado = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_CANCELADO)
                .orElse(null);

        EstadoAsiento estadoDisponible = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_DISPONIBLE)
                .orElseThrow(() -> new BusinessException("Estado de asiento DISPONIBLE no encontrado"));

        EstadoAsiento estadoReservado = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_RESERVADO)
                .orElseThrow(() -> new BusinessException("Estado de asiento RESERVADO no encontrado"));

        TipoEquipaje tipoMaleta = tipoEquipajeRepository
                .findByNombreIgnoreCase(TIPO_EQUIPAJE_MALETA)
                .orElseThrow(() -> new BusinessException("Tipo de equipaje MALETA no encontrado"));

        EstadoEquipaje estadoEquipaje = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_REGISTRADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje REGISTRADO no encontrado"));

        Integer estadoCanceladoId = estadoCancelado != null ? estadoCancelado.getId() : null;

        Set<Integer> pasajerosUsados = new HashSet<>();
        Set<Integer> asientosUsados = new HashSet<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal recargoTotal = BigDecimal.ZERO;

        Integer compradorUserId = request.getUserId();
        Integer primerPasajeroId = null;

        if (compradorUserId != null) {
            userRepository.findById(compradorUserId)
                    .orElseThrow(() -> new BusinessException("Usuario comprador no encontrado"));
        }

        for (ReservaPasajeroItemRequest item : items) {

            validarItem(item);

            Pasajero pasajero = resolverPasajero(item);

            if (!pasajerosUsados.add(pasajero.getId())) {
                throw new BusinessException("No puede repetir pasajeros en la misma reserva");
            }

            if (primerPasajeroId == null) {
                primerPasajeroId = pasajero.getId();
            }

            if (compradorUserId == null && pasajero.getUser() != null) {
                compradorUserId = pasajero.getUser().getId();
            }

            long cruces = boletoRepository.countBoletosPasajeroMismaFechaHora(
                    pasajero.getId(),
                    segmentoVuelo.getFechaSalida(),
                    segmentoVuelo.getHoraSalida(),
                    estadoCanceladoId
            );

            if (cruces > 0) {
                throw new BusinessException(
                        "No se puede seleccionar el vuelo porque el pasajero " +
                                pasajero.getNombreCompleto() +
                                " ya tiene vuelos asignados"
                );
            }

            boolean requiereAsiento = requiereAsiento(item);

            if (requiereAsiento) {

                if (item.getAsientoVueloId() == null || item.getClaseVueloId() == null) {
                    throw new BusinessException("Debe ingresar asiento y clase para cada pasajero que requiere asiento");
                }

                if (!asientosUsados.add(item.getAsientoVueloId())) {
                    throw new BusinessException("No puede repetir asientos en la misma reserva");
                }

                AsientoVuelo asientoVuelo = asientoVueloRepository.findById(item.getAsientoVueloId())
                        .orElseThrow(() -> new BusinessException("Asiento de vuelo no encontrado: " + item.getAsientoVueloId()));

                if (!Objects.equals(asientoVuelo.getSegmentoOperadoId(), segmentoOperado.getId())) {
                    throw new BusinessException("El asiento no pertenece al segmento seleccionado");
                }

                if (!Objects.equals(asientoVuelo.getEstadoAsientoId(), estadoDisponible.getId())) {
                    throw new BusinessException("El asiento " + item.getAsientoVueloId() + " no está disponible");
                }

                AsientoUbi asientoUbi = asientoUbiRepository
                        .findFirstByCodigoAsientoSistemaOrderByIdAsc(asientoVuelo.getCodigoAsientoSistema())
                        .orElseThrow(() -> new BusinessException("No se encontró la ubicación física del asiento"));

                if (asientoUbi.getClaseVueloId() == null ||
                        !Objects.equals(asientoUbi.getClaseVueloId(), item.getClaseVueloId())) {

                    throw new BusinessException("La clase seleccionada no coincide con la clase del asiento");
                }

                BigDecimal precioCalculado = resolverPrecioBase(
                        vueloOperado.getVueloProgramadoId(),
                        item.getClaseVueloId(),
                        item.getPrecioBase()
                );

                item.setPrecioBase(precioCalculado);

                BigDecimal recargoAsiento = resolverRecargoAsiento(
                        vueloOperado.getVueloProgramadoId(),
                        asientoUbi
                );

                recargoTotal = recargoTotal.add(recargoAsiento);
            }

            subtotal = subtotal.add(
                    item.getPrecioBase() != null ? item.getPrecioBase() : BigDecimal.ZERO
            );
        }

        Reserva reserva = new Reserva();

        reserva.setUserId(compradorUserId);
        reserva.setPasajeroId(primerPasajeroId);
        reserva.setVueloOperadoId(vueloOperado.getId());
        reserva.setEstadoReservaId(estadoReserva.getId());
        reserva.setSubtotal(subtotal);
        reserva.setRecargoTotal(recargoTotal);
        reserva.setTotal(subtotal.add(recargoTotal));
        reserva.setEstadoId(1);

        reserva = reservaRepository.save(reserva);

        reserva.setCodigoReserva(generarCodigo("RES", reserva.getId()));

        reserva = reservaRepository.save(reserva);

        ReservaSegmento reservaSegmento = new ReservaSegmento();

        reservaSegmento.setReservaId(reserva.getId());
        reservaSegmento.setSegmentoOperadoId(segmentoOperado.getId());
        reservaSegmento.setOrdenSegmento(segmentoOperado.getOrdenSegmento());

        reservaSegmentoRepository.save(reservaSegmento);

        for (ReservaPasajeroItemRequest item : items) {

            Pasajero pasajero = resolverPasajero(item);

            ReservaPasajero reservaPasajero = new ReservaPasajero();

            reservaPasajero.setReservaId(reserva.getId());
            reservaPasajero.setPasajeroId(pasajero.getId());

            reservaPasajeroRepository.save(reservaPasajero);

            BigDecimal precioBase = item.getPrecioBase() != null
                    ? item.getPrecioBase()
                    : BigDecimal.ZERO;

            BigDecimal recargoAsiento = BigDecimal.ZERO;

            if (requiereAsiento(item)) {

                AsientoVuelo asientoVueloTmp = asientoVueloRepository.findById(item.getAsientoVueloId())
                        .orElseThrow(() -> new BusinessException("Asiento de vuelo no encontrado"));

                AsientoUbi asientoUbiTmp = asientoUbiRepository
                        .findFirstByCodigoAsientoSistemaOrderByIdAsc(asientoVueloTmp.getCodigoAsientoSistema())
                        .orElseThrow(() -> new BusinessException("No se encontró la ubicación física del asiento"));

                recargoAsiento = resolverRecargoAsiento(
                        vueloOperado.getVueloProgramadoId(),
                        asientoUbiTmp
                );
            }

            Boleto boleto = new Boleto();

            boleto.setReservaId(reserva.getId());
            boleto.setPasajeroId(pasajero.getId());
            boleto.setVueloOperadoId(vueloOperado.getId());
            boleto.setEstadoBoletoId(estadoBoleto.getId());
            boleto.setPrecioBase(precioBase);
            boleto.setRecargoEquipaje(BigDecimal.ZERO);
            boleto.setTotal(precioBase.add(recargoAsiento));
            boleto.setEstadoId(1);

            boleto = boletoRepository.save(boleto);

            boleto.setCodigoBoleto(generarCodigo("BOL", boleto.getId()));
            boleto.setCodigoPaseAbordar(generarCodigo("PAB", boleto.getId()));

            boleto = boletoRepository.save(boleto);

            BoletoSegmento boletoSegmento = new BoletoSegmento();

            boletoSegmento.setBoletoId(boleto.getId());
            boletoSegmento.setSegmentoOperadoId(segmentoOperado.getId());
            boletoSegmento.setOrdenSegmento(segmentoOperado.getOrdenSegmento());
            boletoSegmento.setEstadoBoletoId(estadoBoleto.getId());

            boletoSegmento = boletoSegmentoRepository.save(boletoSegmento);

            if (requiereAsiento(item)) {

                AsientoVuelo asientoVuelo = asientoVueloRepository.findById(item.getAsientoVueloId())
                        .orElseThrow(() -> new BusinessException("Asiento de vuelo no encontrado"));

                BoletoAsiento boletoAsiento = new BoletoAsiento();

                boletoAsiento.setBoletoSegmentoId(boletoSegmento.getId());
                boletoAsiento.setAsientoVueloId(asientoVuelo.getId());
                boletoAsiento.setClaseVueloId(item.getClaseVueloId());

                boletoAsientoRepository.save(boletoAsiento);

                asientoVuelo.setEstadoAsientoId(estadoReservado.getId());

                asientoVueloRepository.save(asientoVuelo);
            }

            int cantidadMaletas = item.getCantidadMaletas() != null
                    ? item.getCantidadMaletas()
                    : 0;

            for (int i = 1; i <= cantidadMaletas; i++) {

                Equipaje equipaje = new Equipaje();

                equipaje.setBoletoId(boleto.getId());
                equipaje.setPasajeroId(pasajero.getId());
                equipaje.setSegmentoOperadoId(segmentoOperado.getId());
                equipaje.setTipoEquipajeId(tipoMaleta.getId());
                equipaje.setEstadoEquipajeId(estadoEquipaje.getId());
                equipaje.setNumeroMaleta(i);
                equipaje.setMontoRecargo(BigDecimal.ZERO);

                equipajeRepository.save(equipaje);
            }
        }

        ReservaResponse response = mapResponse(reserva);

        response.setMensaje("Reserva creada correctamente");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse obtenerPorId(
            Integer id
    ) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        return mapResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorPasajero(
            Integer pasajeroId
    ) {

        if (pasajeroId == null) {
            throw new BusinessException("Debe ingresar el pasajero");
        }

        pasajeroRepository.findById(pasajeroId)
                .orElseThrow(() -> new BusinessException("Pasajero no encontrado"));

        return reservaPasajeroRepository
                .findByPasajeroIdAndEstadoIdOrderByIdDesc(pasajeroId, 1)
                .stream()
                .map(rp -> reservaRepository.findById(rp.getReservaId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(r -> Objects.equals(r.getEstadoId(), 1))
                .map(this::mapResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservaResponse cancelar(
            Integer id
    ) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        EstadoReserva estadoCancelada = estadoReservaRepository
                .findByNombreIgnoreCase(ESTADO_RESERVA_CANCELADA)
                .orElseThrow(() -> new BusinessException("Estado de reserva CANCELADA no encontrado"));

        EstadoBoleto estadoBoletoCancelado = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de boleto CANCELADO no encontrado"));

        EstadoAsiento estadoDisponible = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_DISPONIBLE_NOMBRE)
                .orElseThrow(() -> new BusinessException("Estado de asiento DISPONIBLE no encontrado"));

        EstadoEquipaje estadoEquipajeCancelado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje CANCELADO no encontrado"));

        if (Objects.equals(estadoCancelada.getId(), reserva.getEstadoReservaId())) {
            throw new BusinessException("La reserva ya está cancelada");
        }

        reserva.setEstadoReservaId(estadoCancelada.getId());

        reserva = reservaRepository.save(reserva);

        List<Boleto> boletos = boletoRepository.findByReservaIdOrderByIdAsc(reserva.getId());

        for (Boleto boleto : boletos) {

            boleto.setEstadoBoletoId(estadoBoletoCancelado.getId());

            boletoRepository.save(boleto);

            List<BoletoSegmento> segmentos = boletoSegmentoRepository
                    .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

            for (BoletoSegmento boletoSegmento : segmentos) {

                boletoSegmento.setEstadoBoletoId(estadoBoletoCancelado.getId());

                boletoSegmentoRepository.save(boletoSegmento);

                List<BoletoAsiento> asientos = boletoAsientoRepository
                        .findByBoletoSegmentoId(boletoSegmento.getId());

                for (BoletoAsiento boletoAsiento : asientos) {

                    if (boletoAsiento.getAsientoVueloId() == null) {
                        continue;
                    }

                    asientoVueloRepository.findById(boletoAsiento.getAsientoVueloId())
                            .ifPresent(asientoVuelo -> {
                                asientoVuelo.setEstadoAsientoId(estadoDisponible.getId());
                                asientoVueloRepository.save(asientoVuelo);
                            });
                }
            }

            List<Equipaje> equipajes = equipajeRepository.findByBoletoId(boleto.getId());

            for (Equipaje equipaje : equipajes) {
                equipaje.setEstadoEquipajeId(estadoEquipajeCancelado.getId());
                equipajeRepository.save(equipaje);
            }
        }

        ReservaResponse response = mapResponse(reserva);

        response.setMensaje("Reserva cancelada correctamente");

        return response;
    }

    private BigDecimal resolverPrecioBase(
            Integer vueloProgramadoId,
            Integer claseVueloId,
            BigDecimal precioRequest
    ) {

        if (vueloProgramadoId == null || claseVueloId == null) {
            return precioRequest != null ? precioRequest : BigDecimal.ZERO;
        }

        List<BigDecimal> precios = jdbc.queryForList(
                """
                SELECT precio
                FROM precio_vuelo
                WHERE vuelo_programado_id = ?
                  AND clase_vuelo_id = ?
                  AND (fecha_vigencia_desde IS NULL OR fecha_vigencia_desde <= CURRENT_DATE)
                  AND (fecha_vigencia_hasta IS NULL OR fecha_vigencia_hasta >= CURRENT_DATE)
                ORDER BY fecha_vigencia_desde DESC NULLS LAST, id DESC
                LIMIT 1
                """,
                BigDecimal.class,
                vueloProgramadoId,
                claseVueloId
        );

        if (!precios.isEmpty()) {
            return precios.get(0) != null ? precios.get(0) : BigDecimal.ZERO;
        }

        if (precioRequest != null) {
            return precioRequest;
        }

        throw new BusinessException("No existe precio configurado para la clase seleccionada");
    }

    private BigDecimal resolverRecargoAsiento(
            Integer vueloProgramadoId,
            AsientoUbi asientoUbi
    ) {

        if (vueloProgramadoId == null ||
                asientoUbi == null ||
                asientoUbi.getClaseVueloId() == null ||
                asientoUbi.getTipoAsientoId() == null) {

            return BigDecimal.ZERO;
        }

        List<BigDecimal> recargos = jdbc.queryForList(
                """
                SELECT COALESCE(rat.recargo, 0)
                FROM recargo_asiento_tipo rat
                JOIN tipo_asiento ta ON UPPER(ta.nombre) = UPPER(rat.tipo_asiento)
                WHERE rat.vuelo_programado_id = ?
                  AND rat.clase_vuelo_id = ?
                  AND ta.id = ?
                ORDER BY rat.id DESC
                LIMIT 1
                """,
                BigDecimal.class,
                vueloProgramadoId,
                asientoUbi.getClaseVueloId(),
                asientoUbi.getTipoAsientoId()
        );

        if (recargos.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return recargos.get(0) != null ? recargos.get(0) : BigDecimal.ZERO;
    }

    private void validarBase(
            ReservaRequest request
    ) {

        if (request == null ||
                request.getVueloOperadoId() == null ||
                request.getSegmentoOperadoId() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }
    }

    private List<ReservaPasajeroItemRequest> normalizarItems(
            ReservaRequest request
    ) {

        if (request.getPasajeros() != null && !request.getPasajeros().isEmpty()) {
            return request.getPasajeros();
        }

        ReservaPasajeroItemRequest item = new ReservaPasajeroItemRequest();

        item.setPasajeroId(request.getPasajeroId());
        item.setAsientoVueloId(request.getAsientoVueloId());
        item.setClaseVueloId(request.getClaseVueloId());
        item.setCantidadMaletas(request.getCantidadMaletas());
        item.setPrecioBase(request.getPrecioBase());
        item.setRequiereAsiento(request.getRequiereAsiento());

        return List.of(item);
    }

    private Pasajero resolverPasajero(
            ReservaPasajeroItemRequest item
    ) {

        if (item.getPasajeroId() != null) {

            return pasajeroRepository.findById(item.getPasajeroId())
                    .orElseThrow(() ->
                            new BusinessException("Pasajero no encontrado: " + item.getPasajeroId())
                    );
        }

        if (item.getPasaporte() == null ||
                item.getPasaporte().isBlank() ||
                item.getNombreCompleto() == null ||
                item.getNombreCompleto().isBlank() ||
                item.getFechaNacimiento() == null ||
                item.getNacionalidad() == null ||
                item.getNacionalidad().isBlank() ||
                item.getTelefonoEmergencia() == null ||
                item.getTelefonoEmergencia().isBlank()) {

            throw new BusinessException("Debe ingresar los datos obligatorios del pasajero");
        }

        String pasaporte = item.getPasaporte().trim();

        Pasajero existente = pasajeroRepository.findByPasaporte(pasaporte)
                .orElse(null);

        if (existente != null) {
            item.setPasajeroId(existente.getId());
            return existente;
        }

        Pasajero pasajero = new Pasajero();

        pasajero.setUser(null);
        pasajero.setPasaporte(pasaporte);
        pasajero.setNombreCompleto(item.getNombreCompleto().trim());
        pasajero.setFechaNacimiento(item.getFechaNacimiento());
        pasajero.setNacionalidad(item.getNacionalidad().trim());
        pasajero.setCodigoArea(limpiarTexto(item.getCodigoArea()));
        pasajero.setTelefono(limpiarTexto(item.getTelefono()));
        pasajero.setTelefonoEmergencia(item.getTelefonoEmergencia().trim());
        pasajero.setDireccion(limpiarTexto(item.getDireccion()));
        pasajero.setEstadoId(1);

        pasajero = pasajeroRepository.save(pasajero);

        item.setPasajeroId(pasajero.getId());

        return pasajero;
    }

    private String limpiarTexto(
            String valor
    ) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private void validarItem(
            ReservaPasajeroItemRequest item
    ) {

        if (item == null ||
                item.getCantidadMaletas() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios de cada pasajero");
        }

        if (item.getPasajeroId() == null) {

            if (item.getPasaporte() == null ||
                    item.getPasaporte().isBlank() ||
                    item.getNombreCompleto() == null ||
                    item.getNombreCompleto().isBlank() ||
                    item.getFechaNacimiento() == null ||
                    item.getNacionalidad() == null ||
                    item.getNacionalidad().isBlank() ||
                    item.getTelefonoEmergencia() == null ||
                    item.getTelefonoEmergencia().isBlank()) {

                throw new BusinessException("Debe ingresar los datos obligatorios del pasajero");
            }
        }

        if (item.getCantidadMaletas() < 0) {
            throw new BusinessException("La cantidad de maletas no puede ser negativa");
        }
    }

    private boolean requiereAsiento(
            ReservaPasajeroItemRequest item
    ) {

        return item.getRequiereAsiento() == null || item.getRequiereAsiento();
    }

    private ReservaResponse mapResponse(
            Reserva reserva
    ) {

        ReservaResponse response = new ReservaResponse();

        response.setReservaId(reserva.getId());
        response.setUserId(reserva.getUserId());
        response.setVueloOperadoId(reserva.getVueloOperadoId());
        response.setCodigoReserva(reserva.getCodigoReserva());
        response.setSubtotal(reserva.getSubtotal());
        response.setRecargoTotal(reserva.getRecargoTotal());
        response.setTotal(reserva.getTotal());

        if (reserva.getEstadoReservaId() != null) {
            estadoReservaRepository.findById(reserva.getEstadoReservaId())
                    .ifPresent(e -> response.setEstadoReserva(e.getNombre()));
        }

        List<Boleto> boletos = boletoRepository.findByReservaIdOrderByIdAsc(reserva.getId());

        List<ReservaBoletoItemResponse> boletosResponse = boletos.stream()
                .map(this::mapBoletoItem)
                .toList();

        response.setBoletos(boletosResponse);

        if (!boletos.isEmpty()) {

            Boleto primerBoleto = boletos.get(0);
            ReservaBoletoItemResponse primerItem = boletosResponse.get(0);

            response.setBoletoId(primerBoleto.getId());
            response.setCodigoBoleto(primerBoleto.getCodigoBoleto());
            response.setCodigoPaseAbordar(primerBoleto.getCodigoPaseAbordar());

            if (primerBoleto.getEstadoBoletoId() != null) {
                estadoBoletoRepository.findById(primerBoleto.getEstadoBoletoId())
                        .ifPresent(e -> response.setEstadoBoleto(e.getNombre()));
            }

            response.setAsientoVueloId(primerItem.getAsientoVueloId());
            response.setAsiento(primerItem.getAsiento());
            response.setCantidadMaletas(primerItem.getCantidadMaletas());
        }

        response.setMensaje("Reserva encontrada");

        return response;
    }

    private ReservaBoletoItemResponse mapBoletoItem(
            Boleto boleto
    ) {

        ReservaBoletoItemResponse response = new ReservaBoletoItemResponse();

        response.setPasajeroId(boleto.getPasajeroId());
        response.setBoletoId(boleto.getId());
        response.setCodigoBoleto(boleto.getCodigoBoleto());
        response.setCodigoPaseAbordar(boleto.getCodigoPaseAbordar());
        response.setTotal(boleto.getTotal());

        pasajeroRepository.findById(boleto.getPasajeroId())
                .ifPresent(pasajero -> {
                    response.setNombrePasajero(pasajero.getNombreCompleto());
                    response.setPasaporte(pasajero.getPasaporte());
                });

        List<Equipaje> equipajes = equipajeRepository.findByBoletoId(boleto.getId());

        response.setCantidadMaletas(equipajes.size());

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

        if (segmentos.isEmpty()) {
            return response;
        }

        List<BoletoAsiento> asientos = boletoAsientoRepository
                .findByBoletoSegmentoId(segmentos.get(0).getId());

        if (asientos.isEmpty()) {
            return response;
        }

        BoletoAsiento boletoAsiento = asientos.get(0);

        response.setAsientoVueloId(boletoAsiento.getAsientoVueloId());

        if (boletoAsiento.getAsientoVueloId() != null) {

            asientoVueloRepository.findById(boletoAsiento.getAsientoVueloId())
                    .ifPresent(asientoVuelo -> {

                        if (asientoVuelo.getCodigoAsientoSistema() != null) {
                            asientoUbiRepository
                                    .findFirstByCodigoAsientoSistemaOrderByIdAsc(
                                            asientoVuelo.getCodigoAsientoSistema()
                                    )
                                    .ifPresent(asientoUbi ->
                                            response.setAsiento(asientoUbi.getNumeroAsiento())
                                    );
                        }
                    });
        }

        return response;
    }

    private String generarCodigo(
            String prefijo,
            Integer id
    ) {
        return prefijo + "-" + String.format("%06d", id);
    }
}