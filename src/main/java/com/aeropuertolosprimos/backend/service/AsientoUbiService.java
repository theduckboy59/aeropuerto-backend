package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AsientoUbiResponse;
import com.aeropuertolosprimos.backend.dto.GenerarAsientosResponse;
import com.aeropuertolosprimos.backend.dto.LimpiarAsientosResponse;
import com.aeropuertolosprimos.backend.model.AsientoUbi;
import com.aeropuertolosprimos.backend.model.Avion;
import com.aeropuertolosprimos.backend.model.ConfigClaseFilasAvion;
import com.aeropuertolosprimos.backend.model.ModeloAvion;
import com.aeropuertolosprimos.backend.model.TipoAsiento;
import com.aeropuertolosprimos.backend.repository.AsientoUbiRepository;
import com.aeropuertolosprimos.backend.repository.AvionRepository;
import com.aeropuertolosprimos.backend.repository.ConfigClaseFilasAvionRepository;
import com.aeropuertolosprimos.backend.repository.ModeloAvionRepository;
import com.aeropuertolosprimos.backend.repository.TipoAsientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsientoUbiService {

    private final AsientoUbiRepository asientoUbiRepository;
    private final AvionRepository avionRepository;
    private final ModeloAvionRepository modeloAvionRepository;
    private final ConfigClaseFilasAvionRepository configClaseFilasAvionRepository;
    private final TipoAsientoRepository tipoAsientoRepository;

    public Page<AsientoUbiResponse> buscarConFiltros(
            Integer avionId,
            Integer claseVueloId,
            Integer tipoAsientoId,
            Integer nivel,
            Integer fila,
            String columna,
            String numeroAsiento,
            Pageable pageable
    ) {
        return asientoUbiRepository
                .buscarConFiltros(
                        avionId,
                        claseVueloId,
                        tipoAsientoId,
                        nivel,
                        fila,
                        limpiarTexto(columna),
                        limpiarTexto(numeroAsiento),
                        pageable
                )
                .map(this::convertirAResponse);
    }

    public AsientoUbiResponse buscarPorId(Integer id) {
        if (id == null) {
            throw new RuntimeException("Debe enviar el ID del asiento.");
        }

        AsientoUbi asiento = asientoUbiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El asiento no existe."));

        return convertirAResponse(asiento);
    }

    @Transactional
    public GenerarAsientosResponse generarAsientos(Integer avionId, Boolean regenerar) {
        if (avionId == null) {
            throw new RuntimeException("Debe enviar el ID del avión.");
        }

        boolean debeRegenerar = Boolean.TRUE.equals(regenerar);

        Avion avion = avionRepository.findById(avionId)
                .orElseThrow(() -> new RuntimeException("El avión seleccionado no existe."));

        validarAvion(avion);

        ModeloAvion modelo = modeloAvionRepository.findById(avion.getModeloAvionId())
                .orElseThrow(() -> new RuntimeException("El modelo del avión no existe."));

        validarModeloContraAvion(modelo, avion);

        long asientosActuales = asientoUbiRepository.countByAvionId(avionId);

        if (asientosActuales > 0 && !debeRegenerar) {
            throw new RuntimeException("El avión ya tiene asientos generados. Si desea regenerarlos, use regenerar=true.");
        }

        if (asientosActuales > 0 && debeRegenerar) {
            validarQuePuedeModificarAsientos(avion);
            asientoUbiRepository.deleteByAvionId(avionId);
        }

        List<ConfigClaseFilasAvion> configuraciones = configClaseFilasAvionRepository
                .findByAvionIdAndActivoTrueOrderByFilaDesdeAsc(avionId);

        if (configuraciones == null || configuraciones.isEmpty()) {
            throw new RuntimeException("El avión no tiene configuración activa de filas por clase.");
        }

        Integer[] clasePorFila = construirMapaClasePorFila(
                avion.getFilasConfiguradas(),
                configuraciones
        );

        List<Integer> bloques = obtenerBloques(modelo.getConfiguracion());

        validarBloquesContraModelo(modelo, bloques);

        Integer tipoVentanaId = obtenerTipoAsientoId("VENTANA");
        Integer tipoPasilloId = obtenerTipoAsientoId("PASILLO");
        Integer tipoMedioId = obtenerTipoAsientoId("MEDIO");

        List<AsientoUbi> asientos = new ArrayList<>();

        int totalColumnas = calcularTotalColumnas(bloques);

        for (int nivel = 1; nivel <= modelo.getNiveles(); nivel++) {
            for (int fila = 1; fila <= avion.getFilasConfiguradas(); fila++) {

                int columnaGlobal = 1;

                for (int indiceBloque = 0; indiceBloque < bloques.size(); indiceBloque++) {
                    int cantidadAsientosBloque = bloques.get(indiceBloque);
                    int bloque = indiceBloque + 1;

                    for (int posicionBloque = 1; posicionBloque <= cantidadAsientosBloque; posicionBloque++) {
                        String columna = convertirNumeroAColumna(columnaGlobal);
                        String numeroAsiento = fila + columna;

                        AsientoUbi asiento = new AsientoUbi();
                        asiento.setAvionId(avionId);
                        asiento.setClaseVueloId(clasePorFila[fila]);
                        asiento.setTipoAsientoId(obtenerTipoPorColumna(
                                columnaGlobal,
                                bloques,
                                tipoVentanaId,
                                tipoPasilloId,
                                tipoMedioId
                        ));
                        asiento.setNivel(nivel);
                        asiento.setFila(fila);
                        asiento.setColumna(columna);
                        asiento.setNumeroAsiento(numeroAsiento);
                        asiento.setBloque(bloque);
                        asiento.setLado(obtenerLado(indiceBloque, bloques.size()));

                        asientos.add(asiento);

                        columnaGlobal++;
                    }
                }
            }
        }

        asientoUbiRepository.saveAll(asientos);

        GenerarAsientosResponse response = new GenerarAsientosResponse();
        response.setAvionId(avionId);
        response.setModeloAvionId(avion.getModeloAvionId());
        response.setNiveles(modelo.getNiveles());
        response.setFilasConfiguradas(avion.getFilasConfiguradas());
        response.setTotalColumnas(totalColumnas);
        response.setTotalAsientosGenerados(asientos.size());
        response.setMensaje("Asientos generados correctamente.");

        return response;
    }

    @Transactional
    public LimpiarAsientosResponse limpiarAsientosPorAvion(Integer avionId) {
        if (avionId == null) {
            throw new RuntimeException("Debe enviar el ID del avión.");
        }

        Avion avion = avionRepository.findById(avionId)
                .orElseThrow(() -> new RuntimeException("El avión seleccionado no existe."));

        validarQuePuedeModificarAsientos(avion);

        long totalAsientos = asientoUbiRepository.countByAvionId(avionId);

        if (totalAsientos == 0) {
            throw new RuntimeException("El avión no tiene asientos generados.");
        }

        asientoUbiRepository.deleteByAvionId(avionId);

        LimpiarAsientosResponse response = new LimpiarAsientosResponse();
        response.setAvionId(avionId);
        response.setAsientosEliminados(totalAsientos);
        response.setMensaje("Asientos eliminados correctamente.");

        return response;
    }

    private void validarAvion(Avion avion) {
        if (avion.getModeloAvionId() == null) {
            throw new RuntimeException("El avión no tiene modelo asignado.");
        }

        if (avion.getFilasConfiguradas() == null || avion.getFilasConfiguradas() <= 0) {
            throw new RuntimeException("El avión no tiene filas configuradas correctamente.");
        }
    }

    private void validarModeloContraAvion(ModeloAvion modelo, Avion avion) {
        if (modelo.getNiveles() == null || modelo.getNiveles() <= 0) {
            throw new RuntimeException("El modelo del avión no tiene niveles configurados correctamente.");
        }

        if (modelo.getConfiguracion() == null || modelo.getConfiguracion().trim().isEmpty()) {
            throw new RuntimeException("El modelo del avión no tiene configuración de asientos.");
        }

        if (modelo.getFilasMin() != null && avion.getFilasConfiguradas() < modelo.getFilasMin()) {
            throw new RuntimeException("Las filas configuradas del avión son menores al mínimo permitido por el modelo.");
        }

        if (modelo.getFilasMax() != null && avion.getFilasConfiguradas() > modelo.getFilasMax()) {
            throw new RuntimeException("Las filas configuradas del avión son mayores al máximo permitido por el modelo.");
        }
    }

    private void validarBloquesContraModelo(ModeloAvion modelo, List<Integer> bloques) {
        int totalColumnasCalculadas = calcularTotalColumnas(bloques);
        int pasillosCalculados = bloques.size() - 1;

        if (modelo.getTotalColumnas() != null && !modelo.getTotalColumnas().equals(totalColumnasCalculadas)) {
            throw new RuntimeException("La configuración del modelo no coincide con el total de columnas.");
        }

        if (modelo.getPasillos() != null && !modelo.getPasillos().equals(pasillosCalculados)) {
            throw new RuntimeException("La configuración del modelo no coincide con la cantidad de pasillos.");
        }
    }

    private void validarQuePuedeModificarAsientos(Avion avion) {
        if (avion.getCantidadVuelos() != null && avion.getCantidadVuelos() > 0) {
            throw new RuntimeException("No se pueden modificar los asientos porque el avión ya tiene vuelos asociados.");
        }
    }

    private Integer[] construirMapaClasePorFila(
            Integer filasConfiguradas,
            List<ConfigClaseFilasAvion> configuraciones
    ) {
        Integer[] clasePorFila = new Integer[filasConfiguradas + 1];

        for (ConfigClaseFilasAvion config : configuraciones) {
            if (config.getClaseVueloId() == null) {
                throw new RuntimeException("Existe una configuración sin clase de vuelo.");
            }

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

        for (int fila = 1; fila <= filasConfiguradas; fila++) {
            if (clasePorFila[fila] == null) {
                throw new RuntimeException("La fila " + fila + " no tiene clase configurada.");
            }
        }

        return clasePorFila;
    }

    private List<Integer> obtenerBloques(String configuracion) {
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

    private Integer obtenerTipoAsientoId(String nombre) {
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

    private String obtenerLado(int indiceBloque, int totalBloques) {
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

    private int calcularTotalColumnas(List<Integer> bloques) {
        int total = 0;

        for (Integer bloque : bloques) {
            total += bloque;
        }

        return total;
    }

    private String convertirNumeroAColumna(int numero) {
        StringBuilder columna = new StringBuilder();

        while (numero > 0) {
            numero--;
            char letra = (char) ('A' + (numero % 26));
            columna.insert(0, letra);
            numero = numero / 26;
        }

        return columna.toString();
    }

    private String limpiarTexto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim().toUpperCase();
    }

    private AsientoUbiResponse convertirAResponse(AsientoUbi asiento) {
        AsientoUbiResponse response = new AsientoUbiResponse();

        response.setId(asiento.getId());
        response.setAvionId(asiento.getAvionId());
        response.setClaseVueloId(asiento.getClaseVueloId());
        response.setTipoAsientoId(asiento.getTipoAsientoId());
        response.setNivel(asiento.getNivel());
        response.setFila(asiento.getFila());
        response.setColumna(asiento.getColumna());
        response.setNumeroAsiento(asiento.getNumeroAsiento());
        response.setBloque(asiento.getBloque());
        response.setLado(asiento.getLado());
        response.setCreatedAt(asiento.getCreatedAt());
        response.setUpdatedAt(asiento.getUpdatedAt());

        return response;
    }
}