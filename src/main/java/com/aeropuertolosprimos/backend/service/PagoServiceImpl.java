package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ConfirmarPagoRequest;
import com.aeropuertolosprimos.backend.dto.FacturaResponse;
import com.aeropuertolosprimos.backend.dto.PagoRequest;
import com.aeropuertolosprimos.backend.dto.PagoResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final String ESTADO_PAGO_PAGADO = "PAGADO";
    private static final String ESTADO_PAGO_PENDIENTE = "PENDIENTE";

    private static final String TIPO_PAGO_NORMAL = "NORMAL";
    private static final String TIPO_PAGO_RECARGO_EQUIPAJE = "RECARGO_EQUIPAJE";

    private final ReservaRepository reservaRepository;
    private final BoletoRepository boletoRepository;

    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final HistorialBoletoRepository historialBoletoRepository;

    @Override
    @Transactional
    public PagoResponse pagar(
            PagoRequest request
    ) {

        validarPagoNormal(request);

        Reserva reserva = reservaRepository.findById(request.getReservaId())
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        MetodoPago metodoPago = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new BusinessException("Método de pago no encontrado"));

        EstadoPago estadoPagado = obtenerEstadoPago(ESTADO_PAGO_PAGADO);

        String tipoPago = obtenerTipoPago(request.getTipoPago());

        BigDecimal recargoEquipaje = valor(request.getRecargoEquipaje());

        if (tipoPago.equals(TIPO_PAGO_RECARGO_EQUIPAJE)
                && recargoEquipaje.compareTo(BigDecimal.ZERO) <= 0) {

            recargoEquipaje = valor(request.getMonto());
        }

        if (tipoPago.equals(TIPO_PAGO_NORMAL)) {
            pagoRepository
                    .findFirstByReservaIdAndRecargoEquipajeAndEstadoPagoIdOrderByIdDesc(
                            reserva.getId(),
                            BigDecimal.ZERO,
                            estadoPagado.getId()
                    )
                    .ifPresent(pago -> {
                        throw new BusinessException("La reserva ya tiene un pago registrado");
                    });
        }

        if (tipoPago.equals(TIPO_PAGO_RECARGO_EQUIPAJE)) {
            validarRecargoNoPagado(
                    reserva.getId(),
                    recargoEquipaje,
                    estadoPagado.getId()
            );
        }

        BigDecimal monto = request.getMonto() != null
                ? request.getMonto()
                : tipoPago.equals(TIPO_PAGO_RECARGO_EQUIPAJE)
                ? recargoEquipaje
                : valor(reserva.getTotal());

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto debe ser mayor a cero");
        }

        Pago pago = new Pago();

        pago.setReservaId(reserva.getId());
        pago.setMetodoPagoId(metodoPago.getId());
        pago.setMonto(monto);
        pago.setRecargoEquipaje(recargoEquipaje);
        pago.setEstadoPagoId(estadoPagado.getId());
        pago.setFechaPago(LocalDateTime.now());

        pago = pagoRepository.save(pago);

        Factura factura = generarFactura(
                pago,
                request.getNit(),
                request.getNombreCliente()
        );

        registrarHistorialPago(
                reserva,
                tipoPago
        );

        PagoResponse response = mapResponse(pago);

        response.setFactura(mapFactura(factura));
        response.setMensaje("Pago registrado y factura generada correctamente");

        return response;
    }

    @Override
    @Transactional
    public PagoResponse crearPagoRecargoEquipajePendiente(
            Integer reservaId,
            BigDecimal monto
    ) {

        if (reservaId == null) {
            throw new BusinessException("Debe ingresar la reserva");
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El recargo debe ser mayor a cero");
        }

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        EstadoPago estadoPendiente = obtenerEstadoPago(ESTADO_PAGO_PENDIENTE);
        EstadoPago estadoPagado = obtenerEstadoPago(ESTADO_PAGO_PAGADO);

        pagoRepository
                .findFirstByReservaIdAndRecargoEquipajeAndEstadoPagoIdOrderByIdDesc(
                        reserva.getId(),
                        monto,
                        estadoPagado.getId()
                )
                .ifPresent(pago -> {
                    throw new BusinessException("El recargo de equipaje ya fue pagado");
                });

        Pago pagoPendiente = pagoRepository
                .findFirstByReservaIdAndRecargoEquipajeAndEstadoPagoIdOrderByIdDesc(
                        reserva.getId(),
                        monto,
                        estadoPendiente.getId()
                )
                .orElse(null);

        if (pagoPendiente != null) {
            PagoResponse response = mapResponse(pagoPendiente);
            response.setMensaje("Ya existe un pago pendiente por recargo de equipaje");
            return response;
        }

        Pago pago = new Pago();

        pago.setReservaId(reserva.getId());
        pago.setMetodoPagoId(null);
        pago.setMonto(monto);
        pago.setRecargoEquipaje(monto);
        pago.setEstadoPagoId(estadoPendiente.getId());
        pago.setFechaPago(null);

        pago = pagoRepository.save(pago);

        PagoResponse response = mapResponse(pago);
        response.setMensaje("Pago pendiente por recargo de equipaje generado");

        return response;
    }

    @Override
    @Transactional
    public PagoResponse confirmarPagoPendiente(
            Integer pagoId,
            ConfirmarPagoRequest request
    ) {

        if (pagoId == null) {
            throw new BusinessException("Debe ingresar el pago");
        }

        if (request == null || request.getMetodoPagoId() == null) {
            throw new BusinessException("Debe ingresar el método de pago");
        }

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new BusinessException("Pago no encontrado"));

        EstadoPago estadoPendiente = obtenerEstadoPago(ESTADO_PAGO_PENDIENTE);
        EstadoPago estadoPagado = obtenerEstadoPago(ESTADO_PAGO_PAGADO);

        if (!pago.getEstadoPagoId().equals(estadoPendiente.getId())) {
            throw new BusinessException("El pago no está pendiente");
        }

        MetodoPago metodoPago = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new BusinessException("Método de pago no encontrado"));

        pago.setMetodoPagoId(metodoPago.getId());
        pago.setEstadoPagoId(estadoPagado.getId());
        pago.setFechaPago(LocalDateTime.now());

        pago = pagoRepository.save(pago);

        Factura factura = generarFactura(
                pago,
                request.getNit(),
                request.getNombreCliente()
        );

        reservaRepository.findById(pago.getReservaId())
                .ifPresent(reserva ->
                        registrarHistorialPago(
                                reserva,
                                TIPO_PAGO_RECARGO_EQUIPAJE
                        )
                );

        PagoResponse response = mapResponse(pago);
        response.setFactura(mapFactura(factura));
        response.setMensaje("Pago confirmado y factura generada correctamente");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(
            Integer id
    ) {

        if (id == null) {
            throw new BusinessException("Debe ingresar el pago");
        }

        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pago no encontrado"));

        return mapResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorReserva(
            Integer reservaId
    ) {

        if (reservaId == null) {
            throw new BusinessException("Debe ingresar la reserva");
        }

        return pagoRepository.findByReservaIdOrderByIdDesc(reservaId)
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    private void validarPagoNormal(
            PagoRequest request
    ) {

        if (request == null ||
                request.getReservaId() == null ||
                request.getMetodoPagoId() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }
    }

    private void validarRecargoNoPagado(
            Integer reservaId,
            BigDecimal recargoEquipaje,
            Integer estadoPagadoId
    ) {

        if (recargoEquipaje == null || recargoEquipaje.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El recargo debe ser mayor a cero");
        }

        pagoRepository
                .findFirstByReservaIdAndRecargoEquipajeAndEstadoPagoIdOrderByIdDesc(
                        reservaId,
                        recargoEquipaje,
                        estadoPagadoId
                )
                .ifPresent(pago -> {
                    throw new BusinessException("El recargo de equipaje ya fue pagado");
                });
    }

    private EstadoPago obtenerEstadoPago(
            String nombre
    ) {

        return estadoPagoRepository
                .findByNombreIgnoreCase(nombre)
                .orElseThrow(() ->
                        new BusinessException("Estado de pago no encontrado: " + nombre)
                );
    }

    private String obtenerTipoPago(
            String tipoPago
    ) {

        if (tipoPago == null || tipoPago.isBlank()) {
            return TIPO_PAGO_NORMAL;
        }

        return tipoPago.trim().toUpperCase();
    }

    private void registrarHistorialPago(
            Reserva reserva,
            String tipoPago
    ) {

        List<Boleto> boletos = boletoRepository.findByReservaIdOrderByIdAsc(
                reserva.getId()
        );

        for (Boleto boleto : boletos) {

            HistorialBoleto historial = new HistorialBoleto();

            historial.setBoletoId(boleto.getId());

            if (tipoPago.equals(TIPO_PAGO_RECARGO_EQUIPAJE)) {
                historial.setTipoAccion("PAGO_RECARGO_EQUIPAJE");
                historial.setDescripcion(
                        "Pago de recargo de equipaje registrado para la reserva " + reserva.getCodigoReserva()
                );
            } else {
                historial.setTipoAccion("PAGO");
                historial.setDescripcion(
                        "Pago registrado para la reserva " + reserva.getCodigoReserva()
                );
            }

            historialBoletoRepository.save(historial);
        }
    }

    private Factura generarFactura(
            Pago pago,
            String nit,
            String nombreCliente
    ) {

        Factura factura = new Factura();

        factura.setPagoId(pago.getId());
        factura.setNit(limpiarONulo(nit, "CF"));
        factura.setNombreCliente(limpiarONulo(nombreCliente, "CONSUMIDOR FINAL"));
        factura.setSerie("ALP");
        factura.setEstadoFel("CERTIFICADA");

        factura = facturaRepository.save(factura);

        factura.setNumero(
                "FAC-" + String.format("%06d", factura.getId())
        );

        factura.setUuidFel(
                "FEL-" + String.format("%06d", factura.getId())
        );

        return facturaRepository.save(factura);
    }

    private PagoResponse mapResponse(
            Pago pago
    ) {

        PagoResponse response = new PagoResponse();

        response.setId(pago.getId());
        response.setReservaId(pago.getReservaId());
        response.setMetodoPagoId(pago.getMetodoPagoId());
        response.setMonto(pago.getMonto());
        response.setRecargoEquipaje(pago.getRecargoEquipaje());
        response.setEstadoPagoId(pago.getEstadoPagoId());
        response.setFechaPago(pago.getFechaPago());

        reservaRepository.findById(pago.getReservaId())
                .ifPresent(reserva -> {
                    response.setCodigoReserva(reserva.getCodigoReserva());
                    response.setTotalReserva(reserva.getTotal());
                });

        if (pago.getMetodoPagoId() != null) {
            metodoPagoRepository.findById(pago.getMetodoPagoId())
                    .ifPresent(metodo ->
                            response.setMetodoPago(metodo.getNombre())
                    );
        }

        if (pago.getEstadoPagoId() != null) {
            estadoPagoRepository.findById(pago.getEstadoPagoId())
                    .ifPresent(estado ->
                            response.setEstadoPago(estado.getNombre())
                    );
        }

        facturaRepository.findFirstByPagoIdOrderByIdDesc(pago.getId())
                .ifPresent(factura ->
                        response.setFactura(mapFactura(factura))
                );

        response.setMensaje("Pago encontrado");

        return response;
    }

    private FacturaResponse mapFactura(
            Factura factura
    ) {

        FacturaResponse response = new FacturaResponse();

        response.setId(factura.getId());
        response.setPagoId(factura.getPagoId());
        response.setNit(factura.getNit());
        response.setNombreCliente(factura.getNombreCliente());
        response.setSerie(factura.getSerie());
        response.setNumero(factura.getNumero());
        response.setUuidFel(factura.getUuidFel());
        response.setEstadoFel(factura.getEstadoFel());
        response.setFechaFactura(factura.getFechaFactura());

        return response;
    }

    private BigDecimal valor(
            BigDecimal valor
    ) {

        return valor != null ? valor : BigDecimal.ZERO;
    }

    private String limpiarONulo(
            String valor,
            String valorDefault
    ) {

        if (valor == null || valor.isBlank()) {
            return valorDefault;
        }

        return valor.trim();
    }
}