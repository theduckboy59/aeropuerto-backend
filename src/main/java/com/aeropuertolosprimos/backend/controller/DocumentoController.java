package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.PagoResponse;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;
import com.aeropuertolosprimos.backend.dto.ReservaBoletoItemResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.Boleto;
import com.aeropuertolosprimos.backend.repository.BoletoRepository;
import com.aeropuertolosprimos.backend.service.PagoService;
import com.aeropuertolosprimos.backend.service.ReservaService;
import com.aeropuertolosprimos.backend.util.export.ExportFileName;
import com.aeropuertolosprimos.backend.util.export.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final ReservaService reservaService;
    private final PagoService pagoService;
    private final BoletoRepository boletoRepository;
    private final PdfExportService pdfExportService;

    @GetMapping(value = "/reservas/{reservaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reservaPdf(
            @PathVariable Integer reservaId
    ) {

        ReservaResponse reserva = reservaService.obtenerPorId(reservaId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("codigoReserva", reserva.getCodigoReserva());
        data.put("reservaId", reserva.getReservaId());
        data.put("vueloOperadoId", reserva.getVueloOperadoId());
        data.put("estadoReserva", reserva.getEstadoReserva());
        data.put("subtotal", reserva.getSubtotal());
        data.put("recargoTotal", reserva.getRecargoTotal());
        data.put("total", reserva.getTotal());
        data.put("boletoId", reserva.getBoletoId());
        data.put("codigoBoleto", reserva.getCodigoBoleto());
        data.put("codigoPaseAbordar", reserva.getCodigoPaseAbordar());
        data.put("estadoBoleto", reserva.getEstadoBoleto());
        data.put("asiento", reserva.getAsiento());
        data.put("cantidadMaletas", reserva.getCantidadMaletas());

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Reserva / Boleto",
                data
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reserva_" + reserva.getCodigoReserva(), "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/pagos/{pagoId}/factura/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> facturaPorPagoPdf(
            @PathVariable Integer pagoId
    ) {

        PagoResponse pago = pagoService.obtenerPorId(pagoId);

        if (pago.getFactura() == null) {
            throw new BusinessException("El pago no tiene factura generada");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("serie", pago.getFactura().getSerie());
        data.put("numero", pago.getFactura().getNumero());
        data.put("uuidFel", pago.getFactura().getUuidFel());
        data.put("estadoFel", pago.getFactura().getEstadoFel());
        data.put("nit", pago.getFactura().getNit());
        data.put("nombreCliente", pago.getFactura().getNombreCliente());
        data.put("fechaFactura", pago.getFactura().getFechaFactura());
        data.put("pagoId", pago.getId());
        data.put("reservaId", pago.getReservaId());
        data.put("codigoReserva", pago.getCodigoReserva());
        data.put("metodoPago", pago.getMetodoPago());
        data.put("monto", pago.getMonto());
        data.put("recargoEquipaje", pago.getRecargoEquipaje());
        data.put("totalReserva", pago.getTotalReserva());
        data.put("estadoPago", pago.getEstadoPago());
        data.put("fechaPago", pago.getFechaPago());

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Factura",
                data
        );

        String base = "factura_" + pago.getFactura().getSerie() + "_" + pago.getFactura().getNumero();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp(base, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/boletos/{boletoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> boletoPdf(
            @PathVariable Integer boletoId
    ) {

        Boleto boleto = boletoRepository.findById(boletoId)
                .orElseThrow(() -> new BusinessException("Boleto no encontrado"));

        ReservaResponse reserva = reservaService.obtenerPorId(boleto.getReservaId());

        ReservaBoletoItemResponse item = null;

        if (reserva.getBoletos() != null) {
            item = reserva.getBoletos().stream()
                    .filter(b -> b != null && b.getBoletoId() != null && b.getBoletoId().equals(boletoId))
                    .findFirst()
                    .orElse(null);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("codigoReserva", reserva.getCodigoReserva());
        data.put("reservaId", reserva.getReservaId());
        data.put("boletoId", boletoId);
        data.put("codigoBoleto", boleto.getCodigoBoleto());
        data.put("codigoPaseAbordar", boleto.getCodigoPaseAbordar());
        data.put("vueloOperadoId", boleto.getVueloOperadoId());
        data.put("total", boleto.getTotal());

        if (item != null) {
            data.put("pasajero", item.getNombrePasajero());
            data.put("pasaporte", item.getPasaporte());
            data.put("asiento", item.getAsiento());
            data.put("cantidadMaletas", item.getCantidadMaletas());
        }

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Boleto / Pase de abordar",
                data
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("boleto_" + boleto.getCodigoBoleto(), "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
