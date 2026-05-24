package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.service.ReporteService;
import com.aeropuertolosprimos.backend.util.export.ExcelExportService;
import com.aeropuertolosprimos.backend.util.export.ExportFileName;
import com.aeropuertolosprimos.backend.util.export.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService service;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;

    @GetMapping("/consulta-vuelo/{codigoVuelo}")
    public Map<String, Object> consultaVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.consultaVuelo(codigoVuelo);
    }

    @GetMapping(value = "/consulta-vuelo/{codigoVuelo}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> consultaVueloPdf(
            @PathVariable String codigoVuelo
    ) {

        Map<String, Object> data = service.consultaVuelo(codigoVuelo);

        byte[] pdf = pdfExportService.simpleKeyValuePdf(
                "Consulta de vuelo: " + codigoVuelo,
                data
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("consulta_vuelo_" + codigoVuelo, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/vuelos")
    public List<Map<String, Object>> vuelosPorFechaHora(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaHasta
    ) {
        return service.vuelosPorFechaHora(
                fechaDesde,
                horaDesde,
                fechaHasta,
                horaHasta
        );
    }

    @GetMapping(value = "/vuelos/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> vuelosPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaHasta
    ) {

        List<Map<String, Object>> rows = service.vuelosPorFechaHora(
                fechaDesde,
                horaDesde,
                fechaHasta,
                horaHasta
        );

        byte[] pdf = pdfExportService.tablePdf(
                "Reporte de vuelos por fecha y hora",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_vuelos", "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/vuelos/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> vuelosExcel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime horaHasta
    ) {

        List<Map<String, Object>> rows = service.vuelosPorFechaHora(
                fechaDesde,
                horaDesde,
                fechaHasta,
                horaHasta
        );

        byte[] xlsx = excelExportService.toXlsx(
                "Vuelos",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_vuelos", "xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping("/pasajeros-vuelo/{codigoVuelo}")
    public List<Map<String, Object>> pasajerosPorVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.pasajerosPorVuelo(codigoVuelo);
    }

    @GetMapping(value = "/pasajeros-vuelo/{codigoVuelo}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pasajerosPorVueloPdf(
            @PathVariable String codigoVuelo
    ) {

        List<Map<String, Object>> rows = service.pasajerosPorVuelo(codigoVuelo);

        byte[] pdf = pdfExportService.tablePdf(
                "Reporte de pasajeros por vuelo: " + codigoVuelo,
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_pasajeros_" + codigoVuelo, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/pasajeros-vuelo/{codigoVuelo}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> pasajerosPorVueloExcel(
            @PathVariable String codigoVuelo
    ) {

        List<Map<String, Object>> rows = service.pasajerosPorVuelo(codigoVuelo);

        byte[] xlsx = excelExportService.toXlsx(
                "Pasajeros",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_pasajeros_" + codigoVuelo, "xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping("/equipaje-vuelo/{codigoVuelo}")
    public List<Map<String, Object>> equipajePorVuelo(
            @PathVariable String codigoVuelo
    ) {
        return service.equipajePorVuelo(codigoVuelo);
    }

    @GetMapping(value = "/equipaje-vuelo/{codigoVuelo}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> equipajePorVueloPdf(
            @PathVariable String codigoVuelo
    ) {

        List<Map<String, Object>> rows = service.equipajePorVuelo(codigoVuelo);

        byte[] pdf = pdfExportService.tablePdf(
                "Reporte de equipaje por vuelo: " + codigoVuelo,
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_equipaje_" + codigoVuelo, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/equipaje-vuelo/{codigoVuelo}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> equipajePorVueloExcel(
            @PathVariable String codigoVuelo
    ) {

        List<Map<String, Object>> rows = service.equipajePorVuelo(codigoVuelo);

        byte[] xlsx = excelExportService.toXlsx(
                "Equipaje",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_equipaje_" + codigoVuelo, "xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping("/aviones-aerolinea/{aerolineaId}")
    public List<Map<String, Object>> avionesPorAerolinea(
            @PathVariable Integer aerolineaId
    ) {
        return service.avionesPorAerolinea(aerolineaId);
    }

    @GetMapping(value = "/aviones-aerolinea/{aerolineaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> avionesPorAerolineaPdf(
            @PathVariable Integer aerolineaId
    ) {

        List<Map<String, Object>> rows = service.avionesPorAerolinea(aerolineaId);

        byte[] pdf = pdfExportService.tablePdf(
                "Reporte de aviones por aerolínea",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_aviones_aerolinea_" + aerolineaId, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/aviones-aerolinea/{aerolineaId}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> avionesPorAerolineaExcel(
            @PathVariable Integer aerolineaId
    ) {

        List<Map<String, Object>> rows = service.avionesPorAerolinea(aerolineaId);

        byte[] xlsx = excelExportService.toXlsx(
                "Aviones",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_aviones_aerolinea_" + aerolineaId, "xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping("/aerolineas-aeropuerto/{aeropuertoId}")
    public List<Map<String, Object>> aerolineasPorAeropuerto(
            @PathVariable Integer aeropuertoId
    ) {
        return service.aerolineasPorAeropuerto(aeropuertoId);
    }

    @GetMapping(value = "/aerolineas-aeropuerto/{aeropuertoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> aerolineasPorAeropuertoPdf(
            @PathVariable Integer aeropuertoId
    ) {

        List<Map<String, Object>> rows = service.aerolineasPorAeropuerto(aeropuertoId);

        byte[] pdf = pdfExportService.tablePdf(
                "Reporte de aerolíneas por aeropuerto",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_aerolineas_aeropuerto_" + aeropuertoId, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/aerolineas-aeropuerto/{aeropuertoId}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> aerolineasPorAeropuertoExcel(
            @PathVariable Integer aeropuertoId
    ) {

        List<Map<String, Object>> rows = service.aerolineasPorAeropuerto(aeropuertoId);

        byte[] xlsx = excelExportService.toXlsx(
                "Aerolineas",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_aerolineas_aeropuerto_" + aeropuertoId, "xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @GetMapping("/destinos-aerolinea/{aerolineaId}")
    public List<Map<String, Object>> destinosPorAerolinea(
            @PathVariable Integer aerolineaId
    ) {
        return service.destinosPorAerolinea(aerolineaId);
    }

    @GetMapping(value = "/destinos-aerolinea/{aerolineaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> destinosPorAerolineaPdf(
            @PathVariable Integer aerolineaId
    ) {

        List<Map<String, Object>> rows = service.destinosPorAerolinea(aerolineaId);

        byte[] pdf = pdfExportService.tablePdf(
                "Reporte de destinos autorizados por aerolínea",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_destinos_aerolinea_" + aerolineaId, "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/destinos-aerolinea/{aerolineaId}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> destinosPorAerolineaExcel(
            @PathVariable Integer aerolineaId
    ) {

        List<Map<String, Object>> rows = service.destinosPorAerolinea(aerolineaId);

        byte[] xlsx = excelExportService.toXlsx(
                "Destinos",
                rows
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + ExportFileName.withTimestamp("reporte_destinos_aerolinea_" + aerolineaId, "xlsx"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}
