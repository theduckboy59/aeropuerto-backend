package com.aeropuertolosprimos.backend.util.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PdfExportService {
    private static final String EMPRESA = "AEROPUERTO LOS PRIMOS";
    private static final String SUBTITULO = "Sistema de Gestion Aeroportuaria";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final float MARGIN = 36f;
    private static final float HEADER_HEIGHT = 82f;
    private static final float FOOTER_HEIGHT = 34f;

    public byte[] simpleKeyValuePdf(
            String title,
            Map<String, Object> data
    ) {
        Map<String, Object> safeData = data == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(data);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float y = drawHeader(
                    cs,
                    page,
                    title,
                    "Consulta individual"
            );

            y -= 14;

            drawSectionTitle(
                    cs,
                    "DATOS DE LA CONSULTA",
                    MARGIN,
                    y
            );

            y -= 28;

            if (safeData.isEmpty()) {
                writeText(
                        cs,
                        PDType1Font.HELVETICA,
                        10,
                        MARGIN,
                        y,
                        "Sin datos para mostrar"
                );
            } else {
                int index = 0;
                for (Map.Entry<String, Object> entry : safeData.entrySet()) {
                    if (y < FOOTER_HEIGHT + 60) {
                        cs.close();
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = drawHeader(
                                cs,
                                page,
                                title,
                                "Consulta individual"
                        ) - 20;
                    }

                    boolean even = index % 2 == 0;
                    drawKeyValueRow(
                            cs,
                            MARGIN,
                            y,
                            page.getMediaBox().getWidth() - (MARGIN * 2),
                            safe(entry.getKey()),
                            safe(entry.getValue()),
                            even
                    );
                    y -= 24;
                    index++;
                }
            }

            drawFooter(
                    cs,
                    page,
                    doc.getNumberOfPages(),
                    doc.getNumberOfPages()
            );

            cs.close();
            addPageNumbers(doc);
            doc.save(out);
            return out.toByteArray();
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalStateException(
                                        "No se pudo generar PDF: " +
                                                        e.getClass().getSimpleName() +
                                                        " - " +
                                                        e.getMessage(),
                                        e
                        );
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

            PDRectangle pageSize = PDRectangle.LETTER;
            boolean landscape = needsLandscape(safeRows);
            if (landscape) {
                pageSize = new PDRectangle(
                        PDRectangle.LETTER.getHeight(),
                        PDRectangle.LETTER.getWidth()
                );
            }

            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float pageWidth = page.getMediaBox().getWidth();
            float usableWidth = pageWidth - (MARGIN * 2);

            float y = drawHeader(
                    cs,
                    page,
                    title,
                    "Reporte administrativo"
            );

            y -= 8;
            y = drawReportSummary(
                    cs,
                    MARGIN,
                    y,
                    usableWidth,
                    safeRows.size()
            );

            y -= 14;

            if (safeRows.isEmpty()) {
                writeText(
                        cs,
                        PDType1Font.HELVETICA,
                        10,
                        MARGIN,
                        y,
                        "Sin registros para mostrar"
                );
                cs.close();
                addPageNumbers(doc);
                doc.save(out);
                return out.toByteArray();
            }

            List<String> columns = extractColumns(safeRows);
            float fontSize = resolveFontSize(columns.size());
            float headerFontSize = Math.max(fontSize, 7f);
            float rowHeight = 22f;
            float[] widths = resolveColumnWidths(
                    usableWidth,
                    columns
            );

            y = drawTableHeader(
                    cs,
                    MARGIN,
                    y,
                    columns,
                    widths,
                    headerFontSize
            );

            int rowIndex = 0;
            for (Map<String, Object> row : safeRows) {
                if (y - rowHeight < FOOTER_HEIGHT + 20) {
                    cs.close();
                    page = new PDPage(pageSize);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = drawHeader(
                            cs,
                            page,
                            title,
                            "Reporte administrativo"
                    );
                    y -= 18;
                    y = drawTableHeader(
                            cs,
                            MARGIN,
                            y,
                            columns,
                            widths,
                            headerFontSize
                    );
                }

                drawTableRow(
                        cs,
                        MARGIN,
                        y,
                        row,
                        columns,
                        widths,
                        fontSize,
                        rowIndex
                );
                y -= rowHeight;
                rowIndex++;
            }
            cs.close();
            addPageNumbers(doc);
            doc.save(out);
            return out.toByteArray();
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalStateException(
                                        "No se pudo generar PDF: " +
                                                        e.getClass().getSimpleName() +
                                                        " - " +
                                                        e.getMessage(),
                                        e
                        );
        }
    }

    private boolean needsLandscape(
            List<Map<String, Object>> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        return extractColumns(rows).size() > 5;
    }

    private float drawHeader(
            PDPageContentStream cs,
            PDPage page,
            String title,
            String type
    ) throws Exception {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        float y = pageHeight - MARGIN;

        cs.setLineWidth(1.1f);
        cs.addRect(
                MARGIN,
                y - HEADER_HEIGHT + 10,
                pageWidth - (MARGIN * 2),
                HEADER_HEIGHT
        );
        cs.stroke();

        writeText(
                cs,
                PDType1Font.HELVETICA_BOLD,
                16,
                MARGIN + 14,
                y - 12,
                EMPRESA
        );
        writeText(
                cs,
                PDType1Font.HELVETICA,
                9,
                MARGIN + 14,
                y - 28,
                SUBTITULO
        );
        writeText(
                cs,
                PDType1Font.HELVETICA_BOLD,
                12,
                MARGIN + 14,
                y - 52,
                safe(title).toUpperCase()
        );
        writeText(
                cs,
                PDType1Font.HELVETICA,
                8,
                pageWidth - MARGIN - 185,
                y - 14,
                "Tipo: " + safe(type)
        );
        writeText(
                cs,
                PDType1Font.HELVETICA,
                8,
                pageWidth - MARGIN - 185,
                y - 28,
                "Generado: " + LocalDateTime.now().format(DATE_TIME_FORMAT)
        );

        return y - HEADER_HEIGHT - 6;
    }

    private float drawReportSummary(
            PDPageContentStream cs,
            float x,
            float y,
            float width,
            int totalRows
    ) throws Exception {
        cs.setLineWidth(0.6f);
        cs.addRect(
                x,
                y - 22,
                width,
                24
        );
        cs.stroke();
        writeText(
                cs,
                PDType1Font.HELVETICA_BOLD,
                9,
                x + 10,
                y - 8,
                "Resumen:"
        );
        writeText(
                cs,
                PDType1Font.HELVETICA,
                9,
                x + 80,
                y - 8,
                "Total de registros: " + totalRows
        );
        return y - 34;
    }

    private void drawSectionTitle(
            PDPageContentStream cs,
            String title,
            float x,
            float y
    ) throws Exception {
        cs.setLineWidth(0.7f);
        cs.addRect(
                x,
                y - 14,
                504,
                20
        );
        cs.stroke();
        writeText(
                cs,
                PDType1Font.HELVETICA_BOLD,
                10,
                x + 8,
                y - 7,
                safe(title)
        );
    }

    private void drawKeyValueRow(
            PDPageContentStream cs,
            float x,
            float y,
            float width,
            String key,
            String value,
            boolean even
    ) throws Exception {
        cs.setLineWidth(0.3f);
        cs.addRect(
                x,
                y - 16,
                width,
                22
        );
        cs.stroke();
        float keyWidth = width * 0.32f;
        cs.addRect(
                x,
                y - 16,
                keyWidth,
                22
        );
        cs.stroke();
        writeText(
                cs,
                PDType1Font.HELVETICA_BOLD,
                8,
                x + 8,
                y - 7,
                truncate(key, 38)
        );
        writeText(
                cs,
                PDType1Font.HELVETICA,
                8,
                x + keyWidth + 8,
                y - 7,
                truncate(value, 85)
        );
    }

    private float drawTableHeader(
            PDPageContentStream cs,
            float x,
            float y,
            List<String> columns,
            float[] widths,
            float fontSize
    ) throws Exception {
        float rowHeight = 24f;
        float currentX = x;
        cs.setLineWidth(0.7f);
        for (int i = 0; i < columns.size(); i++) {
            cs.addRect(
                    currentX,
                    y - rowHeight,
                    widths[i],
                    rowHeight
            );
            cs.stroke();
            writeText(
                    cs,
                    PDType1Font.HELVETICA_BOLD,
                    fontSize,
                    currentX + 4,
                    y - 14,
                    truncate(
                            humanizeColumn(columns.get(i)),
                            maxChars(widths[i], fontSize)
                    )
            );
            currentX += widths[i];
        }
        return y - rowHeight;
    }

    private void drawTableRow(
            PDPageContentStream cs,
            float x,
            float y,
            Map<String, Object> row,
            List<String> columns,
            float[] widths,
            float fontSize,
            int rowIndex
    ) throws Exception {
        float rowHeight = 22f;
        float currentX = x;
        cs.setLineWidth(0.25f);
        for (int i = 0; i < columns.size(); i++) {
            cs.addRect(
                    currentX,
                    y - rowHeight,
                    widths[i],
                    rowHeight
            );
            cs.stroke();
            Object value = row.get(columns.get(i));
            writeText(
                    cs,
                    PDType1Font.HELVETICA,
                    fontSize,
                    currentX + 4,
                    y - 14,
                    truncate(
                            safe(value),
                            maxChars(widths[i], fontSize)
                    )
            );
            currentX += widths[i];
        }
    }

    private void drawFooter(
            PDPageContentStream cs,
            PDPage page,
            int currentPage,
            int totalPages
    ) throws Exception {
        float y = 26;
        float width = page.getMediaBox().getWidth();
        cs.setLineWidth(0.4f);
        cs.moveTo(MARGIN, y + 12);
        cs.lineTo(width - MARGIN, y + 12);
        cs.stroke();
        writeText(
                cs,
                PDType1Font.HELVETICA,
                7,
                MARGIN,
                y,
                "Documento generado automaticamente por Aeropuerto Los Primos."
        );
        writeText(
                cs,
                PDType1Font.HELVETICA,
                7,
                width - MARGIN - 85,
                y,
                "Pagina " + currentPage + " de " + totalPages
        );
    }

    private void addPageNumbers(
            PDDocument doc
    ) throws Exception {
        int total = doc.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            PDPage page = doc.getPage(i);
            try (PDPageContentStream cs = new PDPageContentStream(
                    doc,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true
            )) {
                drawFooter(
                        cs,
                        page,
                        i + 1,
                        total
                );
            }
        }
    }

    private List<String> extractColumns(
            List<Map<String, Object>> rows
    ) {
        LinkedHashSet<String> columns = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            if (row != null) {
                columns.addAll(row.keySet());
            }
        }
        return columns.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(c -> !c.isBlank())
                .toList();
    }

    private float[] resolveColumnWidths(
            float totalWidth,
            List<String> columns
    ) {
        int count = Math.max(columns.size(), 1);
        float[] widths = new float[count];
        float defaultWidth = totalWidth / count;
        for (int i = 0; i < count; i++) {
            widths[i] = defaultWidth;
        }
        return widths;
    }

    private float resolveFontSize(
            int columns
    ) {
        if (columns <= 4) {
            return 8.5f;
        }
        if (columns <= 6) {
            return 7.2f;
        }
        if (columns <= 8) {
            return 6.2f;
        }
        return 5.4f;
    }

    private int maxChars(
            float width,
            float fontSize
    ) {
        float approxCharWidth = fontSize * 0.48f;
        int chars = (int) ((width - 8) / approxCharWidth);
        return Math.max(chars, 6);
    }

    private String humanizeColumn(
            String column
    ) {
        if (column == null || column.isBlank()) {
            return "-";
        }
        String normalized = column
                .replace("_", " ")
                .replace("-", " ")
                .trim();
        StringBuilder sb = new StringBuilder();
        for (String part : normalized.split(" ")) {
            if (part.isBlank()) {
                continue;
            }
            sb.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) {
                sb.append(part.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private void writeText(
            PDPageContentStream cs,
            org.apache.pdfbox.pdmodel.font.PDFont font,
            float size,
            float x,
            float y,
            String text
    ) throws Exception {
                String safeText = sanitizeForPdf(text);

                if (safeText == null || safeText.isBlank()) {
                        safeText = "-";
                }

        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
                cs.showText(safeText);
        cs.endText();
    }

    private String safe(
            Object value
    ) {
        if (value == null) {
            return "-";
        }
        String text = String.valueOf(value)
                .replace("\t", " ")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
        return text.isBlank() ? "-" : text;
    }

    private String sanitizeForPdf(
            String value
    ) {
                if (value == null) {
                        return "-";
                }

                String text = String.valueOf(value)
                                .replace("\t", " ")
                                .replace("\r", " ")
                                .replace("\n", " ")
                                .replace("á", "a")
                                .replace("é", "e")
                                .replace("í", "i")
                                .replace("ó", "o")
                                .replace("ú", "u")
                                .replace("Á", "A")
                                .replace("É", "E")
                                .replace("Í", "I")
                                .replace("Ó", "O")
                                .replace("Ú", "U")
                                .replace("ñ", "n")
                                .replace("Ñ", "N")
                                .replace("ü", "u")
                                .replace("Ü", "U")
                                .replace("→", "->")
                                .replace("←", "<-")
                                .replace("–", "-")
                                .replace("—", "-")
                                .replace("“", "\"")
                                .replace("”", "\"")
                                .replace("‘", "'")
                                .replace("’", "'")
                                .replace("°", " grados ")
                                .replace("²", "2")
                                .replace("³", "3");

                text = text.replaceAll("[^\\x20-\\x7E]", "");

                if (text.isBlank()) {
                        return "-";
                }

                return text;
    }

    private String truncate(
            String value,
            int max
    ) {
        String safeValue = safe(value);
        if (safeValue.length() <= max) {
            return safeValue;
        }
        if (max <= 3) {
            return safeValue.substring(0, max);
        }
        return safeValue.substring(0, max - 3) + "...";
    }
}
