package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.PagoResponse;
import com.aeropuertolosprimos.backend.dto.ReservaBoletoItemResponse;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;
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

        data.put("Código reserva", reserva.getCodigoReserva());
        data.put("Estado reserva", reserva.getEstadoReserva());

        data.put("Código boleto", reserva.getCodigoBoleto());
        data.put("Pase de abordar", reserva.getCodigoPaseAbordar());
        data.put("Estado boleto", reserva.getEstadoBoleto());

        data.put("Asiento", reserva.getAsiento());
        data.put("Cantidad de maletas", reserva.getCantidadMaletas());

        data.put("Subtotal", reserva.getSubtotal());
        data.put("Recargo total", reserva.getRecargoTotal());
        data.put("Total", reserva.getTotal());

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Reserva / Boleto",
                data
        );

        String base = "reserva_" + valorArchivo(
                reserva.getCodigoReserva(),
                "sin_codigo"
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp(base, "pdf")
                )
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

        data.put("Serie", pago.getFactura().getSerie());
        data.put("Número", pago.getFactura().getNumero());
        data.put("UUID FEL", pago.getFactura().getUuidFel());
        data.put("Estado FEL", pago.getFactura().getEstadoFel());
        data.put("Fecha factura", pago.getFactura().getFechaFactura());

        data.put("NIT", pago.getFactura().getNit());
        data.put("Nombre cliente", pago.getFactura().getNombreCliente());

        data.put("Código reserva", pago.getCodigoReserva());
        data.put("Método de pago", pago.getMetodoPago());
        data.put("Monto pagado", pago.getMonto());
        data.put("Recargo equipaje", pago.getRecargoEquipaje());
        data.put("Total reserva", pago.getTotalReserva());
        data.put("Estado pago", pago.getEstadoPago());
        data.put("Fecha pago", pago.getFechaPago());

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Factura",
                data
        );

        String serie = valorArchivo(
                pago.getFactura().getSerie(),
                "FEL"
        );

        String numero = valorArchivo(
                pago.getFactura().getNumero(),
                "sin_numero"
        );

        String base = "factura_" + serie + "_" + numero;

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp(base, "pdf")
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/boletos/{boletoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> boletoPdf(
            @PathVariable Integer boletoId
    ) {

        Boleto boleto = boletoRepository.findById(boletoId)
                .orElseThrow(() -> new BusinessException("Boleto no encontrado"));

        ReservaResponse reserva = reservaService.obtenerPorId(
                boleto.getReservaId()
        );

        ReservaBoletoItemResponse item = null;

        if (reserva.getBoletos() != null) {
            item = reserva.getBoletos()
                    .stream()
                    .filter(b -> b != null
                            && b.getBoletoId() != null
                            && b.getBoletoId().equals(boletoId))
                    .findFirst()
                    .orElse(null);
        }

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("Código reserva", reserva.getCodigoReserva());
        data.put("Código boleto", boleto.getCodigoBoleto());
        data.put("Pase de abordar", boleto.getCodigoPaseAbordar());
        data.put("Total boleto", boleto.getTotal());

        if (item != null) {
            data.put("Pasajero", item.getNombrePasajero());
            data.put("Pasaporte", item.getPasaporte());
            data.put("Asiento", item.getAsiento());
            data.put("Cantidad de maletas", item.getCantidadMaletas());
        }

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Boleto",
                data
        );

        String base = "boleto_" + valorArchivo(
                boleto.getCodigoBoleto(),
                "sin_codigo"
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp(base, "pdf")
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String valorArchivo(
            String value,
            String fallback
    ) {

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value
                .trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}