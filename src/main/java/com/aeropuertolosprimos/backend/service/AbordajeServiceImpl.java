package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AbordajeEquipajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeResponse;
import com.aeropuertolosprimos.backend.dto.AbordajeVueloPendienteResponse;
import com.aeropuertolosprimos.backend.dto.FinalizarAbordajeResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.Abordaje;
import com.aeropuertolosprimos.backend.model.AsientoUbi;
import com.aeropuertolosprimos.backend.model.AsientoVuelo;
import com.aeropuertolosprimos.backend.model.Boleto;
import com.aeropuertolosprimos.backend.model.BoletoAsiento;
import com.aeropuertolosprimos.backend.model.BoletoSegmento;
import com.aeropuertolosprimos.backend.model.Equipaje;
import com.aeropuertolosprimos.backend.model.EstadoAbordajeVuelo;
import com.aeropuertolosprimos.backend.model.EstadoAsiento;
import com.aeropuertolosprimos.backend.model.EstadoBoleto;
import com.aeropuertolosprimos.backend.model.EstadoCheckIn;
import com.aeropuertolosprimos.backend.model.EstadoEquipaje;
import com.aeropuertolosprimos.backend.model.EstadoPago;
import com.aeropuertolosprimos.backend.model.EstadoVuelo;
import com.aeropuertolosprimos.backend.model.Pago;
import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.model.PuertaEmbarque;
import com.aeropuertolosprimos.backend.model.SegmentoOperado;
import com.aeropuertolosprimos.backend.model.TipoEquipaje;
import com.aeropuertolosprimos.backend.model.VueloOperado;
import com.aeropuertolosprimos.backend.model.VueloProgramado;
import com.aeropuertolosprimos.backend.repository.AbordajeRepository;
import com.aeropuertolosprimos.backend.repository.AsientoUbiRepository;
import com.aeropuertolosprimos.backend.repository.AsientoVueloRepository;
import com.aeropuertolosprimos.backend.repository.BoletoAsientoRepository;
import com.aeropuertolosprimos.backend.repository.BoletoRepository;
import com.aeropuertolosprimos.backend.repository.BoletoSegmentoRepository;
import com.aeropuertolosprimos.backend.repository.CheckInRepository;
import com.aeropuertolosprimos.backend.repository.EquipajeRepository;
import com.aeropuertolosprimos.backend.repository.EstadoAbordajeVueloRepository;
import com.aeropuertolosprimos.backend.repository.EstadoAsientoRepository;
import com.aeropuertolosprimos.backend.repository.EstadoBoletoRepository;
import com.aeropuertolosprimos.backend.repository.EstadoCheckInRepository;
import com.aeropuertolosprimos.backend.repository.EstadoEquipajeRepository;
import com.aeropuertolosprimos.backend.repository.EstadoPagoRepository;
import com.aeropuertolosprimos.backend.repository.EstadoVueloRepository;
import com.aeropuertolosprimos.backend.repository.PagoRepository;
import com.aeropuertolosprimos.backend.repository.PasajeroRepository;
import com.aeropuertolosprimos.backend.repository.PuertaEmbarqueRepository;
import com.aeropuertolosprimos.backend.repository.SegmentoOperadoRepository;
import com.aeropuertolosprimos.backend.repository.TipoEquipajeRepository;
import com.aeropuertolosprimos.backend.repository.VueloOperadoRepository;
import com.aeropuertolosprimos.backend.repository.VueloProgramadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AbordajeServiceImpl implements AbordajeService {

    private static final BigDecimal PESO_MAXIMO_MALETA = new BigDecimal("23.00");
    private static final BigDecimal RECARGO_POR_KG_EXCEDENTE = new BigDecimal("10.00");

    private static final String ESTADO_BOLETO_PENDIENTE = "PENDIENTE_ABORDAR";
    private static final String ESTADO_BOLETO_ABORDADO = "ABORDADO";
    private static final String ESTADO_BOLETO_CANCELADO = "CANCELADO";

    private static final String ESTADO_ASIENTO_OCUPADO = "OCUPADO";
    private static final String ESTADO_ASIENTO_BLOQUEADO = "BLOQUEADO";

    private static final String ESTADO_EQUIPAJE_REGISTRADO = "REGISTRADO";
    private static final String ESTADO_EQUIPAJE_ABORDADO = "ABORDADO";
    private static final String ESTADO_EQUIPAJE_CANCELADO = "CANCELADO";

    private static final String ESTADO_CHECKIN_REALIZADO = "REALIZADO";

    private static final String ESTADO_PAGO_PAGADO = "PAGADO";

    private static final String ESTADO_ABORDAJE_ABORDADO = "ABORDADO";
    private static final String ESTADO_ABORDAJE_CANCELADO = "CANCELADO";

    private static final String ESTADO_VUELO_PROGRAMADO = "PROGRAMADO";
    private static final String ESTADO_VUELO_ABORDANDO = "ABORDANDO";
    private static final String ESTADO_VUELO_EN_VUELO = "EN_VUELO";

    private static final String TIPO_EQUIPAJE_MALETA = "MALETA";

    private final PasajeroRepository pasajeroRepository;
    private final BoletoRepository boletoRepository;
    private final BoletoSegmentoRepository boletoSegmentoRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final EquipajeRepository equipajeRepository;

    private final VueloOperadoRepository vueloOperadoRepository;
    private final VueloProgramadoRepository vueloProgramadoRepository;
    private final SegmentoOperadoRepository segmentoOperadoRepository;

    private final AsientoVueloRepository asientoVueloRepository;
    private final AsientoUbiRepository asientoUbiRepository;

    private final EstadoBoletoRepository estadoBoletoRepository;
    private final EstadoAsientoRepository estadoAsientoRepository;
    private final EstadoEquipajeRepository estadoEquipajeRepository;
    private final TipoEquipajeRepository tipoEquipajeRepository;
    private final EstadoVueloRepository estadoVueloRepository;

    private final AbordajeRepository abordajeRepository;
    private final EstadoAbordajeVueloRepository estadoAbordajeVueloRepository;
    private final PuertaEmbarqueRepository puertaEmbarqueRepository;

    private final CheckInRepository checkInRepository;
    private final EstadoCheckInRepository estadoCheckInRepository;

    private final PagoRepository pagoRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final PagoService pagoService;

    private final CatalogoEstadoService catalogoEstadoService;

    @Override
    @Transactional(readOnly = true)
    public List<AbordajeVueloPendienteResponse> listarVuelosPendientes(
            Integer aerolineaId
    ) {

        if (aerolineaId == null) {
            throw new BusinessException("No tiene una aerolínea asignada");
        }

        List<String> estadosAbordaje = List.of(
                ESTADO_VUELO_PROGRAMADO.toLowerCase(),
                ESTADO_VUELO_ABORDANDO.toLowerCase()
        );

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        List<AbordajeVueloPendienteResponse> vuelos = abordajeRepository
                .listarVuelosPendientesParaAbordaje(
                        aerolineaId,
                        estadosAbordaje,
                        estadoActivoId
                );

        if (vuelos.isEmpty()) {
            throw new BusinessException("No hay vuelos disponibles");
        }

        return vuelos;
    }

    @Override
    @Transactional(readOnly = true)
    public AbordajeResponse buscar(
            Integer vueloOperadoId,
            String pasaporte,
            Integer segmentoOperadoId
    ) {

        ContextoAbordaje contexto = resolverContextoAbordaje(
                vueloOperadoId,
                pasaporte,
                segmentoOperadoId
        );

        validarReservaPagadaParaAbordaje(
                contexto.boleto.getReservaId()
        );

        validarCheckInObligatorio(
                contexto.boletoSegmento
        );

        return mapResponse(
                contexto,
                null,
                contexto.boleto.getRecargoEquipaje(),
                false,
                null,
                "Boleto encontrado y check-in validado para el segmento actual"
        );
    }

    @Override
    @Transactional
    public AbordajeResponse registrarAbordaje(
            AbordajeRequest request
    ) {

        validarRequest(request);

        ContextoAbordaje contexto = resolverContextoAbordaje(
                request.getVueloOperadoId(),
                request.getPasaporte(),
                request.getSegmentoOperadoId()
        );

        validarReservaPagadaParaAbordaje(
                contexto.boleto.getReservaId()
        );

        validarCheckInObligatorio(
                contexto.boletoSegmento
        );

        EstadoBoleto estadoPendiente = obtenerEstadoBoleto(
                ESTADO_BOLETO_PENDIENTE
        );

        EstadoBoleto estadoAbordado = obtenerEstadoBoleto(
                ESTADO_BOLETO_ABORDADO
        );

        EstadoAbordajeVuelo estadoAbordajeAbordado = obtenerEstadoAbordaje(
                ESTADO_ABORDAJE_ABORDADO
        );

        if (!Objects.equals(contexto.boletoSegmento.getEstadoBoletoId(), estadoPendiente.getId())) {
            throw new BusinessException("El segmento del boleto no está pendiente de abordar");
        }

        BigDecimal recargoPorPeso = calcularYActualizarEquipaje(
                contexto,
                request
        );

        if (recargoPorPeso.compareTo(BigDecimal.ZERO) > 0) {

            Pago pagoPagado = buscarPagoRecargoPagado(
                    contexto.boleto.getReservaId(),
                    recargoPorPeso
            );

            if (pagoPagado == null) {

                var pagoPendiente = pagoService.crearPagoRecargoEquipajePendiente(
                        contexto.boleto.getReservaId(),
                        recargoPorPeso
                );

                return mapResponse(
                        contexto,
                        request.getCantidadMaletasPresentadas(),
                        recargoPorPeso,
                        true,
                        pagoPendiente.getId(),
                        "Se generó pago pendiente por recargo de equipaje. Debe pagarse antes de abordar."
                );
            }
        }

        marcarSegmentoComoAbordado(
                contexto,
                estadoAbordado
        );

        actualizarEstadoBoletoPrincipalSiAplica(
                contexto.boleto,
                estadoAbordado
        );

        ocuparAsientoSegmentoActual(
                contexto
        );

        marcarEquipajeAbordado(
                contexto.boleto,
                contexto.segmentoOperado.getId()
        );

        registrarMovimientoAbordaje(
                contexto,
                request.getEmpleadoId(),
                request.getTipoAbordaje(),
                estadoAbordajeAbordado,
                true
        );

        Boleto boletoActualizado = boletoRepository.findById(contexto.boleto.getId())
                .orElseThrow(() -> new BusinessException("Boleto no encontrado"));

        BoletoSegmento boletoSegmentoActualizado = boletoSegmentoRepository.findById(contexto.boletoSegmento.getId())
                .orElseThrow(() -> new BusinessException("Segmento de boleto no encontrado"));

        ContextoAbordaje contextoActualizado = new ContextoAbordaje(
                boletoActualizado,
                boletoSegmentoActualizado,
                contexto.segmentoOperado,
                contexto.vueloOperado
        );

        return mapResponse(
                contextoActualizado,
                request.getCantidadMaletasPresentadas(),
                recargoPorPeso,
                false,
                null,
                "Pasajero abordado correctamente en el segmento actual"
        );
    }

    @Override
    @Transactional
    public FinalizarAbordajeResponse finalizarAbordaje(
            Integer vueloOperadoId,
            Integer segmentoOperadoId
    ) {

        if (vueloOperadoId == null) {
            throw new BusinessException("Debe ingresar el vuelo operado");
        }

        VueloOperado vueloOperado = vueloOperadoRepository.findById(vueloOperadoId)
                .orElseThrow(() -> new BusinessException("Vuelo operado no encontrado"));

        SegmentoOperado segmentoActual = resolverSegmentoActual(
                vueloOperado,
                segmentoOperadoId
        );

        EstadoBoleto estadoPendiente = obtenerEstadoBoleto(
                ESTADO_BOLETO_PENDIENTE
        );

        EstadoBoleto estadoAbordado = obtenerEstadoBoleto(
                ESTADO_BOLETO_ABORDADO
        );

        EstadoBoleto estadoCancelado = obtenerEstadoBoleto(
                ESTADO_BOLETO_CANCELADO
        );

        EstadoAsiento estadoBloqueado = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_BLOQUEADO)
                .orElseThrow(() -> new BusinessException("Estado de asiento BLOQUEADO no encontrado"));

        EstadoAbordajeVuelo estadoAbordajeCancelado = obtenerEstadoAbordaje(
                ESTADO_ABORDAJE_CANCELADO
        );

        EstadoVuelo estadoEnVuelo = estadoVueloRepository
                .findByNombreIgnoreCase(ESTADO_VUELO_EN_VUELO)
                .orElseThrow(() -> new BusinessException("Estado de vuelo EN_VUELO no encontrado"));

        Integer estadoActivoId = obtenerEstadoActivoId();

        List<Boleto> boletos = boletoRepository
                .findByVueloOperadoIdAndEstadoId(
                        vueloOperadoId,
                        estadoActivoId
                );

        int abordados = 0;
        int cancelados = 0;

        for (Boleto boleto : boletos) {

            List<BoletoSegmento> segmentosBoleto = boletoSegmentoRepository
                    .findByBoletoIdOrderByOrdenSegmentoAsc(
                            boleto.getId()
                    );

            BoletoSegmento boletoSegmentoActual = segmentosBoleto.stream()
                    .filter(segmento ->
                            Objects.equals(segmento.getSegmentoOperadoId(), segmentoActual.getId())
                    )
                    .findFirst()
                    .orElse(null);

            if (boletoSegmentoActual == null) {
                continue;
            }

            if (Objects.equals(boletoSegmentoActual.getEstadoBoletoId(), estadoAbordado.getId())) {
                abordados++;
                continue;
            }

            if (Objects.equals(boletoSegmentoActual.getEstadoBoletoId(), estadoPendiente.getId())) {

                cancelarSegmentosPendientesDesdeOrden(
                        boleto,
                        segmentosBoleto,
                        segmentoActual,
                        estadoPendiente,
                        estadoCancelado,
                        estadoBloqueado
                );

                registrarMovimientoAbordajeCancelado(
                        boleto,
                        boletoSegmentoActual,
                        vueloOperado,
                        segmentoActual,
                        estadoAbordajeCancelado
                );

                boleto.setEstadoBoletoId(
                        estadoCancelado.getId()
                );

                boletoRepository.save(boleto);

                cancelados++;
            }
        }

        segmentoActual.setEstadoVueloId(
                estadoEnVuelo.getId()
        );

        if (segmentoActual.getFechaSalidaReal() == null) {
            segmentoActual.setFechaSalidaReal(LocalDate.now());
        }

        if (segmentoActual.getHoraSalidaReal() == null) {
            segmentoActual.setHoraSalidaReal(LocalTime.now());
        }

        segmentoOperadoRepository.save(segmentoActual);

        vueloOperado.setEstadoVueloId(
                estadoEnVuelo.getId()
        );

        vueloOperadoRepository.save(vueloOperado);

        FinalizarAbordajeResponse response = new FinalizarAbordajeResponse();

        response.setVueloOperadoId(vueloOperado.getId());
        response.setSegmentoOperadoId(segmentoActual.getId());
        response.setOrdenSegmento(segmentoActual.getOrdenSegmento());
        response.setSegmentoActualOrden(vueloOperado.getSegmentoActualOrden());
        response.setCantidadSegmentos(vueloOperado.getCantidadSegmentos());
        response.setEstadoVuelo(estadoEnVuelo.getNombre());
        response.setBoletosAbordados(abordados);
        response.setBoletosCancelados(cancelados);
        response.setMensaje("Se completó el abordaje del segmento actual y el vuelo pasó a EN_VUELO");

        return response;
    }

    private void validarRequest(
            AbordajeRequest request
    ) {

        if (request == null ||
                request.getVueloOperadoId() == null ||
                request.getPasaporte() == null ||
                request.getPasaporte().isBlank() ||
                request.getCantidadMaletasPresentadas() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getCantidadMaletasPresentadas() < 0) {
            throw new BusinessException("La cantidad de maletas no puede ser negativa");
        }
    }

    private ContextoAbordaje resolverContextoAbordaje(
            Integer vueloOperadoId,
            String pasaporte,
            Integer segmentoOperadoId
    ) {

        if (vueloOperadoId == null ||
                pasaporte == null ||
                pasaporte.isBlank()) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        VueloOperado vueloOperado = vueloOperadoRepository.findById(vueloOperadoId)
                .orElseThrow(() -> new BusinessException("Vuelo operado no encontrado"));

        SegmentoOperado segmentoActual = resolverSegmentoActual(
                vueloOperado,
                segmentoOperadoId
        );

        Pasajero pasajero = pasajeroRepository
                .findByPasaporte(pasaporte.trim())
                .orElseThrow(() -> new BusinessException("El pasajero no se encuentra registrado en el vuelo"));

        EstadoBoleto estadoCancelado = obtenerEstadoBoleto(
                ESTADO_BOLETO_CANCELADO
        );

        Integer estadoActivoId = obtenerEstadoActivoId();

        Boleto boleto = boletoRepository
                .findFirstByPasajeroIdAndVueloOperadoIdAndEstadoBoletoIdNotAndEstadoIdOrderByIdDesc(
                        pasajero.getId(),
                        vueloOperadoId,
                        estadoCancelado.getId(),
                        estadoActivoId
                )
                .orElseThrow(() ->
                        new BusinessException("El pasajero no se encuentra registrado en el vuelo")
                );

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(
                        boleto.getId()
                );

        BoletoSegmento boletoSegmentoActual = segmentos.stream()
                .filter(segmento ->
                        Objects.equals(segmento.getSegmentoOperadoId(), segmentoActual.getId())
                )
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("El pasajero no tiene boleto para el segmento actual del vuelo")
                );

        return new ContextoAbordaje(
                boleto,
                boletoSegmentoActual,
                segmentoActual,
                vueloOperado
        );
    }

    private SegmentoOperado resolverSegmentoActual(
            VueloOperado vueloOperado,
            Integer segmentoOperadoId
    ) {

        SegmentoOperado segmento;

        if (segmentoOperadoId != null) {
            segmento = segmentoOperadoRepository.findById(segmentoOperadoId)
                    .orElseThrow(() -> new BusinessException("Segmento operado no encontrado"));

            if (!Objects.equals(segmento.getVueloOperadoId(), vueloOperado.getId())) {
                throw new BusinessException("El segmento no pertenece al vuelo operado seleccionado");
            }

            Integer ordenActual = vueloOperado.getSegmentoActualOrden() != null
                    ? vueloOperado.getSegmentoActualOrden()
                    : 1;

            if (!Objects.equals(segmento.getOrdenSegmento(), ordenActual)) {
                throw new BusinessException("Solo se puede abordar el segmento actual del vuelo");
            }

            return segmento;
        }

        return obtenerSegmentoActual(
                vueloOperado
        );
    }

    private void validarCheckInObligatorio(
            BoletoSegmento boletoSegmento
    ) {

        EstadoCheckIn estadoRealizado = estadoCheckInRepository
                .findByNombreIgnoreCase(ESTADO_CHECKIN_REALIZADO)
                .orElseThrow(() -> new BusinessException("Estado de check-in REALIZADO no encontrado"));

        boolean tieneCheckIn = checkInRepository.existsByBoletoSegmentoIdAndEstadoCheckinId(
                boletoSegmento.getId(),
                estadoRealizado.getId()
        );

        if (!tieneCheckIn) {
            throw new BusinessException("El pasajero debe realizar check-in antes de abordar.");
        }
    }

    private void validarReservaPagadaParaAbordaje(
            Integer reservaId
    ) {

        if (reservaId == null) {
            throw new BusinessException("La reserva debe estar pagada para poder abordar.");
        }

        EstadoPago estadoPagado = estadoPagoRepository
                .findByNombreIgnoreCase(ESTADO_PAGO_PAGADO)
                .orElseThrow(() -> new BusinessException("Estado de pago PAGADO no encontrado"));

        boolean tienePagoReserva = pagoRepository.findByReservaIdOrderByIdDesc(reservaId)
                .stream()
                .anyMatch(pago ->
                        Objects.equals(pago.getEstadoPagoId(), estadoPagado.getId()) &&
                                valor(pago.getRecargoEquipaje()).compareTo(BigDecimal.ZERO) == 0
                );

        if (!tienePagoReserva) {
            throw new BusinessException("La reserva debe estar pagada para poder abordar.");
        }
    }

    private BigDecimal valor(
            BigDecimal value
    ) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal calcularYActualizarEquipaje(
            ContextoAbordaje contexto,
            AbordajeRequest request
    ) {

        int cantidadPresentada = request.getCantidadMaletasPresentadas();

        if (cantidadPresentada > 0 &&
                (request.getEquipajes() == null || request.getEquipajes().isEmpty())) {

            throw new BusinessException("Debe ingresar el peso del equipaje presentado");
        }

        if (cantidadPresentada > 0 &&
                request.getEquipajes().size() != cantidadPresentada) {

            throw new BusinessException("La cantidad de pesos ingresados no coincide con la cantidad de maletas presentadas");
        }

        TipoEquipaje tipoMaleta = tipoEquipajeRepository
                .findByNombreIgnoreCase(TIPO_EQUIPAJE_MALETA)
                .orElseThrow(() -> new BusinessException("Tipo de equipaje MALETA no encontrado"));

        EstadoEquipaje estadoRegistrado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_REGISTRADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje REGISTRADO no encontrado"));

        EstadoEquipaje estadoCancelado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje CANCELADO no encontrado"));

        List<Equipaje> equipajes = new ArrayList<>(
                equipajeRepository
                        .findByBoletoIdOrderByNumeroMaletaAsc(
                                contexto.boleto.getId()
                        )
                        .stream()
                        .filter(equipaje ->
                                equipaje.getSegmentoOperadoId() == null ||
                                        Objects.equals(
                                                equipaje.getSegmentoOperadoId(),
                                                contexto.segmentoOperado.getId()
                                        )
                        )
                        .toList()
        );

        while (equipajes.size() < cantidadPresentada) {

            Equipaje equipaje = new Equipaje();

            equipaje.setBoletoId(contexto.boleto.getId());
            equipaje.setPasajeroId(contexto.boleto.getPasajeroId());
            equipaje.setSegmentoOperadoId(contexto.segmentoOperado.getId());
            equipaje.setTipoEquipajeId(tipoMaleta.getId());
            equipaje.setEstadoEquipajeId(estadoRegistrado.getId());
            equipaje.setNumeroMaleta(equipajes.size() + 1);
            equipaje.setMontoRecargo(BigDecimal.ZERO);

            equipaje = equipajeRepository.save(equipaje);

            equipajes.add(equipaje);
        }

        List<AbordajeEquipajeRequest> pesos = request.getEquipajes() == null
                ? List.of()
                : request.getEquipajes()
                .stream()
                .sorted(Comparator.comparing(
                        item -> item.getNumeroMaleta() != null
                                ? item.getNumeroMaleta()
                                : 0
                ))
                .toList();

        BigDecimal recargoTotal = BigDecimal.ZERO;

        for (int i = 0; i < equipajes.size(); i++) {

            Equipaje equipaje = equipajes.get(i);

            if (i >= cantidadPresentada) {
                equipaje.setEstadoEquipajeId(estadoCancelado.getId());
                equipajeRepository.save(equipaje);
                continue;
            }

            AbordajeEquipajeRequest pesoRequest = pesos.get(i);

            if (pesoRequest.getPeso() == null) {
                throw new BusinessException("Debe ingresar el peso de todas las maletas");
            }

            BigDecimal peso = pesoRequest.getPeso()
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal excedente = peso.subtract(PESO_MAXIMO_MALETA);

            BigDecimal recargoMaleta = BigDecimal.ZERO;

            if (excedente.compareTo(BigDecimal.ZERO) > 0) {
                recargoMaleta = excedente
                        .multiply(RECARGO_POR_KG_EXCEDENTE)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            equipaje.setPeso(peso);
            equipaje.setMontoRecargo(recargoMaleta);
            equipaje.setSegmentoOperadoId(contexto.segmentoOperado.getId());

            equipajeRepository.save(equipaje);

            recargoTotal = recargoTotal.add(recargoMaleta);
        }

        contexto.boleto.setRecargoEquipaje(recargoTotal);

        BigDecimal precioBase = contexto.boleto.getPrecioBase() != null
                ? contexto.boleto.getPrecioBase()
                : BigDecimal.ZERO;

        contexto.boleto.setTotal(
                precioBase.add(recargoTotal)
        );

        contexto.boleto = boletoRepository.save(contexto.boleto);

        return recargoTotal;
    }

    private Pago buscarPagoRecargoPagado(
            Integer reservaId,
            BigDecimal recargo
    ) {

        if (reservaId == null ||
                recargo == null ||
                recargo.compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        EstadoPago estadoPagado = estadoPagoRepository
                .findByNombreIgnoreCase(ESTADO_PAGO_PAGADO)
                .orElseThrow(() -> new BusinessException("Estado de pago PAGADO no encontrado"));

        return pagoRepository
                .findFirstByReservaIdAndRecargoEquipajeAndEstadoPagoIdOrderByIdDesc(
                        reservaId,
                        recargo,
                        estadoPagado.getId()
                )
                .orElse(null);
    }

    private void marcarSegmentoComoAbordado(
            ContextoAbordaje contexto,
            EstadoBoleto estadoAbordado
    ) {

        contexto.boletoSegmento.setEstadoBoletoId(
                estadoAbordado.getId()
        );

        boletoSegmentoRepository.save(
                contexto.boletoSegmento
        );
    }

    private void actualizarEstadoBoletoPrincipalSiAplica(
            Boleto boleto,
            EstadoBoleto estadoAbordado
    ) {

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(
                        boleto.getId()
                );

        boolean todosAbordados = segmentos.stream()
                .allMatch(segmento ->
                        Objects.equals(
                                segmento.getEstadoBoletoId(),
                                estadoAbordado.getId()
                        )
                );

        if (todosAbordados) {
            boleto.setEstadoBoletoId(
                    estadoAbordado.getId()
            );

            boletoRepository.save(boleto);
        }
    }

    private void ocuparAsientoSegmentoActual(
            ContextoAbordaje contexto
    ) {

        EstadoAsiento estadoOcupado = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_OCUPADO)
                .orElseThrow(() -> new BusinessException("Estado de asiento OCUPADO no encontrado"));

        List<BoletoAsiento> asientos = boletoAsientoRepository
                .findByBoletoSegmentoId(
                        contexto.boletoSegmento.getId()
                );

        for (BoletoAsiento boletoAsiento : asientos) {

            if (boletoAsiento.getAsientoVueloId() == null) {
                continue;
            }

            asientoVueloRepository.findById(boletoAsiento.getAsientoVueloId())
                    .ifPresent(asientoVuelo -> {
                        asientoVuelo.setEstadoAsientoId(estadoOcupado.getId());
                        asientoVueloRepository.save(asientoVuelo);
                    });
        }
    }

    private void marcarEquipajeAbordado(
            Boleto boleto,
            Integer segmentoOperadoId
    ) {

        EstadoEquipaje estadoAbordado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_ABORDADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje ABORDADO no encontrado"));

        List<Equipaje> equipajes = equipajeRepository
                .findByBoletoId(
                        boleto.getId()
                );

        for (Equipaje equipaje : equipajes) {

            if (Objects.equals(equipaje.getSegmentoOperadoId(), segmentoOperadoId)) {
                equipaje.setEstadoEquipajeId(
                        estadoAbordado.getId()
                );

                equipajeRepository.save(equipaje);
            }
        }
    }

    private void cancelarSegmentosPendientesDesdeOrden(
            Boleto boleto,
            List<BoletoSegmento> segmentosBoleto,
            SegmentoOperado segmentoActual,
            EstadoBoleto estadoPendiente,
            EstadoBoleto estadoCancelado,
            EstadoAsiento estadoBloqueado
    ) {

        Integer ordenActual = segmentoActual.getOrdenSegmento() != null
                ? segmentoActual.getOrdenSegmento()
                : 1;

        for (BoletoSegmento segmento : segmentosBoleto) {

            Integer ordenSegmento = segmento.getOrdenSegmento() != null
                    ? segmento.getOrdenSegmento()
                    : 1;

            if (ordenSegmento < ordenActual) {
                continue;
            }

            if (!Objects.equals(segmento.getEstadoBoletoId(), estadoPendiente.getId())) {
                continue;
            }

            segmento.setEstadoBoletoId(
                    estadoCancelado.getId()
            );

            boletoSegmentoRepository.save(segmento);

            cancelarAsientosDeSegmento(
                    segmento,
                    estadoBloqueado
            );

            cancelarEquipajeDeSegmento(
                    boleto,
                    segmento.getSegmentoOperadoId()
            );
        }
    }

    private void cancelarAsientosDeSegmento(
            BoletoSegmento boletoSegmento,
            EstadoAsiento estadoBloqueado
    ) {

        List<BoletoAsiento> asientos = boletoAsientoRepository
                .findByBoletoSegmentoId(
                        boletoSegmento.getId()
                );

        for (BoletoAsiento boletoAsiento : asientos) {

            if (boletoAsiento.getAsientoVueloId() == null) {
                continue;
            }

            asientoVueloRepository.findById(boletoAsiento.getAsientoVueloId())
                    .ifPresent(asientoVuelo -> {
                        asientoVuelo.setEstadoAsientoId(estadoBloqueado.getId());
                        asientoVueloRepository.save(asientoVuelo);
                    });
        }
    }

    private void cancelarEquipajeDeSegmento(
            Boleto boleto,
            Integer segmentoOperadoId
    ) {

        EstadoEquipaje estadoCancelado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje CANCELADO no encontrado"));

        List<Equipaje> equipajes = equipajeRepository
                .findByBoletoId(
                        boleto.getId()
                );

        for (Equipaje equipaje : equipajes) {

            if (Objects.equals(equipaje.getSegmentoOperadoId(), segmentoOperadoId)) {
                equipaje.setEstadoEquipajeId(
                        estadoCancelado.getId()
                );

                equipajeRepository.save(equipaje);
            }
        }
    }

    private void registrarMovimientoAbordaje(
            ContextoAbordaje contexto,
            Integer empleadoId,
            String tipoAbordaje,
            EstadoAbordajeVuelo estadoAbordaje,
            Boolean boletoValidado
    ) {

        if (abordajeRepository.existsByBoletoSegmentoIdAndEstadoAbordajeVueloId(
                contexto.boletoSegmento.getId(),
                estadoAbordaje.getId()
        )) {
            return;
        }

        Abordaje abordaje = new Abordaje();

        abordaje.setBoletoSegmentoId(contexto.boletoSegmento.getId());
        abordaje.setEmpleadoId(empleadoId);
        abordaje.setPuertaEmbarqueId(
                obtenerPuertaEmbarqueId(contexto)
        );
        abordaje.setEstadoAbordajeVueloId(estadoAbordaje.getId());
        abordaje.setTipoAbordaje(
                tipoAbordaje != null && !tipoAbordaje.isBlank()
                        ? tipoAbordaje.trim().toUpperCase()
                        : "MANUAL"
        );
        abordaje.setFechaAbordaje(LocalDate.now());
        abordaje.setHoraAbordaje(LocalTime.now());
        abordaje.setBoletoValidado(boletoValidado);

        abordajeRepository.save(abordaje);
    }

    private void registrarMovimientoAbordajeCancelado(
            Boleto boleto,
            BoletoSegmento boletoSegmento,
            VueloOperado vueloOperado,
            SegmentoOperado segmentoOperado,
            EstadoAbordajeVuelo estadoAbordaje
    ) {

        if (abordajeRepository.existsByBoletoSegmentoIdAndEstadoAbordajeVueloId(
                boletoSegmento.getId(),
                estadoAbordaje.getId()
        )) {
            return;
        }

        ContextoAbordaje contexto = new ContextoAbordaje(
                boleto,
                boletoSegmento,
                segmentoOperado,
                vueloOperado
        );

        Abordaje abordaje = new Abordaje();

        abordaje.setBoletoSegmentoId(boletoSegmento.getId());
        abordaje.setEmpleadoId(null);
        abordaje.setPuertaEmbarqueId(
                obtenerPuertaEmbarqueId(contexto)
        );
        abordaje.setEstadoAbordajeVueloId(estadoAbordaje.getId());
        abordaje.setTipoAbordaje("AUTOMATICO");
        abordaje.setFechaAbordaje(LocalDate.now());
        abordaje.setHoraAbordaje(LocalTime.now());
        abordaje.setBoletoValidado(false);

        abordajeRepository.save(abordaje);
    }

    private Integer obtenerPuertaEmbarqueId(
            ContextoAbordaje contexto
    ) {

        if (contexto.segmentoOperado.getOrdenSegmento() != null &&
                contexto.segmentoOperado.getOrdenSegmento() > 1) {
            return null;
        }

        VueloProgramado vueloProgramado = vueloProgramadoRepository
                .findById(contexto.vueloOperado.getVueloProgramadoId())
                .orElse(null);

        if (vueloProgramado == null ||
                vueloProgramado.getPuertaEmbarqueSalida() == null ||
                vueloProgramado.getPuertaEmbarqueSalida().isBlank()) {
            return null;
        }

        Integer estadoActivoId = obtenerEstadoActivoId();

        return puertaEmbarqueRepository
                .findFirstByAeropuertoIdAndCodigoIgnoreCaseAndEstadoId(
                        vueloProgramado.getAeropuertoSalidaId(),
                        vueloProgramado.getPuertaEmbarqueSalida().trim(),
                        estadoActivoId
                )
                .map(PuertaEmbarque::getId)
                .orElse(null);
    }

    private EstadoBoleto obtenerEstadoBoleto(
            String nombre
    ) {

        return estadoBoletoRepository
                .findByNombreIgnoreCase(nombre)
                .orElseThrow(() ->
                        new BusinessException("Estado de boleto no encontrado: " + nombre)
                );
    }

    private EstadoAbordajeVuelo obtenerEstadoAbordaje(
            String nombre
    ) {

        return estadoAbordajeVueloRepository
                .findByNombreIgnoreCase(nombre)
                .orElseThrow(() ->
                        new BusinessException("Estado de abordaje no encontrado: " + nombre)
                );
    }

    private Integer obtenerEstadoActivoId() {
        return catalogoEstadoService.obtenerActivoId();
    }

    private SegmentoOperado obtenerSegmentoActual(
            VueloOperado vueloOperado
    ) {

        Integer ordenActual = vueloOperado.getSegmentoActualOrden() != null
                ? vueloOperado.getSegmentoActualOrden()
                : 1;

        return segmentoOperadoRepository
                .findByVueloOperadoIdAndOrdenSegmento(
                        vueloOperado.getId(),
                        ordenActual
                )
                .orElseThrow(() -> new BusinessException("Segmento actual no encontrado"));
    }

    private String obtenerAsiento(
            BoletoSegmento boletoSegmento
    ) {

        List<BoletoAsiento> asientos = boletoAsientoRepository
                .findByBoletoSegmentoId(
                        boletoSegmento.getId()
                );

        if (asientos.isEmpty()) {
            return null;
        }

        Integer asientoVueloId = asientos.get(0).getAsientoVueloId();

        if (asientoVueloId == null) {
            return null;
        }

        AsientoVuelo asientoVuelo = asientoVueloRepository.findById(asientoVueloId)
                .orElse(null);

        if (asientoVuelo == null || asientoVuelo.getCodigoAsientoSistema() == null) {
            return null;
        }

        return asientoUbiRepository
                .findFirstByCodigoAsientoSistemaOrderByIdAsc(
                        asientoVuelo.getCodigoAsientoSistema()
                )
                .map(AsientoUbi::getNumeroAsiento)
                .orElse(null);
    }

    private AbordajeResponse mapResponse(
            ContextoAbordaje contexto,
            Integer cantidadMaletasPresentadas,
            BigDecimal recargo,
            Boolean requierePagoRecargo,
            Integer pagoRecargoId,
            String mensaje
    ) {

        AbordajeResponse response = new AbordajeResponse();

        response.setBoletoId(contexto.boleto.getId());
        response.setCodigoBoleto(contexto.boleto.getCodigoBoleto());
        response.setCodigoPaseAbordar(contexto.boleto.getCodigoPaseAbordar());
        response.setPasajeroId(contexto.boleto.getPasajeroId());
        response.setVueloOperadoId(contexto.boleto.getVueloOperadoId());

        response.setBoletoSegmentoId(contexto.boletoSegmento.getId());
        response.setSegmentoOperadoId(contexto.segmentoOperado.getId());
        response.setOrdenSegmento(contexto.boletoSegmento.getOrdenSegmento());
        response.setSegmentoActualOrden(contexto.vueloOperado.getSegmentoActualOrden());
        response.setCantidadSegmentos(contexto.vueloOperado.getCantidadSegmentos());

        response.setRecargoEquipaje(
                recargo != null ? recargo : BigDecimal.ZERO
        );
        response.setRequierePagoRecargo(requierePagoRecargo);
        response.setPagoRecargoId(pagoRecargoId);
        response.setTotal(contexto.boleto.getTotal());
        response.setMensaje(mensaje);
        response.setAsiento(obtenerAsiento(contexto.boletoSegmento));

        pasajeroRepository.findById(contexto.boleto.getPasajeroId())
                .ifPresent(pasajero -> {
                    response.setNombrePasajero(pasajero.getNombreCompleto());
                    response.setPasaporte(pasajero.getPasaporte());
                });

        if (contexto.boleto.getEstadoBoletoId() != null) {
            estadoBoletoRepository.findById(contexto.boleto.getEstadoBoletoId())
                    .ifPresent(estado ->
                            response.setEstadoBoleto(estado.getNombre())
                    );
        }

        if (contexto.boletoSegmento.getEstadoBoletoId() != null) {
            estadoBoletoRepository.findById(contexto.boletoSegmento.getEstadoBoletoId())
                    .ifPresent(estado ->
                            response.setEstadoBoletoSegmento(estado.getNombre())
                    );
        }

        List<Equipaje> equipajes = equipajeRepository
                .findByBoletoId(contexto.boleto.getId());

        response.setCantidadMaletasRegistradas(
                equipajes.size()
        );

        response.setCantidadMaletasPresentadas(
                cantidadMaletasPresentadas
        );

        if (pagoRecargoId != null) {
            pagoRepository.findById(pagoRecargoId)
                    .ifPresent(pago -> {
                        if (pago.getEstadoPagoId() != null) {
                            estadoPagoRepository.findById(pago.getEstadoPagoId())
                                    .ifPresent(estado ->
                                            response.setEstadoPagoRecargo(estado.getNombre())
                                    );
                        }
                    });
        }

        return response;
    }

    private static class ContextoAbordaje {

        private Boleto boleto;

        private final BoletoSegmento boletoSegmento;

        private final SegmentoOperado segmentoOperado;

        private final VueloOperado vueloOperado;

        private ContextoAbordaje(
                Boleto boleto,
                BoletoSegmento boletoSegmento,
                SegmentoOperado segmentoOperado,
                VueloOperado vueloOperado
        ) {
            this.boleto = boleto;
            this.boletoSegmento = boletoSegmento;
            this.segmentoOperado = segmentoOperado;
            this.vueloOperado = vueloOperado;
        }
    }
}
