package com.aeropuertolosprimos.backend.util.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
public class ExcelExportService {

    public byte[] toXlsx(
            String sheetName,
            List<Map<String, Object>> rows
    ) {

        List<Map<String, Object>> safeRows =
                rows == null ? List.of() : rows;

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String safeSheet = (sheetName == null || sheetName.isBlank())
                    ? "Reporte"
                    : sheetName.trim();

            Sheet sheet = workbook.createSheet(
                    safeSheet.length() > 31
                            ? safeSheet.substring(0, 31)
                            : safeSheet
            );

            List<String> headers = buildHeaders(safeRows);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 0;

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (Map<String, Object> row : safeRows) {
                Row excelRow = sheet.createRow(rowIdx++);

                for (int i = 0; i < headers.size(); i++) {
                    String key = headers.get(i);
                    Object value = row != null ? row.get(key) : null;

                    Cell cell = excelRow.createCell(i);
                    cell.setCellValue(stringify(value));
                    cell.setCellStyle(cellStyle);
                }
            }

            for (int i = 0; i < Math.min(headers.size(), 30); i++) {
                sheet.autoSizeColumn(i);
                int w = Math.min(sheet.getColumnWidth(i) + 800, 12000);
                sheet.setColumnWidth(i, w);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar Excel", e);
        }
    }

    private List<String> buildHeaders(
            List<Map<String, Object>> rows
    ) {

        LinkedHashSet<String> headers = new LinkedHashSet<>();

        for (Map<String, Object> row : rows) {
            if (row == null) continue;
            headers.addAll(row.keySet());
        }

        if (headers.isEmpty()) {
            return List.of("mensaje");
        }

        return new ArrayList<>(headers);
    }

    private String stringify(
            Object value
    ) {
        if (value == null) return "";
        return String.valueOf(value);
    }
}

