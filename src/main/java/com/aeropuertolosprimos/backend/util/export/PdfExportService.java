package com.aeropuertolosprimos.backend.util.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    public byte[] simpleKeyValuePdf(
            String title,
            Map<String, Object> data
    ) {

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            float margin = 54;

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getHeight() - margin;
            y = writeLine(cs, PDType1Font.HELVETICA_BOLD, 16, margin, y, safe(title));
            y = writeLine(cs, PDType1Font.HELVETICA, 9, margin, y - 6, "Generado: " + LocalDateTime.now());
            y -= 10;

            if (data == null || data.isEmpty()) {
                writeLine(cs, PDType1Font.HELVETICA, 11, margin, y, "Sin datos");
            } else {
                for (Map.Entry<String, Object> e : data.entrySet()) {
                    String line = safe(e.getKey()) + ": " + safe(e.getValue());
                    y = writeWrapped(cs, PDType1Font.HELVETICA, 11, margin, y, line, 500);
                    y -= 2;

                    if (y < margin + 60) {
                        cs.close();
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = page.getMediaBox().getHeight() - margin;
                    }
                }
            }

            cs.close();

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar PDF", e);
        }
    }

    public byte[] tablePdf(
            String title,
            List<Map<String, Object>> rows
    ) {

        List<Map<String, Object>> safeRows =
                rows == null ? List.of() : rows;

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            float margin = 54;
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = page.getMediaBox().getHeight() - margin;
            float width = page.getMediaBox().getWidth() - margin * 2;

            y = writeLine(cs, PDType1Font.HELVETICA_BOLD, 16, margin, y, safe(title));
            y = writeLine(cs, PDType1Font.HELVETICA, 9, margin, y - 6, "Generado: " + LocalDateTime.now());
            y -= 14;

            if (safeRows.isEmpty()) {
                writeLine(cs, PDType1Font.HELVETICA, 11, margin, y, "Sin datos");
            } else {
                for (int i = 0; i < safeRows.size(); i++) {
                    Map<String, Object> row = safeRows.get(i);
                    String header = "Registro " + (i + 1);
                    y = writeLine(cs, PDType1Font.HELVETICA_BOLD, 12, margin, y, header);
                    y -= 2;

                    for (Map.Entry<String, Object> e : row.entrySet()) {
                        String line = " - " + safe(e.getKey()) + ": " + safe(e.getValue());
                        y = writeWrapped(cs, PDType1Font.HELVETICA, 10, margin, y, line, width);
                        y -= 1;

                        if (y < margin + 60) {
                            cs.close();
                            page = new PDPage(PDRectangle.LETTER);
                            doc.addPage(page);
                            cs = new PDPageContentStream(doc, page);
                            y = page.getMediaBox().getHeight() - margin;
                        }
                    }

                    y -= 6;
                    if (y < margin + 60) {
                        cs.close();
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = page.getMediaBox().getHeight() - margin;
                    }
                }
            }

            cs.close();

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar PDF", e);
        }
    }

    private float writeLine(
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
        cs.showText(text);
        cs.endText();
        return y - (size + 2);
    }

    private float writeWrapped(
            PDPageContentStream cs,
            org.apache.pdfbox.pdmodel.font.PDFont font,
            int size,
            float x,
            float y,
            String text,
            float maxWidth
    ) throws Exception {

        String t = text == null ? "" : text;

        String[] words = t.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            float w = font.getStringWidth(candidate) / 1000 * size;

            if (w > maxWidth && !line.isEmpty()) {
                y = writeLine(cs, font, size, x, y, line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (!line.isEmpty()) {
            y = writeLine(cs, font, size, x, y, line.toString());
        }

        return y;
    }

    private String safe(Object value) {
        if (value == null) return "-";
        String s = String.valueOf(value);
        return s.replace("\t", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
