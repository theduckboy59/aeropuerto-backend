package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionCompletaResponse;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionRequest;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionSugerenciaResponse;
import com.aeropuertolosprimos.backend.service.ConfigClaseFilasAvionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config-clase-filas-avion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConfigClaseFilasAvionController {

    private final ConfigClaseFilasAvionService service;

    /*
     * Pantalla principal.
     *
     * Carga automáticamente todos los aviones activos con su configuración completa.
     * No hay "seleccionar avión" como paso principal.
     *
     * GET /config-clase-filas-avion/aviones-activos/completa
     * GET /config-clase-filas-avion/aviones-activos/completa?q=AV-0001
     */
    @GetMapping("/aviones-activos/completa")
    public List<ConfigClaseFilasAvionCompletaResponse> listarConfiguracionesAvionesActivos(
            @RequestParam(required = false) String q
    ) {
        return service.listarConfiguracionesAvionesActivos(q);
    }

    /*
     * Consulta/refresco de un avión activo específico.
     */
    @GetMapping("/avion/{avionId}/completa")
    public ConfigClaseFilasAvionCompletaResponse obtenerConfiguracionCompleta(
            @PathVariable Integer avionId
    ) {
        return service.obtenerConfiguracionCompleta(avionId);
    }

    /*
     * Crea un solo rango.
     *
     * El rango nuevo tiene prioridad.
     * Si se cruza con rangos anteriores, el backend los recorta,
     * divide o desactiva automáticamente.
     */
    @PostMapping("/avion/{avionId}/rango")
    public ConfigClaseFilasAvionCompletaResponse crearRango(
            @PathVariable Integer avionId,
            @RequestBody ConfigClaseFilasAvionRequest request
    ) {
        return service.crearRango(
                avionId,
                request
        );
    }

    /*
     * Edita un solo rango existente.
     *
     * El front envía un solo rango:
     * {
     *   "claseVueloId": 1,
     *   "filaDesde": 14,
     *   "filaHasta": 18
     * }
     */
    @PutMapping("/rango/{rangoId}")
    public ConfigClaseFilasAvionCompletaResponse actualizarRango(
            @PathVariable Integer rangoId,
            @RequestBody ConfigClaseFilasAvionRequest request
    ) {
        return service.actualizarRango(
                rangoId,
                request
        );
    }

    /*
     * Desactiva un solo rango.
     *
     * No borra físicamente.
     * Solo pone activo = false.
     */
    @DeleteMapping("/rango/{rangoId}")
    public ConfigClaseFilasAvionCompletaResponse eliminarRango(
            @PathVariable Integer rangoId
    ) {
        return service.eliminarRango(rangoId);
    }

    /*
     * Reinicia toda la configuración de un avión.
     *
     * No inactiva el avión.
     * No borra historial.
     * Solo pone activo = false a todos los rangos activos del avión.
     */
    @DeleteMapping("/avion/{avionId}/completa")
    public ConfigClaseFilasAvionCompletaResponse reiniciarConfiguracionCompleta(
            @PathVariable Integer avionId
    ) {
        return service.reiniciarConfiguracionCompleta(avionId);
    }

    /*
     * Sugiere el siguiente rango libre después del rango base.
     *
     * GET /config-clase-filas-avion/avion/1/sugerencia?claseBase=EJECUTIVA&filaDesde=1&filaHasta=5
     */
    @GetMapping("/avion/{avionId}/sugerencia")
    public ConfigClaseFilasAvionSugerenciaResponse sugerirSiguienteRango(
            @PathVariable Integer avionId,
            @RequestParam String claseBase,
            @RequestParam Integer filaDesde,
            @RequestParam Integer filaHasta
    ) {
        return service.sugerirSiguienteRango(
                avionId,
                claseBase,
                filaDesde,
                filaHasta
        );
    }
}