package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionCompletaResponse;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionRequest;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionResponse;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionSugerenciaResponse;
import com.aeropuertolosprimos.backend.model.Avion;
import com.aeropuertolosprimos.backend.model.ClaseVuelo;
import com.aeropuertolosprimos.backend.model.ConfigClaseFilasAvion;
import com.aeropuertolosprimos.backend.model.ModeloAvion;
import com.aeropuertolosprimos.backend.repository.AvionRepository;
import com.aeropuertolosprimos.backend.repository.ClaseVueloRepository;
import com.aeropuertolosprimos.backend.repository.ConfigClaseFilasAvionRepository;
import com.aeropuertolosprimos.backend.repository.ModeloAvionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigClaseFilasAvionService {


    private static final String ECONOMICA = "ECONOMICA";
    private static final String EJECUTIVA = "EJECUTIVA";
    private static final String INHABILITADO = "INHABILITADO";

    private static final List<String> CLASES_CONFIGURABLES =
            List.of(
                    ECONOMICA,
                    EJECUTIVA
            );

    private final ConfigClaseFilasAvionRepository configRepository;
    private final AvionRepository avionRepository;
    private final ClaseVueloRepository claseVueloRepository;
    private final ModeloAvionRepository modeloAvionRepository;

    private final EstadoAvionCatalogService estadoAvionCatalogService;

    private final AsientoUbiSyncService asientoUbiSyncService;

    private final CatalogoEstadoService catalogoEstadoService;

    public List<ConfigClaseFilasAvionCompletaResponse> listarConfiguracionesAvionesActivos(
            String q
    ) {
        Integer estadoActivoId =
                obtenerEstadoActivoId();

        String busqueda =
                limpiarBusqueda(q);

        List<Avion> avionesActivos =
                avionRepository.findByEstadoId(estadoActivoId);

        return avionesActivos
                .stream()
                .filter(avion ->
                        coincideBusquedaAvion(
                                avion,
                                busqueda
                        )
                )
                .map(this::construirConfiguracionCompletaDesdeAvion)
                .toList();
    }

    public ConfigClaseFilasAvionCompletaResponse obtenerConfiguracionCompleta(
            Integer avionId
    ) {
        Avion avion =
                obtenerAvion(avionId);

        validarAvionActivo(avion);

        return construirConfiguracionCompletaDesdeAvion(avion);
    }

    @Transactional
    public ConfigClaseFilasAvionCompletaResponse crearRango(
            Integer avionId,
            ConfigClaseFilasAvionRequest request
    ) {
        return guardarRangoDinamico(
                avionId,
                null,
                request
        );
    }

    @Transactional
    public ConfigClaseFilasAvionCompletaResponse actualizarRango(
            Integer rangoId,
            ConfigClaseFilasAvionRequest request
    ) {
        ConfigClaseFilasAvion rangoActual =
                obtenerConfigPorId(rangoId);

        if (Boolean.FALSE.equals(rangoActual.getActivo())) {
            throw new RuntimeException(
                    "El rango seleccionado ya se encuentra inactivo."
            );
        }

        return guardarRangoDinamico(
                rangoActual.getAvionId(),
                rangoActual.getId(),
                request
        );
    }

    @Transactional
    public ConfigClaseFilasAvionCompletaResponse eliminarRango(
            Integer rangoId
    ) {
        ConfigClaseFilasAvion rango =
                obtenerConfigPorId(rangoId);

        if (Boolean.FALSE.equals(rango.getActivo())) {
            throw new RuntimeException(
                    "El rango seleccionado ya se encuentra inactivo."
            );
        }

        Avion avion =
                obtenerAvion(rango.getAvionId());

        validarAvionActivo(avion);
        validarQuePuedeModificarConfiguracion(avion);
        validarAvionNoEsteAsignado(avion, "eliminar");

        rango.setActivo(false);

        configRepository.save(rango);

        asientoUbiSyncService.sincronizarPorAvion(avion.getId());

        return construirConfiguracionCompletaDesdeAvion(avion);
    }

    @Transactional
    public ConfigClaseFilasAvionCompletaResponse reiniciarConfiguracionCompleta(
            Integer avionId
    ) {
        Avion avion =
                obtenerAvion(avionId);

        validarAvionActivo(avion);
        validarQuePuedeModificarConfiguracion(avion);
        validarAvionNoEsteAsignado(avion, "reiniciar");

        List<ConfigClaseFilasAvion> activas =
                configRepository.findByAvionIdAndActivoTrue(avionId);

        for (ConfigClaseFilasAvion activa : activas) {
            activa.setActivo(false);
        }

        configRepository.saveAll(activas);

        asientoUbiSyncService.sincronizarPorAvion(avionId);

        return construirConfiguracionCompletaDesdeAvion(avion);
    }

    public ConfigClaseFilasAvionSugerenciaResponse sugerirSiguienteRango(
            Integer avionId,
            String claseBase,
            Integer filaDesde,
            Integer filaHasta
    ) {
        Avion avion =
                obtenerAvion(avionId);

        validarAvionActivo(avion);

        if (claseBase == null || claseBase.isBlank()) {
            throw new RuntimeException(
                    "Debe enviar la clase base."
            );
        }

        String claseBaseNormalizada =
                normalizar(claseBase);

        if (!CLASES_CONFIGURABLES.contains(claseBaseNormalizada)) {
            throw new RuntimeException(
                    "Solo se puede sugerir usando ECONOMICA o EJECUTIVA."
            );
        }

        validarRangoBasico(
                filaDesde,
                filaHasta,
                avion.getFilasConfiguradas()
        );

        String claseSugerida =
                EJECUTIVA.equals(claseBaseNormalizada)
                        ? ECONOMICA
                        : EJECUTIVA;

        List<ConfigClaseFilasAvion> activas =
                obtenerConfiguracionesVendiblesActivas(
                        avion.getId()
                );

        Rango rangoSugerido =
                calcularPrimerRangoLibreDespues(
                        avion.getFilasConfiguradas(),
                        filaHasta,
                        activas
                );

        ConfigClaseFilasAvionSugerenciaResponse response =
                new ConfigClaseFilasAvionSugerenciaResponse();

        response.setAvionId(
                avion.getId()
        );

        response.setFilasConfiguradas(
                avion.getFilasConfiguradas()
        );

        response.setClaseBase(
                claseBaseNormalizada
        );

        response.setFilaDesdeBase(
                filaDesde
        );

        response.setFilaHastaBase(
                filaHasta
        );

        response.setClaseSugerida(
                claseSugerida
        );

        if (rangoSugerido == null) {
            response.setFilaDesdeSugerida(null);
            response.setFilaHastaSugerida(null);
            response.setMensaje(
                    "No quedan filas libres para sugerir otro rango."
            );
        } else {
            response.setFilaDesdeSugerida(
                    rangoSugerido.desde()
            );

            response.setFilaHastaSugerida(
                    rangoSugerido.hasta()
            );

            response.setMensaje(
                    "Sugerencia generada correctamente."
            );
        }

        return response;
    }

    private ConfigClaseFilasAvionCompletaResponse guardarRangoDinamico(
            Integer avionId,
            Integer rangoEditarId,
            ConfigClaseFilasAvionRequest request
    ) {
        Avion avion =
                obtenerAvion(avionId);

        validarAvionActivo(avion);
        validarQuePuedeModificarConfiguracion(avion);
        validarAvionNoEsteAsignado(avion, "crear/actualizar");

        validarRequestRango(
                request,
                avion.getFilasConfiguradas()
        );

        ClaseVuelo claseNueva =
                obtenerClaseVuelo(
                        request.getClaseVueloId()
                );

        String nombreClaseNueva =
                normalizar(
                        claseNueva.getNombre()
                );

        if (!CLASES_CONFIGURABLES.contains(nombreClaseNueva)) {
            throw new RuntimeException(
                    "Solo se pueden configurar las clases ECONOMICA y EJECUTIVA."
            );
        }

        if (rangoEditarId != null) {
            ConfigClaseFilasAvion rangoActual =
                    obtenerConfigPorId(rangoEditarId);

            if (!rangoActual.getAvionId().equals(avionId)) {
                throw new RuntimeException(
                        "El rango no pertenece al avión indicado."
                );
            }

            if (Boolean.FALSE.equals(rangoActual.getActivo())) {
                throw new RuntimeException(
                        "El rango seleccionado ya se encuentra inactivo."
                );
            }

            rangoActual.setActivo(false);

            configRepository.save(rangoActual);
        }

        List<ConfigClaseFilasAvion> activas =
                obtenerConfiguracionesVendiblesActivas(
                        avionId
                );

        List<ConfigClaseFilasAvion> rangosModificados =
                new ArrayList<>();

        List<ConfigClaseFilasAvion> partesNuevas =
                new ArrayList<>();

        for (ConfigClaseFilasAvion activa : activas) {
            if (!hayCruce(
                    activa.getFilaDesde(),
                    activa.getFilaHasta(),
                    request.getFilaDesde(),
                    request.getFilaHasta()
            )) {
                continue;
            }

            activa.setActivo(false);

            rangosModificados.add(activa);

            partesNuevas.addAll(
                    dividirRangoAnteriorContraNuevo(
                            activa,
                            request.getFilaDesde(),
                            request.getFilaHasta()
                    )
            );
        }

        configRepository.saveAll(rangosModificados);
        configRepository.saveAll(partesNuevas);

        ConfigClaseFilasAvion nuevo =
                new ConfigClaseFilasAvion();

        nuevo.setAvionId(avionId);
        nuevo.setClaseVueloId(request.getClaseVueloId());
        nuevo.setFilaDesde(request.getFilaDesde());
        nuevo.setFilaHasta(request.getFilaHasta());
        nuevo.setActivo(true);

        configRepository.save(nuevo);

        asientoUbiSyncService.sincronizarPorAvion(avionId);

        return construirConfiguracionCompletaDesdeAvion(avion);
    }

    private List<ConfigClaseFilasAvion> dividirRangoAnteriorContraNuevo(
            ConfigClaseFilasAvion anterior,
            Integer nuevoDesde,
            Integer nuevoHasta
    ) {
        List<ConfigClaseFilasAvion> partes =
                new ArrayList<>();

        if (anterior.getFilaDesde() < nuevoDesde) {
            ConfigClaseFilasAvion izquierda =
                    copiarRango(
                            anterior,
                            anterior.getFilaDesde(),
                            nuevoDesde - 1
                    );

            partes.add(izquierda);
        }

        if (anterior.getFilaHasta() > nuevoHasta) {
            ConfigClaseFilasAvion derecha =
                    copiarRango(
                            anterior,
                            nuevoHasta + 1,
                            anterior.getFilaHasta()
                    );

            partes.add(derecha);
        }

        return partes;
    }

    private ConfigClaseFilasAvion copiarRango(
            ConfigClaseFilasAvion original,
            Integer filaDesde,
            Integer filaHasta
    ) {
        ConfigClaseFilasAvion copia =
                new ConfigClaseFilasAvion();

        copia.setAvionId(
                original.getAvionId()
        );

        copia.setClaseVueloId(
                original.getClaseVueloId()
        );

        copia.setFilaDesde(
                filaDesde
        );

        copia.setFilaHasta(
                filaHasta
        );

        copia.setActivo(true);

        return copia;
    }

    private ConfigClaseFilasAvionCompletaResponse construirConfiguracionCompletaDesdeAvion(
            Avion avion
    ) {
        List<ClaseVuelo> clasesConfigurables =
                obtenerClasesConfigurablesObligatorias();

        Map<Integer, ClaseVuelo> clasesPorId =
                construirMapaClasesPorId(
                        clasesConfigurables
                );

        List<ConfigClaseFilasAvion> activasVendibles =
                obtenerConfiguracionesVendiblesActivas(
                        avion.getId()
                )
                        .stream()
                        .sorted(
                                comparadorConfiguraciones(
                                        clasesPorId
                                )
                        )
                        .toList();

        boolean configurado =
                !activasVendibles.isEmpty();

        List<ConfigClaseFilasAvionResponse> detalle =
                new ArrayList<>();

        detalle.addAll(
                activasVendibles
                        .stream()
                        .map(config ->
                                convertirAResponse(
                                        config,
                                        clasesPorId
                                )
                        )
                        .toList()
        );

        agregarClasesSinRangoSiFaltan(
                avion.getId(),
                clasesConfigurables,
                activasVendibles,
                clasesPorId,
                detalle
        );

        List<Rango> rangosInhabilitados =
                calcularRangosInhabilitados(
                        avion.getFilasConfiguradas(),
                        activasVendibles
                );

        for (Rango rango : rangosInhabilitados) {
            detalle.add(
                    crearResponseInhabilitado(
                            avion.getId(),
                            rango
                    )
            );
        }

        detalle.sort(
                Comparator
                        .comparingInt(
                                ConfigClaseFilasAvionService::obtenerOrdenResponse
                        )
                        .thenComparing(response ->
                                response.getFilaDesde() == null
                                        ? Integer.MAX_VALUE
                                        : response.getFilaDesde()
                        )
        );

        return construirResponseCompleta(
                avion,
                configurado,
                detalle,
                rangosInhabilitados
        );
    }

    private List<ConfigClaseFilasAvion> obtenerConfiguracionesVendiblesActivas(
            Integer avionId
    ) {
        List<ClaseVuelo> clasesConfigurables =
                obtenerClasesConfigurablesObligatorias();

        Map<Integer, ClaseVuelo> clasesPorId =
                construirMapaClasesPorId(
                        clasesConfigurables
                );

        return configRepository
                .findByAvionIdAndActivoTrueOrderByFilaDesdeAsc(
                        avionId
                )
                .stream()
                .filter(config ->
                        esConfiguracionVendibleValida(
                                config,
                                clasesPorId
                        )
                )
                .toList();
    }

    private boolean esConfiguracionVendibleValida(
            ConfigClaseFilasAvion config,
            Map<Integer, ClaseVuelo> clasesPorId
    ) {
        if (config.getClaseVueloId() == null) {
            return false;
        }

        ClaseVuelo clase =
                clasesPorId.get(
                        config.getClaseVueloId()
                );

        if (clase == null) {
            return false;
        }

        String nombreClase =
                normalizar(
                        clase.getNombre()
                );

        return CLASES_CONFIGURABLES.contains(nombreClase)
                && config.getFilaDesde() != null
                && config.getFilaHasta() != null
                && Boolean.TRUE.equals(config.getActivo());
    }

    private void agregarClasesSinRangoSiFaltan(
            Integer avionId,
            List<ClaseVuelo> clasesConfigurables,
            List<ConfigClaseFilasAvion> activasVendibles,
            Map<Integer, ClaseVuelo> clasesPorId,
            List<ConfigClaseFilasAvionResponse> detalle
    ) {
        for (ClaseVuelo clase : clasesConfigurables) {
            String nombreClase =
                    normalizar(
                            clase.getNombre()
                    );

            boolean existeClase =
                    activasVendibles
                            .stream()
                            .anyMatch(config -> {
                                ClaseVuelo claseConfig =
                                        clasesPorId.get(
                                                config.getClaseVueloId()
                                        );

                                return claseConfig != null
                                        && normalizar(
                                        claseConfig.getNombre()
                                ).equals(nombreClase);
                            });

            if (!existeClase) {
                detalle.add(
                        crearResponseClaseSinRango(
                                avionId,
                                clase
                        )
                );
            }
        }
    }

    private List<Rango> calcularRangosInhabilitados(
            Integer filasConfiguradas,
            List<ConfigClaseFilasAvion> configuracionesVendibles
    ) {
        List<Rango> rangos =
                new ArrayList<>();

        if (filasConfiguradas == null || filasConfiguradas <= 0) {
            return rangos;
        }

        boolean[] filasOcupadas =
                new boolean[filasConfiguradas + 1];

        for (ConfigClaseFilasAvion config : configuracionesVendibles) {
            for (int fila = config.getFilaDesde(); fila <= config.getFilaHasta(); fila++) {
                if (fila >= 1 && fila <= filasConfiguradas) {
                    filasOcupadas[fila] = true;
                }
            }
        }

        int filaActual = 1;

        while (filaActual <= filasConfiguradas) {
            if (filasOcupadas[filaActual]) {
                filaActual++;
                continue;
            }

            int inicio = filaActual;

            while (filaActual <= filasConfiguradas && !filasOcupadas[filaActual]) {
                filaActual++;
            }

            int fin = filaActual - 1;

            rangos.add(
                    new Rango(
                            inicio,
                            fin
                    )
            );
        }

        return rangos;
    }

    private Rango calcularPrimerRangoLibreDespues(
            Integer filasConfiguradas,
            Integer filaHastaBase,
            List<ConfigClaseFilasAvion> configuracionesVendibles
    ) {
        if (filasConfiguradas == null || filasConfiguradas <= 0) {
            return null;
        }

        boolean[] ocupadas =
                new boolean[filasConfiguradas + 1];

        for (ConfigClaseFilasAvion config : configuracionesVendibles) {
            for (int fila = config.getFilaDesde(); fila <= config.getFilaHasta(); fila++) {
                if (fila >= 1 && fila <= filasConfiguradas) {
                    ocupadas[fila] = true;
                }
            }
        }

        int filaActual =
                filaHastaBase + 1;

        while (filaActual <= filasConfiguradas && ocupadas[filaActual]) {
            filaActual++;
        }

        if (filaActual > filasConfiguradas) {
            return null;
        }

        int inicio =
                filaActual;

        while (filaActual <= filasConfiguradas && !ocupadas[filaActual]) {
            filaActual++;
        }

        int fin =
                filaActual - 1;

        return new Rango(
                inicio,
                fin
        );
    }

    private boolean hayCruce(
            Integer desdeA,
            Integer hastaA,
            Integer desdeB,
            Integer hastaB
    ) {
        return desdeA <= hastaB && hastaA >= desdeB;
    }

    private void validarRequestRango(
            ConfigClaseFilasAvionRequest request,
            Integer filasConfiguradas
    ) {
        if (request == null) {
            throw new RuntimeException(
                    "Debe enviar los datos del rango."
            );
        }

        if (request.getClaseVueloId() == null) {
            throw new RuntimeException(
                    "Debe seleccionar una clase de vuelo."
            );
        }

        validarRangoBasico(
                request.getFilaDesde(),
                request.getFilaHasta(),
                filasConfiguradas
        );
    }

    private void validarRangoBasico(
            Integer filaDesde,
            Integer filaHasta,
            Integer filasConfiguradas
    ) {
        if (filaDesde == null || filaHasta == null) {
            throw new RuntimeException(
                    "Debe ingresar fila inicial y fila final."
            );
        }

        if (filaDesde <= 0) {
            throw new RuntimeException(
                    "La fila inicial debe ser mayor a 0."
            );
        }

        if (filaHasta < filaDesde) {
            throw new RuntimeException(
                    "La fila final no puede ser menor que la fila inicial."
            );
        }

        if (filasConfiguradas == null || filasConfiguradas <= 0) {
            throw new RuntimeException(
                    "El avión no tiene filas configuradas correctamente."
            );
        }

        if (filaHasta > filasConfiguradas) {
            throw new RuntimeException(
                    "El rango de filas supera las filas configuradas del avión."
            );
        }
    }

    private Avion obtenerAvion(
            Integer avionId
    ) {
        if (avionId == null) {
            throw new RuntimeException(
                    "Debe enviar el ID del avión."
            );
        }

        return avionRepository
                .findById(avionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El avión seleccionado no existe."
                        )
                );
    }

    private ConfigClaseFilasAvion obtenerConfigPorId(
            Integer id
    ) {
        if (id == null) {
            throw new RuntimeException(
                    "Debe enviar el ID del rango."
            );
        }

        return configRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "El rango seleccionado no existe."
                        )
                );
    }

    private ClaseVuelo obtenerClaseVuelo(
            Integer claseVueloId
    ) {
        return claseVueloRepository
                .findById(claseVueloId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "La clase de vuelo seleccionada no existe."
                        )
                );
    }

    private void validarAvionActivo(
            Avion avion
    ) {
        Integer estadoActivoId =
                obtenerEstadoActivoId();

        if (avion.getEstadoId() == null ||
                !avion.getEstadoId().equals(estadoActivoId)) {

            throw new RuntimeException(
                    "El avión seleccionado no está activo."
            );
        }
    }

    private void validarQuePuedeModificarConfiguracion(
            Avion avion
    ) {
        if (avion.getCantidadVuelos() != null &&
                avion.getCantidadVuelos() > 0) {

            throw new RuntimeException(
                    "No se puede modificar la configuración de filas porque el avión ya tiene vuelos asociados."
            );
        }
    }

    private void validarAvionNoEsteAsignado(Avion avion, String accion) {
        String nombreEstado = estadoAvionCatalogService.getNombreById(avion.getEstadoAvionId());

        if ("ASIGNADO".equals(nombreEstado)) {
            throw new RuntimeException(
                    String.format("No se puede %s la configuración de filas porque el avión está en estado ASIGNADO. " +
                                    "Solo se permite en DISPONIBLE, MANTENIMIENTO o FUERA_SERVICIO.",
                            accion)
            );
        }
    }

    private Integer obtenerEstadoActivoId() {
        return catalogoEstadoService.obtenerActivoId();
    }

    private List<ClaseVuelo> obtenerClasesConfigurablesObligatorias() {
        List<ClaseVuelo> clases =
                claseVueloRepository.findAllByOrderByNombreAsc();

        List<ClaseVuelo> configurables =
                clases
                        .stream()
                        .filter(clase ->
                                CLASES_CONFIGURABLES.contains(
                                        normalizar(
                                                clase.getNombre()
                                        )
                                )
                        )
                        .sorted(Comparator.comparingInt(clase ->
                                obtenerOrdenClase(
                                        normalizar(
                                                clase.getNombre()
                                        )
                                )
                        ))
                        .toList();

        Map<String, ClaseVuelo> mapa =
                construirMapaClasesPorNombre(
                        configurables
                );

        if (!mapa.containsKey(ECONOMICA)) {
            throw new RuntimeException(
                    "No existe la clase de vuelo ECONOMICA."
            );
        }

        if (!mapa.containsKey(EJECUTIVA)) {
            throw new RuntimeException(
                    "No existe la clase de vuelo EJECUTIVA."
            );
        }

        return configurables;
    }

    private Map<Integer, ClaseVuelo> construirMapaClasesPorId(
            List<ClaseVuelo> clases
    ) {
        Map<Integer, ClaseVuelo> mapa =
                new HashMap<>();

        for (ClaseVuelo clase : clases) {
            mapa.put(
                    clase.getId(),
                    clase
            );
        }

        return mapa;
    }

    private Map<String, ClaseVuelo> construirMapaClasesPorNombre(
            List<ClaseVuelo> clases
    ) {
        Map<String, ClaseVuelo> mapa =
                new HashMap<>();

        for (ClaseVuelo clase : clases) {
            mapa.put(
                    normalizar(
                            clase.getNombre()
                    ),
                    clase
            );
        }

        return mapa;
    }

    private ConfigClaseFilasAvionCompletaResponse construirResponseCompleta(
            Avion avion,
            Boolean configurado,
            List<ConfigClaseFilasAvionResponse> detalle,
            List<Rango> rangosInhabilitados
    ) {
        ConfigClaseFilasAvionCompletaResponse response =
                new ConfigClaseFilasAvionCompletaResponse();

        ModeloAvion modelo =
                modeloAvionRepository
                        .findById(
                                avion.getModeloAvionId()
                        )
                        .orElse(null);

        response.setAvionId(
                avion.getId()
        );

        response.setCodigoAvion(
                avion.getCodigoAvion()
        );

        response.setModeloAvionId(
                avion.getModeloAvionId()
        );

        if (modelo != null) {
            response.setModeloFabricante(
                    modelo.getFabricante()
            );

            response.setModeloCodigo(
                    modelo.getCodigoModelo()
            );

            response.setModeloNombre(
                    modelo.getNombre()
            );
        }

        response.setFilasConfiguradas(
                avion.getFilasConfiguradas()
        );

        response.setConfigurado(
                configurado
        );

        response.setConfiguraciones(
                detalle
        );

        response.setFilasInhabilitadasAutomaticas(
                rangosInhabilitados
                        .stream()
                        .map(this::formatearRango)
                        .toList()
        );

        return response;
    }

    private ConfigClaseFilasAvionResponse crearResponseClaseSinRango(
            Integer avionId,
            ClaseVuelo clase
    ) {
        ConfigClaseFilasAvionResponse response =
                new ConfigClaseFilasAvionResponse();

        response.setId(null);
        response.setAvionId(avionId);
        response.setClaseVueloId(clase.getId());
        response.setClaseVueloNombre(
                normalizar(
                        clase.getNombre()
                )
        );
        response.setFilaDesde(null);
        response.setFilaHasta(null);
        response.setActivo(false);

        return response;
    }

    private ConfigClaseFilasAvionResponse crearResponseInhabilitado(
            Integer avionId,
            Rango rango
    ) {
        ConfigClaseFilasAvionResponse response =
                new ConfigClaseFilasAvionResponse();

        response.setId(null);
        response.setAvionId(avionId);
        response.setClaseVueloId(null);
        response.setClaseVueloNombre(INHABILITADO);
        response.setFilaDesde(rango.desde());
        response.setFilaHasta(rango.hasta());
        response.setActivo(true);

        return response;
    }

    private ConfigClaseFilasAvionResponse convertirAResponse(
            ConfigClaseFilasAvion config,
            Map<Integer, ClaseVuelo> clasesPorId
    ) {
        ConfigClaseFilasAvionResponse response =
                new ConfigClaseFilasAvionResponse();

        response.setId(
                config.getId()
        );

        response.setAvionId(
                config.getAvionId()
        );

        response.setClaseVueloId(
                config.getClaseVueloId()
        );

        ClaseVuelo clase =
                clasesPorId.get(
                        config.getClaseVueloId()
                );

        if (clase != null) {
            response.setClaseVueloNombre(
                    normalizar(
                            clase.getNombre()
                    )
            );
        }

        response.setFilaDesde(
                config.getFilaDesde()
        );

        response.setFilaHasta(
                config.getFilaHasta()
        );

        response.setActivo(
                config.getActivo()
        );

        response.setCreatedAt(
                config.getCreatedAt()
        );

        response.setUpdatedAt(
                config.getUpdatedAt()
        );

        return response;
    }

    private Comparator<ConfigClaseFilasAvion> comparadorConfiguraciones(
            Map<Integer, ClaseVuelo> clasesPorId
    ) {
        return Comparator
                .comparingInt((ConfigClaseFilasAvion config) -> {
                    ClaseVuelo clase =
                            clasesPorId.get(
                                    config.getClaseVueloId()
                            );

                    if (clase == null) {
                        return 99;
                    }

                    return obtenerOrdenClase(
                            normalizar(
                                    clase.getNombre()
                            )
                    );
                })
                .thenComparing(config ->
                        config.getFilaDesde() == null
                                ? Integer.MAX_VALUE
                                : config.getFilaDesde()
                );
    }

    private static int obtenerOrdenResponse(
            ConfigClaseFilasAvionResponse response
    ) {
        if (response.getClaseVueloNombre() == null) {
            return 99;
        }

        return switch (response.getClaseVueloNombre()) {
            case EJECUTIVA -> 1;
            case ECONOMICA -> 2;
            case INHABILITADO -> 3;
            default -> 99;
        };
    }

    private int obtenerOrdenClase(
            String nombreClase
    ) {
        if (EJECUTIVA.equals(nombreClase)) {
            return 1;
        }

        if (ECONOMICA.equals(nombreClase)) {
            return 2;
        }

        if (INHABILITADO.equals(nombreClase)) {
            return 3;
        }

        return 99;
    }

    private boolean coincideBusquedaAvion(
            Avion avion,
            String busqueda
    ) {
        if (busqueda == null || busqueda.isBlank()) {
            return true;
        }

        String q =
                normalizar(busqueda);

        if (contieneNormalizado(
                avion.getCodigoAvion(),
                q
        )) {
            return true;
        }

        if (contieneNormalizado(
                avion.getNumeroSerie(),
                q
        )) {
            return true;
        }

        if (avion.getModeloAvionId() == null) {
            return false;
        }

        return modeloAvionRepository
                .findById(avion.getModeloAvionId())
                .map(modelo ->
                        contieneNormalizado(
                                modelo.getFabricante(),
                                q
                        )
                                || contieneNormalizado(
                                modelo.getCodigoModelo(),
                                q
                        )
                                || contieneNormalizado(
                                modelo.getNombre(),
                                q
                        )
                )
                .orElse(false);
    }

    private boolean contieneNormalizado(
            String texto,
            String busquedaNormalizada
    ) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        return normalizar(texto)
                .contains(busquedaNormalizada);
    }

    private String formatearRango(
            Rango rango
    ) {
        if (rango.desde().equals(rango.hasta())) {
            return String.valueOf(
                    rango.desde()
            );
        }

        return rango.desde() + " - " + rango.hasta();
    }

    private String limpiarBusqueda(
            String valor
    ) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizar(
            String texto
    ) {
        if (texto == null) {
            return "";
        }

        return Normalizer
                .normalize(
                        texto,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private record Rango(
            Integer desde,
            Integer hasta
    ) {
    }
}