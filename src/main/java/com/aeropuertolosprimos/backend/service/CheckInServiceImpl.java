package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.CheckInRequest;
import com.aeropuertolosprimos.backend.dto.CheckInResponse;
import com.aeropuertolosprimos.backend.dto.CheckInSegmentoResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private static final String ESTADO_CHECKIN_REALIZADO = "REALIZADO";
    private static final String ESTADO_PAGO_PAGADO = "PAGADO";
    private static final String ESTADO_BOLETO_CANCELADO = "CANCELADO";

    private final BoletoRepository boletoRepository;
    private final BoletoSegmentoRepository boletoSegmentoRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final PasajeroRepository pasajeroRepository;
    private final ReservaRepository reservaRepository;
    private final AsientoVueloRepository asientoVueloRepository;
    private final AsientoUbiRepository asientoUbiRepository;

    private final CheckInRepository checkInRepository;
    private final EstadoCheckInRepository estadoCheckInRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final EstadoBoletoRepository estadoBoletoRepository;
    private final PagoRepository pagoRepository;
    private final HistorialBoletoRepository historialBoletoRepository;
    private final CatalogoEstadoService catalogoEstadoService;

    @Override
    @Transactional
    public CheckInResponse realizar(
            CheckInRequest request
    ) {

        Boleto boleto = resolverBoleto(request);

        validarBoletoParaCheckIn(boleto);

        EstadoCheckIn estadoRealizado = estadoCheckInRepository
                .findByNombreIgnoreCase(ESTADO_CHECKIN_REALIZADO)
                .orElseThrow(() -> new BusinessException("Estado de check-in REALIZADO no encontrado"));

        List<BoletoSegmento> boletoSegmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

        if (boletoSegmentos.isEmpty()) {
            throw new BusinessException("El boleto no tiene segmentos asociados");
        }

        boolean creoCheckIn = false;

        for (BoletoSegmento boletoSegmento : boletoSegmentos) {

            CheckIn checkIn = checkInRepository
                    .findFirstByBoletoSegmentoIdAndEstadoCheckinIdOrderByIdDesc(
                            boletoSegmento.getId(),
                            estadoRealizado.getId()
                    )
                    .orElse(null);

            if (checkIn == null) {

                checkIn = new CheckIn();

                checkIn.setBoletoSegmentoId(boletoSegmento.getId());
                checkIn.setEstadoCheckinId(estadoRealizado.getId());
                checkIn.setTipoCheckin(
                        request.getTipoCheckin() != null && !request.getTipoCheckin().isBlank()
                                ? request.getTipoCheckin().trim()
                                : "WEB"
                );
                checkIn.setEmpleadoId(request.getEmpleadoId());

                checkInRepository.save(checkIn);

                creoCheckIn = true;
            }
        }

        CheckInResponse response = mapResponse(boleto);

        if (!creoCheckIn) {
            response.setMensaje("El check-in ya estaba realizado");
            return response;
        }

        HistorialBoleto historial = new HistorialBoleto();

        historial.setBoletoId(boleto.getId());
        historial.setTipoAccion("CHECKIN");
        historial.setDescripcion("Check-in realizado para el boleto " + boleto.getCodigoBoleto());

        historialBoletoRepository.save(historial);

        response.setMensaje("Check-in realizado correctamente");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInResponse consultarPorBoleto(
            Integer boletoId
    ) {

        if (boletoId == null) {
            throw new BusinessException("Debe ingresar el boleto");
        }

        Boleto boleto = boletoRepository.findById(boletoId)
                .orElseThrow(() -> new BusinessException("Boleto no encontrado"));

        CheckInResponse response = mapResponse(boleto);

        response.setMensaje("Check-in encontrado");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInResponse consultarPorPase(
            String codigoPaseAbordar
    ) {

        if (codigoPaseAbordar == null || codigoPaseAbordar.isBlank()) {
            throw new BusinessException("Debe ingresar el pase de abordar");
        }

        Boleto boleto = boletoRepository
                .findFirstByCodigoPaseAbordarAndEstadoIdOrderByIdDesc(
                        codigoPaseAbordar.trim(),
                        obtenerEstadoActivoId()
                )
                .orElseThrow(() -> new BusinessException("Boleto no encontrado"));

        CheckInResponse response = mapResponse(boleto);

        response.setMensaje("Check-in encontrado");

        return response;
    }

    private Boleto resolverBoleto(
            CheckInRequest request
    ) {

        if (request == null) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getBoletoId() != null) {
            return boletoRepository.findById(request.getBoletoId())
                    .orElseThrow(() -> new BusinessException("Boleto no encontrado"));
        }

        if (request.getCodigoPaseAbordar() != null &&
                !request.getCodigoPaseAbordar().isBlank()) {

            return boletoRepository
                    .findFirstByCodigoPaseAbordarAndEstadoIdOrderByIdDesc(
                            request.getCodigoPaseAbordar().trim(),
                            obtenerEstadoActivoId()
                    )
                    .orElseThrow(() -> new BusinessException("Boleto no encontrado"));
        }

        if (request.getVueloOperadoId() != null &&
                request.getPasaporte() != null &&
                !request.getPasaporte().isBlank()) {

            Pasajero pasajero = pasajeroRepository
                    .findByPasaporte(request.getPasaporte().trim())
                    .orElseThrow(() -> new BusinessException("Pasajero no encontrado"));

            EstadoBoleto estadoCancelado = estadoBoletoRepository
                    .findByNombreIgnoreCase(ESTADO_BOLETO_CANCELADO)
                    .orElseThrow(() -> new BusinessException("Estado de boleto CANCELADO no encontrado"));

            return boletoRepository
                    .findFirstByPasajeroIdAndVueloOperadoIdAndEstadoBoletoIdNotAndEstadoIdOrderByIdDesc(
                            pasajero.getId(),
                            request.getVueloOperadoId(),
                            estadoCancelado.getId(),
                            obtenerEstadoActivoId()
                    )
                    .orElseThrow(() -> new BusinessException("Boleto no encontrado para el pasajero y vuelo"));
        }

        throw new BusinessException("Debe ingresar boletoId, código de pase o pasaporte con vuelo");
    }

    private void validarBoletoParaCheckIn(
            Boleto boleto
    ) {

        EstadoBoleto estadoCancelado = estadoBoletoRepository
                .findByNombreIgnoreCase(ESTADO_BOLETO_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de boleto CANCELADO no encontrado"));

        if (Objects.equals(boleto.getEstadoBoletoId(), estadoCancelado.getId())) {
            throw new BusinessException("No se puede hacer check-in a un boleto cancelado");
        }

        EstadoPago estadoPagado = estadoPagoRepository
                .findByNombreIgnoreCase(ESTADO_PAGO_PAGADO)
                .orElseThrow(() -> new BusinessException("Estado de pago PAGADO no encontrado"));

        if (!tienePagoPrincipal(boleto.getReservaId(), estadoPagado.getId())) {
            throw new BusinessException("La reserva no tiene pago registrado");
        }
    }

    private CheckInResponse mapResponse(
            Boleto boleto
    ) {

        CheckInResponse response = new CheckInResponse();

        response.setBoletoId(boleto.getId());
        response.setCodigoBoleto(boleto.getCodigoBoleto());
        response.setCodigoPaseAbordar(boleto.getCodigoPaseAbordar());
        response.setReservaId(boleto.getReservaId());
        response.setPasajeroId(boleto.getPasajeroId());
        response.setVueloOperadoId(boleto.getVueloOperadoId());
        response.setReservaPagada(tienePago(boleto.getReservaId()));

        reservaRepository.findById(boleto.getReservaId())
                .ifPresent(reserva ->
                        response.setCodigoReserva(reserva.getCodigoReserva())
                );

        pasajeroRepository.findById(boleto.getPasajeroId())
                .ifPresent(pasajero -> {
                    response.setNombrePasajero(pasajero.getNombreCompleto());
                    response.setPasaporte(pasajero.getPasaporte());
                });

        response.setAsiento(obtenerAsiento(boleto));

        List<CheckInSegmentoResponse> segmentosResponse = new ArrayList<>();

        List<BoletoSegmento> boletoSegmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

        for (BoletoSegmento boletoSegmento : boletoSegmentos) {

            CheckInSegmentoResponse segmentoResponse = new CheckInSegmentoResponse();

            segmentoResponse.setBoletoSegmentoId(boletoSegmento.getId());
            segmentoResponse.setSegmentoOperadoId(boletoSegmento.getSegmentoOperadoId());
            segmentoResponse.setOrdenSegmento(boletoSegmento.getOrdenSegmento());

            List<CheckIn> checkIns = checkInRepository
                    .findByBoletoSegmentoIdOrderByIdDesc(boletoSegmento.getId());

            if (!checkIns.isEmpty()) {

                CheckIn checkIn = checkIns.get(0);

                segmentoResponse.setCheckInId(checkIn.getId());
                segmentoResponse.setTipoCheckin(checkIn.getTipoCheckin());
                segmentoResponse.setFechaCheckin(checkIn.getFechaCheckin());
                segmentoResponse.setEmpleadoId(checkIn.getEmpleadoId());

                if (checkIn.getEstadoCheckinId() != null) {
                    estadoCheckInRepository.findById(checkIn.getEstadoCheckinId())
                            .ifPresent(estado ->
                                    segmentoResponse.setEstadoCheckin(estado.getNombre())
                            );
                }
            }

            segmentosResponse.add(segmentoResponse);
        }

        response.setSegmentos(segmentosResponse);

        return response;
    }

    private boolean tienePago(
            Integer reservaId
    ) {

        if (reservaId == null) {
            return false;
        }

        EstadoPago estadoPagado = estadoPagoRepository
                .findByNombreIgnoreCase(ESTADO_PAGO_PAGADO)
                .orElse(null);

        if (estadoPagado == null) {
            return false;
        }

        return pagoRepository
                .findByReservaIdOrderByIdDesc(reservaId)
                .stream()
                .anyMatch(pago -> esPagoPrincipalPagado(pago, estadoPagado.getId()));
    }

    private boolean tienePagoPrincipal(
            Integer reservaId,
            Integer estadoPagadoId
    ) {

        if (reservaId == null || estadoPagadoId == null) {
            return false;
        }

        return pagoRepository
                .findByReservaIdOrderByIdDesc(reservaId)
                .stream()
                .anyMatch(pago -> esPagoPrincipalPagado(pago, estadoPagadoId));
    }

    private boolean esPagoPrincipalPagado(
            Pago pago,
            Integer estadoPagadoId
    ) {

        return Objects.equals(pago.getEstadoPagoId(), estadoPagadoId) &&
                valor(pago.getRecargoEquipaje()).compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal valor(
            BigDecimal value
    ) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer obtenerEstadoActivoId() {
        return catalogoEstadoService.obtenerActivoId();
    }

    private String obtenerAsiento(
            Boleto boleto
    ) {

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

        if (segmentos.isEmpty()) {
            return null;
        }

        List<BoletoAsiento> asientos = boletoAsientoRepository
                .findByBoletoSegmentoId(segmentos.get(0).getId());

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
}
