package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.GenerarAsientosResponse;
import com.aeropuertolosprimos.backend.model.AsientoUbi;
import com.aeropuertolosprimos.backend.model.Avion;
import com.aeropuertolosprimos.backend.model.ConfigClaseFilasAvion;
import com.aeropuertolosprimos.backend.model.ModeloAvion;
import com.aeropuertolosprimos.backend.model.TipoAsiento;
import com.aeropuertolosprimos.backend.repository.AsientoUbiRepository;
import com.aeropuertolosprimos.backend.repository.AsientoVueloRepository;
import com.aeropuertolosprimos.backend.repository.AvionRepository;
import com.aeropuertolosprimos.backend.repository.ClaseVueloRepository;
import com.aeropuertolosprimos.backend.repository.ConfigClaseFilasAvionRepository;
import com.aeropuertolosprimos.backend.repository.ModeloAvionRepository;
import com.aeropuertolosprimos.backend.repository.TipoAsientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AsientoUbiSyncService {

    private static final String ASIGNADO = "ASIGNADO";

    private static final String VENTANA = "VENTANA";
    private static final String PASILLO = "PASILLO";
    private static final String MEDIO = "MEDIO";

    private final AsientoUbiRepository asientoUbiRepository;
    private final AsientoVueloRepository asientoVueloRepository;
    private final AvionRepository avionRepository;
    private final ModeloAvionRepository modeloAvionRepository;
    private final ConfigClaseFilasAvionRepository configClaseFilasAvionRepository;
    private final TipoAsientoRepository tipoAsientoRepository;
    private final ClaseVueloRepository claseVueloRepository;
    private final EstadoAvionCatalogService estadoAvionCatalogService;



    @Transactional
    public GenerarAsientosResponse sincronizarPorAvion(
            Integer avionId
    ) {

        if (avionId == null) {
            throw new RuntimeException("Debe enviar el ID del avión.");
        }

        Avion avion = avionRepository.findById(avionId)
                .orElseThrow(() ->
                        new RuntimeException("El avión seleccionado no existe.")
                );

        validarAvionParaSincronizar(avion);
        validarAvionNoAsignado(avion);

        ModeloAvion modelo = modeloAvionRepository.findById(avion.getModeloAvionId())
                .orElseThrow(() ->
                        new RuntimeException("El modelo del avión no existe.")
                );

        validarModeloContraAvion(
                modelo,
                avion
        );

        List<Integer> bloques = obtenerBloques(
                modelo.getConfiguracion()
        );

        validarBloquesContraModelo(
                modelo,
                bloques
        );

        Integer[] clasePorFila = construirMapaClasePorFila(
                avion.getFilasConfiguradas(),
                configClaseFilasAvionRepository.findByAvionIdAndActivoTrueOrderByFilaDesdeAsc(avionId)
        );

        Integer tipoVentanaId = obtenerTipoAsientoId(VENTANA);
        Integer tipoPasilloId = obtenerTipoAsientoId(PASILLO);
        Integer tipoMedioId = obtenerTipoAsientoId(MEDIO);

        long asientosAnteriores = asientoUbiRepository.countByAvionId(avionId);

        List<AsientoUbi> asientosDeseados = generarAsientosFisicos(
                avion,
                modelo,
                bloques,
                clasePorFila,
                tipoVentanaId,
                tipoPasilloId,
                tipoMedioId
        );

        ResultadoSincronizacion resultado = sincronizarSinBorrar(
                avion,
                asientosDeseados
        );

        asientoUbiRepository.flush();

        long asientosActuales = asientoUbiRepository.countByAvionId(avionId);

        GenerarAsientosResponse response = new GenerarAsientosResponse();

        response.setAvionId(avionId);
        response.setModeloAvionId(avion.getModeloAvionId());
        response.setNiveles(modelo.getNiveles());
        response.setFilasConfiguradas(avion.getFilasConfiguradas());
        response.setTotalColumnas(calcularTotalColumnas(bloques));
        response.setTotalAsientosGenerados((int) asientosActuales);
        response.setMensaje(
                "Asientos sincronizados correctamente. Registros anteriores activos: "
                        + asientosAnteriores
                        + ". Actualizados: "
                        + resultado.actualizados
                        + ". Reutilizados: "
                        + resultado.reutilizados
                        + ". Creados: "
                        + resultado.creados
                        + ". Limpiados: "
                        + resultado.limpiados
                        + ". Registros actuales activos: "
                        + asientosActuales
                        + "."
        );

        return response;
    }

    private ResultadoSincronizacion sincronizarSinBorrar(
            Avion avion,
            List<AsientoUbi> asientosDeseados
    ) {

        List<AsientoUbi> actuales = asientoUbiRepository.findByAvionId(
                avion.getId()
        );

        Map<String, AsientoUbi> actualesPorClave = new HashMap<>();

        for (AsientoUbi actual : actuales) {
            String clave = claveAsiento(actual);

            if (clave != null) {
                actualesPorClave.put(clave, actual);
            }
        }

        List<AsientoUbi> reutilizables = asientoUbiRepository
                .findByAvionIdIsNullOrderByIdAsc();

        int indiceReutilizable = 0;

        Set<String> clavesDeseadas = new HashSet<>();

        List<AsientoUbi> guardar = new ArrayList<>();

        ResultadoSincronizacion resultado = new ResultadoSincronizacion();

        for (AsientoUbi deseado : asientosDeseados) {

            String clave = claveAsiento(deseado);

            if (clave == null) {
                continue;
            }

            clavesDeseadas.add(clave);

            AsientoUbi existente = actualesPorClave.get(clave);

            if (existente != null) {

                copiarDatosAsiento(
                        existente,
                        deseado
                );

                guardar.add(existente);
                resultado.actualizados++;

                continue;
            }

            if (indiceReutilizable < reutilizables.size()) {

                AsientoUbi reutilizado = reutilizables.get(
                        indiceReutilizable
                );

                indiceReutilizable++;

                copiarDatosAsiento(
                        reutilizado,
                        deseado
                );

                guardar.add(reutilizado);
                resultado.reutilizados++;

                continue;
            }

            guardar.add(deseado);
            resultado.creados++;
        }

        for (AsientoUbi actual : actuales) {

            String clave = claveAsiento(actual);

            if (clave == null) {
                continue;
            }

            if (!clavesDeseadas.contains(clave)) {

                if (actual.getCodigoAsientoSistema() != null &&
                        !actual.getCodigoAsientoSistema().isBlank()) {

                    asientoVueloRepository.limpiarPorCodigoAsientoSistema(
                            actual.getCodigoAsientoSistema()
                    );
                }

                limpiarAsientoUbi(
                        actual
                );

                guardar.add(actual);
                resultado.limpiados++;
            }
        }

        asientoUbiRepository.saveAll(
                guardar
        );

        return resultado;
    }

    private List<AsientoUbi> generarAsientosFisicos(
            Avion avion,
            ModeloAvion modelo,
            List<Integer> bloques,
            Integer[] clasePorFila,
            Integer tipoVentanaId,
            Integer tipoPasilloId,
            Integer tipoMedioId
    ) {

        List<AsientoUbi> asientos = new ArrayList<>();

        for (int nivel = 1; nivel <= modelo.getNiveles(); nivel++) {

            for (int fila = 1; fila <= avion.getFilasConfiguradas(); fila++) {

                int columnaGlobal = 1;

                for (int indiceBloque = 0; indiceBloque < bloques.size(); indiceBloque++) {

                    int bloque = indiceBloque + 1;
                    int cantidadAsientosBloque = bloques.get(indiceBloque);

                    for (int posicionBloque = 1; posicionBloque <= cantidadAsientosBloque; posicionBloque++) {

                        String columna = convertirNumeroAColumna(columnaGlobal);

                        AsientoUbi asiento = new AsientoUbi();

                        asiento.setAvionId(avion.getId());

                        asiento.setClaseVueloId(
                                clasePorFila[fila]
                        );

                        asiento.setTipoAsientoId(
                                obtenerTipoPorColumna(
                                        columnaGlobal,
                                        bloques,
                                        tipoVentanaId,
                                        tipoPasilloId,
                                        tipoMedioId
                                )
                        );

                        asiento.setNivel(nivel);
                        asiento.setFila(fila);
                        asiento.setColumna(columna);
                        asiento.setNumeroAsiento(fila + columna);

                        asiento.setCodigoAsientoSistema(
                                generarCodigoAsientoSistema(
                                        avion,
                                        nivel,
                                        fila,
                                        columna
                                )
                        );

                        asiento.setBloque(bloque);
                        asiento.setLado(
                                obtenerLado(
                                        indiceBloque,
                                        bloques.size()
                                )
                        );

                        asientos.add(asiento);

                        columnaGlobal++;
                    }
                }
            }
        }

        return asientos;
    }

    private void copiarDatosAsiento(
            AsientoUbi destino,
            AsientoUbi origen
    ) {

        destino.setAvionId(origen.getAvionId());
        destino.setClaseVueloId(origen.getClaseVueloId());
        destino.setTipoAsientoId(origen.getTipoAsientoId());
        destino.setNivel(origen.getNivel());
        destino.setFila(origen.getFila());
        destino.setColumna(origen.getColumna());
        destino.setNumeroAsiento(origen.getNumeroAsiento());
        destino.setCodigoAsientoSistema(origen.getCodigoAsientoSistema());
        destino.setBloque(origen.getBloque());
        destino.setLado(origen.getLado());
    }

    private void limpiarAsientoUbi(
            AsientoUbi asiento
    ) {

        asiento.setAvionId(null);
        asiento.setClaseVueloId(null);
        asiento.setTipoAsientoId(null);
        asiento.setNivel(null);
        asiento.setFila(null);
        asiento.setColumna(null);
        asiento.setNumeroAsiento(null);
        asiento.setCodigoAsientoSistema(null);
        asiento.setBloque(null);
        asiento.setLado(null);
    }

    private String claveAsiento(
            AsientoUbi asiento
    ) {

        if (
                asiento.getNivel() == null ||
                        asiento.getFila() == null ||
                        asiento.getColumna() == null
        ) {
            return null;
        }

        return asiento.getNivel()
                + "|"
                + asiento.getFila()
                + "|"
                + asiento.getColumna().trim().toUpperCase(Locale.ROOT);
    }

    private String generarCodigoAsientoSistema(
            Avion avion,
            Integer nivel,
            Integer fila,
            String columna
    ) {

        return "AV"
                + avion.getId()
                + "-N"
                + nivel
                + "-F"
                + fila
                + "-"
                + columna.trim().toUpperCase(Locale.ROOT);
    }

    private void validarAvionParaSincronizar(
            Avion avion
    ) {

        if (avion.getModeloAvionId() == null) {
            throw new RuntimeException("El avión no tiene modelo asignado.");
        }

        if (avion.getFilasConfiguradas() == null || avion.getFilasConfiguradas() <= 0) {
            throw new RuntimeException("El avión no tiene filas configuradas correctamente.");
        }

        if (avion.getEstadoAvionId() == null) {
            throw new RuntimeException("El avión no tiene estado operativo asignado.");
        }
    }

    private void validarAvionNoAsignado(
            Avion avion
    ) {

        String nombreEstado = normalizar(
                estadoAvionCatalogService.getNombreById(
                        avion.getEstadoAvionId()
                )
        );

        if (ASIGNADO.equals(nombreEstado)) {
            throw new RuntimeException(
                    "No se pueden sincronizar asientos porque el avión está en estado ASIGNADO."
            );
        }
    }

    private void validarModeloContraAvion(
            ModeloAvion modelo,
            Avion avion
    ) {

        if (modelo.getEstadoId() == null || modelo.getEstadoId() != 1) {
            throw new RuntimeException("No se puede usar un modelo de avión inactivo.");
        }

        if (modelo.getNiveles() == null || modelo.getNiveles() <= 0) {
            throw new RuntimeException("El modelo del avión no tiene niveles configurados correctamente.");
        }

        if (modelo.getConfiguracion() == null || modelo.getConfiguracion().trim().isEmpty()) {
            throw new RuntimeException("El modelo del avión no tiene configuración de asientos.");
        }

        if (modelo.getFilasMin() != null && avion.getFilasConfiguradas() < modelo.getFilasMin()) {
            throw new RuntimeException(
                    "Las filas configuradas del avión son menores al mínimo permitido por el modelo."
            );
        }

        if (modelo.getFilasMax() != null && avion.getFilasConfiguradas() > modelo.getFilasMax()) {
            throw new RuntimeException(
                    "Las filas configuradas del avión son mayores al máximo permitido por el modelo."
            );
        }
    }

    private Integer[] construirMapaClasePorFila(
            Integer filasConfiguradas,
            List<ConfigClaseFilasAvion> configuraciones
    ) {

        Integer[] clasePorFila = new Integer[filasConfiguradas + 1];

        if (configuraciones == null || configuraciones.isEmpty()) {
            return clasePorFila;
        }

        for (ConfigClaseFilasAvion config : configuraciones) {

            if (config.getClaseVueloId() == null) {
                throw new RuntimeException("Existe una configuración activa sin clase de vuelo.");
            }

            claseVueloRepository.findById(config.getClaseVueloId())
                    .orElseThrow(() ->
                            new RuntimeException("Existe una configuración con una clase de vuelo inexistente.")
                    );

            if (config.getFilaDesde() == null || config.getFilaHasta() == null) {
                throw new RuntimeException("Existe una configuración de clase con filas incompletas.");
            }

            if (config.getFilaDesde() <= 0) {
                throw new RuntimeException("La fila inicial debe ser mayor a 0.");
            }

            if (config.getFilaHasta() < config.getFilaDesde()) {
                throw new RuntimeException("La fila final no puede ser menor que la fila inicial.");
            }

            if (config.getFilaHasta() > filasConfiguradas) {
                throw new RuntimeException("Una configuración de clase supera las filas configuradas del avión.");
            }

            for (int fila = config.getFilaDesde(); fila <= config.getFilaHasta(); fila++) {

                if (clasePorFila[fila] != null) {
                    throw new RuntimeException("Existen rangos de clase cruzados para el avión.");
                }

                clasePorFila[fila] = config.getClaseVueloId();
            }
        }

        return clasePorFila;
    }

    private List<Integer> obtenerBloques(
            String configuracion
    ) {

        List<Integer> bloques = new ArrayList<>();

        String[] partes = configuracion.trim().split("-");

        for (String parte : partes) {

            try {

                int cantidad = Integer.parseInt(parte.trim());

                if (cantidad <= 0) {
                    throw new RuntimeException("La configuración del modelo contiene valores inválidos.");
                }

                bloques.add(cantidad);

            } catch (NumberFormatException e) {
                throw new RuntimeException("La configuración del modelo tiene formato inválido.");
            }
        }

        if (bloques.isEmpty()) {
            throw new RuntimeException("La configuración del modelo no contiene bloques de asientos.");
        }

        return bloques;
    }

    private void validarBloquesContraModelo(
            ModeloAvion modelo,
            List<Integer> bloques
    ) {

        int totalColumnasCalculadas = calcularTotalColumnas(bloques);
        int pasillosCalculados = bloques.size() - 1;

        if (modelo.getTotalColumnas() == null || !modelo.getTotalColumnas().equals(totalColumnasCalculadas)) {
            throw new RuntimeException("La configuración del modelo no coincide con el total de columnas.");
        }

        if (modelo.getPasillos() == null || !modelo.getPasillos().equals(pasillosCalculados)) {
            throw new RuntimeException("La configuración del modelo no coincide con la cantidad de pasillos.");
        }
    }

    private Integer obtenerTipoAsientoId(
            String nombre
    ) {

        TipoAsiento tipo = tipoAsientoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RuntimeException("No existe el tipo de asiento " + nombre + "."));

        return tipo.getId();
    }

    private Integer obtenerTipoPorColumna(
            int columnaGlobal,
            List<Integer> bloques,
            Integer tipoVentanaId,
            Integer tipoPasilloId,
            Integer tipoMedioId
    ) {

        int totalColumnas = calcularTotalColumnas(bloques);

        if (columnaGlobal == 1 || columnaGlobal == totalColumnas) {
            return tipoVentanaId;
        }

        int acumulado = 0;

        for (int i = 0; i < bloques.size() - 1; i++) {

            acumulado += bloques.get(i);

            int antesDelPasillo = acumulado;
            int despuesDelPasillo = acumulado + 1;

            if (columnaGlobal == antesDelPasillo || columnaGlobal == despuesDelPasillo) {
                return tipoPasilloId;
            }
        }

        return tipoMedioId;
    }

    private String obtenerLado(
            int indiceBloque,
            int totalBloques
    ) {

        if (totalBloques == 1) {
            return "CENTRO";
        }

        if (indiceBloque == 0) {
            return "IZQUIERDO";
        }

        if (indiceBloque == totalBloques - 1) {
            return "DERECHO";
        }

        return "CENTRO";
    }

    private int calcularTotalColumnas(
            List<Integer> bloques
    ) {

        int total = 0;

        for (Integer bloque : bloques) {
            total += bloque;
        }

        return total;
    }

    private String convertirNumeroAColumna(
            int numero
    ) {

        StringBuilder columna = new StringBuilder();

        while (numero > 0) {

            numero--;

            char letra = (char) ('A' + (numero % 26));

            columna.insert(0, letra);

            numero = numero / 26;
        }

        return columna.toString();
    }

    private String normalizar(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private static class ResultadoSincronizacion {

        private int actualizados;

        private int reutilizados;

        private int creados;

        private int limpiados;
    }
}