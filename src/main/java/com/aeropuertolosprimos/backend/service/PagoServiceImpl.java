package com.aeropuertolosprimos.backend.service;

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
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private static final String ESTADO_PAGO_PAGADO = "PAGADO";

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

        validarRequest(request);

        Reserva reserva = reservaRepository.findById(request.getReservaId())
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        MetodoPago metodoPago = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new BusinessException("Método de pago no encontrado"));

        EstadoPago estadoPagado = estadoPagoRepository
                .findByNombreIgnoreCase(ESTADO_PAGO_PAGADO)
                .orElseThrow(() -> new BusinessException("Estado de pago PAGADO no encontrado"));

        pagoRepository
                .findFirstByReservaIdAndEstadoPagoIdOrderByIdDesc(
                        reserva.getId(),
                        estadoPagado.getId()
                )
                .ifPresent(p -> {
                    throw new BusinessException("La reserva ya tiene un pago registrado");
                });

        BigDecimal monto = request.getMonto() != null
                ? request.getMonto()
                : valor(reserva.getTotal());

        BigDecimal recargoEquipaje = request.getRecargoEquipaje() != null
                ? request.getRecargoEquipaje()
                : BigDecimal.ZERO;

        Pago pago = new Pago();

        pago.setReservaId(reserva.getId());
        pago.setMetodoPagoId(metodoPago.getId());
        pago.setMonto(monto);
        pago.setRecargoEquipaje(recargoEquipaje);
        pago.setEstadoPagoId(estadoPagado.getId());

        pago = pagoRepository.save(pago);

        Factura factura = new Factura();

        factura.setPagoId(pago.getId());
        factura.setNit(limpiarONulo(request.getNit(), "CF"));
        factura.setNombreCliente(limpiarONulo(request.getNombreCliente(), "CONSUMIDOR FINAL"));
        factura.setSerie("ALP");
        factura.setEstadoFel("CERTIFICADA");

        factura = facturaRepository.save(factura);

        factura.setNumero(
                "FAC-" + String.format("%06d", factura.getId())
        );

        factura.setUuidFel(
                "FEL-" + String.format("%06d", factura.getId())
        );

        factura = facturaRepository.save(factura);

        List<Boleto> boletos = boletoRepository.findByReservaIdOrderByIdAsc(
                reserva.getId()
        );

        for (Boleto boleto : boletos) {

            HistorialBoleto historial = new HistorialBoleto();

            historial.setBoletoId(boleto.getId());
            historial.setTipoAccion("PAGO");
            historial.setDescripcion("Pago registrado para la reserva " + reserva.getCodigoReserva());

            historialBoletoRepository.save(historial);
        }

        PagoResponse response = mapResponse(pago);

        response.setMensaje("Pago registrado y factura generada correctamente");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(
            Integer id
    ) {

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

    private void validarRequest(
            PagoRequest request
    ) {

        if (request == null ||
                request.getReservaId() == null ||
                request.getMetodoPagoId() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }
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

        metodoPagoRepository.findById(pago.getMetodoPagoId())
                .ifPresent(metodo ->
                        response.setMetodoPago(metodo.getNombre())
                );

        estadoPagoRepository.findById(pago.getEstadoPagoId())
                .ifPresent(estado ->
                        response.setEstadoPago(estado.getNombre())
                );

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