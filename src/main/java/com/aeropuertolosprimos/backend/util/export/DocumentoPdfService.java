package com.aeropuertolosprimos.backend.util.export;

import com.aeropuertolosprimos.backend.dto.PagoResponse;
import com.aeropuertolosprimos.backend.dto.ReservaBoletoItemResponse;
import com.aeropuertolosprimos.backend.dto.ReservaBoletoSegmentoResponse;
import com.aeropuertolosprimos.backend.dto.ReservaResponse;
import com.aeropuertolosprimos.backend.model.Boleto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class DocumentoPdfService {

    public byte[] reservaPdf(
            ReservaResponse reserva
    ) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = 735;

            y = titulo(cs, "AEROPUERTO LOS PRIMOS", "COMPROBANTE DE RESERVA", y);

            y = bloque(cs, "RESERVA", y);
            y = fila(cs, "Codigo reserva", reserva.getCodigoReserva(), "Estado reserva", reserva.getEstadoReserva(), y);
            y = fila(cs, "Fecha reserva", reserva.getFechaReserva(), "Estado pago", reserva.getEstadoPago(), y);
            y = fila(cs, "Pasajeros", reserva.getCantidadPasajeros(), "Total", money(reserva.getTotal()), y);

            y -= 12;
            y = bloque(cs, "BOLETOS", y);

            encabezadoTabla(cs, y, "Pasajero", "Pasaporte", "Boleto", "Asiento", "Total");
            y -= 18;

            if (reserva.getBoletos() != null) {
                for (ReservaBoletoItemResponse b : reserva.getBoletos()) {
                    filaTabla(
                            cs,
                            y,
                            b.getNombrePasajero(),
                            b.getPasaporte(),
                            b.getCodigoBoleto(),
                            b.getAsiento(),
                            money(b.getTotal())
                    );
                    y -= 16;
                }
            }

            y -= 12;
            y = bloque(cs, "RESUMEN ECONOMICO", y);
            y = fila(cs, "Subtotal", money(reserva.getSubtotal()), "Recargos", money(reserva.getRecargoTotal()), y);
            y = fila(cs, "Total reserva", money(reserva.getTotal()), "Pago", reserva.getEstadoPago(), y);

            pie(cs);

            cs.close();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar PDF de reserva", e);
        }
    }

    public byte[] boletoPdf(
            ReservaResponse reserva,
            Boleto boleto,
            ReservaBoletoItemResponse item
    ) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = 735;

            y = titulo(cs, "AEROPUERTO LOS PRIMOS", "BOLETO ELECTRONICO", y);

            y = bloque(cs, "DATOS DEL BOLETO", y);
            y = fila(cs, "Codigo boleto", boleto.getCodigoBoleto(), "Pase abordar", boleto.getCodigoPaseAbordar(), y);
            y = fila(cs, "Codigo reserva", reserva.getCodigoReserva(), "Estado boleto",
                    item != null ? item.getEstadoBoleto() : reserva.getEstadoBoleto(), y);

            y -= 12;
            y = bloque(cs, "PASAJERO", y);
            y = fila(cs, "Nombre", item != null ? item.getNombrePasajero() : "-",
                    "Pasaporte", item != null ? item.getPasaporte() : "-", y);
            y = fila(cs, "Asiento", item != null ? item.getAsiento() : "-",
                    "Maletas", item != null ? item.getCantidadMaletas() : "-", y);

            y -= 12;
            y = bloque(cs, "SEGMENTOS", y);

            encabezadoTabla(cs, y, "Segmento", "Asiento", "Clase", "Estado", "");
            y -= 18;

            if (item != null && item.getSegmentos() != null) {
                for (ReservaBoletoSegmentoResponse s : item.getSegmentos()) {
                    filaTabla(
                            cs,
                            y,
                            String.valueOf(s.getOrdenSegmento()),
                            s.getAsiento(),
                            s.getClaseVueloNombre(),
                            s.getEstadoBoletoSegmento(),
                            ""
                    );
                    y -= 16;
                }
            }

            y -= 12;
            y = bloque(cs, "IMPORTE", y);
            y = fila(cs, "Precio base", money(boleto.getPrecioBase()),
                    "Recargo equipaje", money(boleto.getRecargoEquipaje()), y);
            y = fila(cs, "Total boleto", money(boleto.getTotal()), "Estado pago reserva", reserva.getEstadoPago(), y);

            pie(cs);

            cs.close();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar PDF de boleto", e);
        }
    }

    public byte[] facturaPdf(
            PagoResponse pago
    ) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = 735;

            y = titulo(cs, "AEROPUERTO LOS PRIMOS", "FACTURA FEL", y);

            y = bloque(cs, "DATOS FEL", y);
            y = fila(cs, "Serie", pago.getFactura().getSerie(), "Numero", pago.getFactura().getNumero(), y);
            y = fila(cs, "UUID FEL", pago.getFactura().getUuidFel(), "Estado FEL", pago.getFactura().getEstadoFel(), y);
            y = fila(cs, "Fecha factura", pago.getFactura().getFechaFactura(), "Estado pago", pago.getEstadoPago(), y);

            y -= 12;
            y = bloque(cs, "CLIENTE", y);
            y = fila(cs, "NIT", pago.getFactura().getNit(), "Nombre", pago.getFactura().getNombreCliente(), y);

            y -= 12;
            y = bloque(cs, "DETALLE", y);

            encabezadoTabla(cs, y, "Concepto", "Reserva", "Metodo", "Monto", "");
            y -= 18;

            filaTabla(
                    cs,
                    y,
                    "Pago de reserva aerea",
                    pago.getCodigoReserva(),
                    pago.getMetodoPago(),
                    money(pago.getMonto()),
                    ""
            );

            y -= 30;
            y = bloque(cs, "TOTAL", y);
            y = fila(cs, "Total pagado", money(pago.getMonto()), "Recargo equipaje", money(pago.getRecargoEquipaje()), y);

            pie(cs);

            cs.close();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar PDF de factura", e);
        }
    }

    private float titulo(
            PDPageContentStream cs,
            String empresa,
            String documento,
            float y
    ) throws Exception {
        texto(cs, PDType1Font.HELVETICA_BOLD, 18, 54, y, empresa);
        texto(cs, PDType1Font.HELVETICA_BOLD, 14, 54, y - 22, documento);
        texto(cs, PDType1Font.HELVETICA, 9, 400, y, "Generado: " + LocalDateTime.now());
        return y - 55;
    }

    private float bloque(
            PDPageContentStream cs,
            String titulo,
            float y
    ) throws Exception {
        cs.addRect(54, y - 4, 504, 18);
        cs.stroke();
        texto(cs, PDType1Font.HELVETICA_BOLD, 10, 60, y, titulo);
        return y - 24;
    }

    private float fila(
            PDPageContentStream cs,
            String k1,
            Object v1,
            String k2,
            Object v2,
            float y
    ) throws Exception {
        texto(cs, PDType1Font.HELVETICA_BOLD, 9, 60, y, safe(k1) + ":");
        texto(cs, PDType1Font.HELVETICA, 9, 160, y, safe(v1));
        texto(cs, PDType1Font.HELVETICA_BOLD, 9, 315, y, safe(k2) + ":");
        texto(cs, PDType1Font.HELVETICA, 9, 425, y, safe(v2));
        return y - 16;
    }

    private void encabezadoTabla(
            PDPageContentStream cs,
            float y,
            String c1,
            String c2,
            String c3,
            String c4,
            String c5
    ) throws Exception {
        filaTablaFont(cs, y, c1, c2, c3, c4, c5, true);
    }

    private void filaTabla(
            PDPageContentStream cs,
            float y,
            Object c1,
            Object c2,
            Object c3,
            Object c4,
            Object c5
    ) throws Exception {
        filaTablaFont(cs, y, c1, c2, c3, c4, c5, false);
    }

    private void filaTablaFont(
            PDPageContentStream cs,
            float y,
            Object c1,
            Object c2,
            Object c3,
            Object c4,
            Object c5,
            boolean bold
    ) throws Exception {
        var font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;

        texto(cs, font, 8, 60, y, safe(c1));
        texto(cs, font, 8, 165, y, safe(c2));
        texto(cs, font, 8, 260, y, safe(c3));
        texto(cs, font, 8, 365, y, safe(c4));
        texto(cs, font, 8, 470, y, safe(c5));
    }

    private void pie(
            PDPageContentStream cs
    ) throws Exception {
        texto(cs, PDType1Font.HELVETICA, 8, 54, 45,
                "Documento generado por Aeropuerto Los Primos. No muestra IDs internos del sistema.");
    }

    private void texto(
            PDPageContentStream cs,
            org.apache.pdfbox.pdmodel.font.PDFont font,
            int size,
            float x,
            float y,
            String text
    ) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(safe(text));
        cs.endText();
    }

    private String safe(
            Object value
    ) {
        if (value == null) {
            return "-";
        }

        return String.valueOf(value)
                .replace("\t", " ")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    private String money(
            BigDecimal value
    ) {
        BigDecimal amount = value != null ? value : BigDecimal.ZERO;
        return "Q " + amount.setScale(2, RoundingMode.HALF_UP);
    }
}
