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
import com.aeropuertolosprimos.backend.util.export.DocumentoPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final ReservaService reservaService;
    private final PagoService pagoService;
    private final BoletoRepository boletoRepository;
        private final DocumentoPdfService documentoPdfService;

    @GetMapping(value = "/reservas/{reservaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> reservaPdf(
            @PathVariable Integer reservaId
    ) {

        ReservaResponse reserva = reservaService.obtenerPorId(reservaId);
        byte[] pdf = documentoPdfService.reservaPdf(reserva);

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
        byte[] pdf = documentoPdfService.facturaPdf(pago);

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

        byte[] pdf = documentoPdfService.boletoPdf(
                reserva,
                boleto,
                item
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