package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ReservaBoletoItemResponse;
import com.aeropuertolosprimos.backend.dto.ReservaBoletoSegmentoResponse;
import com.aeropuertolosprimos.backend.dto.ReservaPasajeroItemRequest;
import com.aeropuertolosprimos.backend.dto.ReservaRequest;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;
import com.aeropuertolosprimos.backend.dto.ReservaSegmentoAsientoRequest;
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
    private final ClaseVueloRepository claseVueloRepository;

    private final CatalogoEstadoService catalogoEstadoService;

    private final JdbcTemplate jdbc;

    @Override
    @Transactional
    public ReservaResponse crear(
            ReservaRequest request
    ) {

        validarBase(request);

        List<ReservaPasajeroItemRequest> items = normalizarItems(request);

        VueloOperado vueloOperado = vueloOperadoRepository
                .findById(request.getVueloOperadoId())
                .orElseThrow(() ->
                        new BusinessException("Vuelo operado no encontrado")
                );

        List<SegmentoOperado> segmentosOperados = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(vueloOperado.getId());

        if (segmentosOperados.isEmpty()) {
            throw new BusinessException("El vuelo operado no tiene segmentos");
        }

        EstadoReserva estadoReserva = estadoReservaRepository
                .findByNombreIgnoreCase(ESTADO_RESERVA_CONFIRMADA)
                .orElseThrow(() ->
                        new BusinessException("Estado de reserva CONFIRMADA no encontrado")
                );

        EstadoBoleto estadoBoleto = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_PENDIENTE)
                .orElseThrow(() ->
                        new BusinessException("Estado de boleto PENDIENTE_ABORDAR no encontrado")
                );

        EstadoBoleto estadoCancelado = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_CANCELADO)
                .orElse(null);

        EstadoAsiento estadoDisponible = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_DISPONIBLE)
                .orElseThrow(() ->
                        new BusinessException("Estado de asiento DISPONIBLE no encontrado")
                );

        EstadoAsiento estadoReservado = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_RESERVADO)
                .orElseThrow(() ->
                        new BusinessException("Estado de asiento RESERVADO no encontrado")
                );

        TipoEquipaje tipoMaleta = tipoEquipajeRepository
                .findByNombreIgnoreCase(TIPO_EQUIPAJE_MALETA)
                .orElseThrow(() ->
                        new BusinessException("Tipo de equipaje MALETA no encontrado")
                );

        EstadoEquipaje estadoEquipaje = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_REGISTRADO)
                .orElseThrow(() ->
                        new BusinessException("Estado de equipaje REGISTRADO no encontrado")
                );

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();
        Integer estadoCanceladoId = estadoCancelado != null ? estadoCancelado.getId() : null;

        if (request.getUserId() != null) {
            userRepository.findById(request.getUserId())
                    .orElseThrow(() ->
                            new BusinessException("Usuario comprador no encontrado")
                    );
        }

        List<PasajeroPlan> planes = construirPlanes(
                request,
                items,
                vueloOperado,
                segmentosOperados,
                estadoDisponible,
                estadoCanceladoId
        );

        Integer compradorUserId = request.getUserId();
        Integer primerPasajeroId = null;

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal recargoTotal = BigDecimal.ZERO;

        for (PasajeroPlan plan : planes) {

            if (primerPasajeroId == null) {
                primerPasajeroId = plan.pasajero.getId();
            }

            if (compradorUserId == null && plan.pasajero.getUser() != null) {
                compradorUserId = plan.pasajero.getUser().getId();
            }

            subtotal = subtotal.add(plan.precioBase);
            recargoTotal = recargoTotal.add(plan.recargoAsiento);
        }

        Reserva reserva = new Reserva();

        reserva.setUserId(compradorUserId);
        reserva.setPasajeroId(primerPasajeroId);
        reserva.setVueloOperadoId(vueloOperado.getId());
        reserva.setEstadoReservaId(estadoReserva.getId());
        reserva.setSubtotal(subtotal);
        reserva.setRecargoTotal(recargoTotal);
        reserva.setTotal(subtotal.add(recargoTotal));
        reserva.setEstadoId(estadoActivoId);

        reserva = reservaRepository.save(reserva);

        reserva.setCodigoReserva(generarCodigo("RES", reserva.getId()));

        reserva = reservaRepository.save(reserva);

        for (SegmentoOperado segmentoOperado : segmentosOperados) {

            ReservaSegmento reservaSegmento = new ReservaSegmento();

            reservaSegmento.setReservaId(reserva.getId());
            reservaSegmento.setSegmentoOperadoId(segmentoOperado.getId());
            reservaSegmento.setOrdenSegmento(segmentoOperado.getOrdenSegmento());
            reservaSegmento.setEstadoId(estadoActivoId);

            reservaSegmentoRepository.save(reservaSegmento);
        }

        for (PasajeroPlan plan : planes) {

            ReservaPasajero reservaPasajero = new ReservaPasajero();

            reservaPasajero.setReservaId(reserva.getId());
            reservaPasajero.setPasajeroId(plan.pasajero.getId());
            reservaPasajero.setEstadoId(estadoActivoId);

            reservaPasajeroRepository.save(reservaPasajero);

            Boleto boleto = new Boleto();

            boleto.setReservaId(reserva.getId());
            boleto.setPasajeroId(plan.pasajero.getId());
            boleto.setVueloOperadoId(vueloOperado.getId());
            boleto.setEstadoBoletoId(estadoBoleto.getId());
            boleto.setPrecioBase(plan.precioBase);
            boleto.setRecargoEquipaje(BigDecimal.ZERO);
            boleto.setTotal(plan.precioBase.add(plan.recargoAsiento));
            boleto.setEstadoId(estadoActivoId);

            boleto = boletoRepository.save(boleto);

            boleto.setCodigoBoleto(generarCodigo("BOL", boleto.getId()));
            boleto.setCodigoPaseAbordar(generarCodigo("PAB", boleto.getId()));

            boleto = boletoRepository.save(boleto);

            for (SegmentoAsientoPlan segmentoPlan : plan.segmentos) {

                BoletoSegmento boletoSegmento = new BoletoSegmento();

                boletoSegmento.setBoletoId(boleto.getId());
                boletoSegmento.setSegmentoOperadoId(segmentoPlan.segmentoOperado.getId());
                boletoSegmento.setOrdenSegmento(segmentoPlan.segmentoOperado.getOrdenSegmento());
                boletoSegmento.setEstadoBoletoId(estadoBoleto.getId());
                boletoSegmento.setEstadoId(estadoActivoId);

                boletoSegmento = boletoSegmentoRepository.save(boletoSegmento);

                if (segmentoPlan.asientoVuelo != null) {

                    BoletoAsiento boletoAsiento = new BoletoAsiento();

                    boletoAsiento.setBoletoSegmentoId(boletoSegmento.getId());
                    boletoAsiento.setAsientoVueloId(segmentoPlan.asientoVuelo.getId());
                    boletoAsiento.setClaseVueloId(plan.item.getClaseVueloId());
                    boletoAsiento.setEstadoId(estadoActivoId);

                    boletoAsientoRepository.save(boletoAsiento);

                    segmentoPlan.asientoVuelo.setEstadoAsientoId(estadoReservado.getId());

                    asientoVueloRepository.save(segmentoPlan.asientoVuelo);
                }

                int cantidadMaletas = plan.item.getCantidadMaletas() != null
                        ? plan.item.getCantidadMaletas()
                        : 0;

                for (int i = 1; i <= cantidadMaletas; i++) {

                    Equipaje equipaje = new Equipaje();

                    equipaje.setBoletoId(boleto.getId());
                    equipaje.setPasajeroId(plan.pasajero.getId());
                    equipaje.setSegmentoOperadoId(segmentoPlan.segmentoOperado.getId());
                    equipaje.setTipoEquipajeId(tipoMaleta.getId());
                    equipaje.setEstadoEquipajeId(estadoEquipaje.getId());
                    equipaje.setNumeroMaleta(i);
                    equipaje.setMontoRecargo(BigDecimal.ZERO);
                    equipaje.setEstadoId(estadoActivoId);

                    equipajeRepository.save(equipaje);
                }
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
                .orElseThrow(() ->
                        new BusinessException("Reserva no encontrada")
                );

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
                .orElseThrow(() ->
                        new BusinessException("Pasajero no encontrado")
                );

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return reservaPasajeroRepository
                .findByPasajeroIdAndEstadoIdOrderByIdDesc(
                        pasajeroId,
                        estadoActivoId
                )
                .stream()
                .map(rp -> reservaRepository.findById(rp.getReservaId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(r -> Objects.equals(r.getEstadoId(), estadoActivoId))
                .map(this::mapResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservaResponse cancelar(
            Integer id
    ) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("Reserva no encontrada")
                );

        EstadoReserva estadoCancelada = estadoReservaRepository
                .findByNombreIgnoreCase(ESTADO_RESERVA_CANCELADA)
                .orElseThrow(() ->
                        new BusinessException("Estado de reserva CANCELADA no encontrado")
                );

        EstadoBoleto estadoBoletoCancelado = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_CANCELADO)
                .orElseThrow(() ->
                        new BusinessException("Estado de boleto CANCELADO no encontrado")
                );

        EstadoAsiento estadoDisponible = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_DISPONIBLE)
                .orElseThrow(() ->
                        new BusinessException("Estado de asiento DISPONIBLE no encontrado")
                );

        EstadoEquipaje estadoEquipajeCancelado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_CANCELADO)
                .orElseThrow(() ->
                        new BusinessException("Estado de equipaje CANCELADO no encontrado")
                );

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

    private List<PasajeroPlan> construirPlanes(
            ReservaRequest request,
            List<ReservaPasajeroItemRequest> items,
            VueloOperado vueloOperado,
            List<SegmentoOperado> segmentosOperados,
            EstadoAsiento estadoDisponible,
            Integer estadoCanceladoId
    ) {

        Set<Integer> pasajerosUsados = new HashSet<>();
        Set<Integer> asientosUsados = new HashSet<>();

        List<PasajeroPlan> planes = new ArrayList<>();

        boolean permiteSegmentosGlobales = items.size() == 1;

        for (ReservaPasajeroItemRequest item : items) {

            validarItem(item);

            Pasajero pasajero = resolverPasajero(item);

            if (!pasajerosUsados.add(pasajero.getId())) {
                throw new BusinessException("No puede repetir pasajeros en la misma reserva");
            }

            validarCruceHorarioPasajero(
                    pasajero,
                    segmentosOperados,
                    estadoCanceladoId
            );

            boolean requiereAsiento = requiereAsiento(item);

            List<SegmentoAsientoPlan> segmentosPlan = new ArrayList<>();

            BigDecimal precioBase = BigDecimal.ZERO;
            BigDecimal recargoAsiento = BigDecimal.ZERO;

            if (requiereAsiento) {

                if (item.getClaseVueloId() == null) {
                    throw new BusinessException("Debe ingresar la clase del vuelo");
                }

                List<ReservaSegmentoAsientoRequest> seleccionados =
                        normalizarSegmentosAsientos(
                                request,
                                item,
                                segmentosOperados,
                                permiteSegmentosGlobales
                        );

                Map<Integer, ReservaSegmentoAsientoRequest> seleccionPorSegmento =
                        mapearSeleccionPorSegmento(
                                seleccionados,
                                vueloOperado.getId()
                        );

                for (SegmentoOperado segmentoOperado : segmentosOperados) {

                    ReservaSegmentoAsientoRequest seleccionado =
                            seleccionPorSegmento.get(segmentoOperado.getId());

                    if (seleccionado == null || seleccionado.getAsientoVueloId() == null) {
                        throw new BusinessException("Debe seleccionar asiento para cada segmento del vuelo");
                    }

                    if (!asientosUsados.add(seleccionado.getAsientoVueloId())) {
                        throw new BusinessException("No puede repetir asientos en la misma reserva");
                    }

                    AsientoVuelo asientoVuelo = asientoVueloRepository
                            .findById(seleccionado.getAsientoVueloId())
                            .orElseThrow(() ->
                                    new BusinessException("Asiento de vuelo no encontrado: " + seleccionado.getAsientoVueloId())
                            );

                    if (!Objects.equals(asientoVuelo.getSegmentoOperadoId(), segmentoOperado.getId())) {
                        throw new BusinessException("El asiento no pertenece al segmento seleccionado");
                    }

                    if (!Objects.equals(asientoVuelo.getEstadoAsientoId(), estadoDisponible.getId())) {
                        throw new BusinessException("El asiento " + asientoVuelo.getId() + " no está disponible");
                    }

                    AsientoUbi asientoUbi = asientoUbiRepository
                            .findFirstByCodigoAsientoSistemaOrderByIdAsc(asientoVuelo.getCodigoAsientoSistema())
                            .orElseThrow(() ->
                                    new BusinessException("No se encontró la ubicación física del asiento")
                            );

                    if (asientoUbi.getClaseVueloId() == null ||
                            !Objects.equals(asientoUbi.getClaseVueloId(), item.getClaseVueloId())) {

                        throw new BusinessException("La clase seleccionada no coincide con la clase del asiento");
                    }

                    recargoAsiento = recargoAsiento.add(
                            resolverRecargoAsiento(
                                    vueloOperado.getVueloProgramadoId(),
                                    asientoUbi
                            )
                    );

                    segmentosPlan.add(
                            new SegmentoAsientoPlan(
                                    segmentoOperado,
                                    asientoVuelo,
                                    asientoUbi
                            )
                    );
                }

                precioBase = resolverPrecioBase(
                        vueloOperado.getVueloProgramadoId(),
                        item.getClaseVueloId(),
                        item.getPrecioBase()
                );

                item.setPrecioBase(precioBase);

            } else {

                for (SegmentoOperado segmentoOperado : segmentosOperados) {
                    segmentosPlan.add(
                            new SegmentoAsientoPlan(
                                    segmentoOperado,
                                    null,
                                    null
                            )
                    );
                }

                precioBase = item.getPrecioBase() != null
                        ? item.getPrecioBase()
                        : BigDecimal.ZERO;
            }

            planes.add(
                    new PasajeroPlan(
                            item,
                            pasajero,
                            segmentosPlan,
                            precioBase,
                            recargoAsiento
                    )
            );
        }

        return planes;
    }

    private void validarCruceHorarioPasajero(
            Pasajero pasajero,
            List<SegmentoOperado> segmentosOperados,
            Integer estadoCanceladoId
    ) {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        for (SegmentoOperado segmentoOperado : segmentosOperados) {

            SegmentoVuelo segmentoVuelo = segmentoVueloRepository
                    .findById(segmentoOperado.getSegmentoVueloId())
                    .orElseThrow(() ->
                            new BusinessException("Segmento de vuelo no encontrado")
                    );

            if (segmentoVuelo.getFechaSalida() == null ||
                    segmentoVuelo.getHoraSalida() == null) {
                continue;
            }

            long cruces = boletoRepository.countBoletosPasajeroMismaFechaHora(
                    pasajero.getId(),
                    segmentoVuelo.getFechaSalida(),
                    segmentoVuelo.getHoraSalida(),
                    estadoActivoId,
                    estadoCanceladoId
            );

            if (cruces > 0) {
                throw new BusinessException(
                        "No se puede seleccionar el vuelo porque el pasajero " +
                                pasajero.getNombreCompleto() +
                                " ya tiene vuelos asignados"
                );
            }
        }
    }

    private Map<Integer, ReservaSegmentoAsientoRequest> mapearSeleccionPorSegmento(
            List<ReservaSegmentoAsientoRequest> seleccionados,
            Integer vueloOperadoId
    ) {

        Map<Integer, ReservaSegmentoAsientoRequest> map = new HashMap<>();

        for (ReservaSegmentoAsientoRequest seleccionado : seleccionados) {

            if (seleccionado == null ||
                    seleccionado.getSegmentoOperadoId() == null) {

                throw new BusinessException("Debe ingresar el segmento para cada asiento");
            }

            SegmentoOperado segmentoOperado = segmentoOperadoRepository
                    .findById(seleccionado.getSegmentoOperadoId())
                    .orElseThrow(() ->
                            new BusinessException("Segmento operado no encontrado: " + seleccionado.getSegmentoOperadoId())
                    );

            if (!Objects.equals(segmentoOperado.getVueloOperadoId(), vueloOperadoId)) {
                throw new BusinessException("El segmento no pertenece al vuelo operado seleccionado");
            }

            if (map.containsKey(seleccionado.getSegmentoOperadoId())) {
                throw new BusinessException("No puede repetir segmentos en la selección de asientos");
            }

            map.put(
                    seleccionado.getSegmentoOperadoId(),
                    seleccionado
            );
        }

        return map;
    }

    private List<ReservaSegmentoAsientoRequest> normalizarSegmentosAsientos(
            ReservaRequest request,
            ReservaPasajeroItemRequest item,
            List<SegmentoOperado> segmentosOperados,
            boolean permiteSegmentosGlobales
    ) {

        if (item.getSegmentosAsientos() != null &&
                !item.getSegmentosAsientos().isEmpty()) {

            return item.getSegmentosAsientos();
        }

        if (request.getSegmentosAsientos() != null &&
                !request.getSegmentosAsientos().isEmpty()) {

            if (!permiteSegmentosGlobales) {
                throw new BusinessException("Para varios pasajeros debe ingresar los asientos dentro de cada pasajero");
            }

            return request.getSegmentosAsientos();
        }

        if (segmentosOperados.size() == 1) {

            SegmentoOperado unico = segmentosOperados.get(0);

            ReservaSegmentoAsientoRequest seleccionado = new ReservaSegmentoAsientoRequest();

            seleccionado.setSegmentoOperadoId(
                    request.getSegmentoOperadoId() != null
                            ? request.getSegmentoOperadoId()
                            : unico.getId()
            );

            seleccionado.setAsientoVueloId(
                    item.getAsientoVueloId() != null
                            ? item.getAsientoVueloId()
                            : request.getAsientoVueloId()
            );

            return List.of(seleccionado);
        }

        throw new BusinessException("Debe ingresar un asiento por cada segmento del vuelo");
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
                request.getVueloOperadoId() == null) {

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
        item.setSegmentosAsientos(request.getSegmentosAsientos());

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

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

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
        pasajero.setEstadoId(estadoActivoId);

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
                    .ifPresent(e ->
                            response.setEstadoReserva(e.getNombre())
                    );
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
                        .ifPresent(e ->
                                response.setEstadoBoleto(e.getNombre())
                        );
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

        long maletasUnicas = equipajes.stream()
                .map(Equipaje::getNumeroMaleta)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        response.setCantidadMaletas((int) maletasUnicas);

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

        List<ReservaBoletoSegmentoResponse> segmentosResponse = segmentos.stream()
                .map(this::mapBoletoSegmento)
                .toList();

        response.setSegmentos(segmentosResponse);

        if (!segmentosResponse.isEmpty()) {

            ReservaBoletoSegmentoResponse primero = segmentosResponse.get(0);

            response.setAsientoVueloId(primero.getAsientoVueloId());
            response.setAsiento(primero.getAsiento());
        }

        return response;
    }

    private ReservaBoletoSegmentoResponse mapBoletoSegmento(
            BoletoSegmento boletoSegmento
    ) {

        ReservaBoletoSegmentoResponse response = new ReservaBoletoSegmentoResponse();

        response.setBoletoSegmentoId(boletoSegmento.getId());
        response.setSegmentoOperadoId(boletoSegmento.getSegmentoOperadoId());
        response.setOrdenSegmento(boletoSegmento.getOrdenSegmento());

        List<BoletoAsiento> asientos = boletoAsientoRepository
                .findByBoletoSegmentoId(boletoSegmento.getId());

        if (asientos.isEmpty()) {
            return response;
        }

        BoletoAsiento boletoAsiento = asientos.get(0);

        response.setAsientoVueloId(boletoAsiento.getAsientoVueloId());
        response.setClaseVueloId(boletoAsiento.getClaseVueloId());

        if (boletoAsiento.getClaseVueloId() != null) {
            claseVueloRepository.findById(boletoAsiento.getClaseVueloId())
                    .ifPresent(clase ->
                            response.setClaseVueloNombre(clase.getNombre())
                    );
        }

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

    private static class PasajeroPlan {

        private final ReservaPasajeroItemRequest item;

        private final Pasajero pasajero;

        private final List<SegmentoAsientoPlan> segmentos;

        private final BigDecimal precioBase;

        private final BigDecimal recargoAsiento;

        private PasajeroPlan(
                ReservaPasajeroItemRequest item,
                Pasajero pasajero,
                List<SegmentoAsientoPlan> segmentos,
                BigDecimal precioBase,
                BigDecimal recargoAsiento
        ) {

            this.item = item;
            this.pasajero = pasajero;
            this.segmentos = segmentos;
            this.precioBase = precioBase;
            this.recargoAsiento = recargoAsiento;
        }
    }

    private static class SegmentoAsientoPlan {

        private final SegmentoOperado segmentoOperado;

        private final AsientoVuelo asientoVuelo;

        private final AsientoUbi asientoUbi;

        private SegmentoAsientoPlan(
                SegmentoOperado segmentoOperado,
                AsientoVuelo asientoVuelo,
                AsientoUbi asientoUbi
        ) {

            this.segmentoOperado = segmentoOperado;
            this.asientoVuelo = asientoVuelo;
            this.asientoUbi = asientoUbi;
        }
    }
}