package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AvionRequest;
import com.aeropuertolosprimos.backend.dto.AvionResponse;
import com.aeropuertolosprimos.backend.model.Aerolinea;
import com.aeropuertolosprimos.backend.model.Avion;
import com.aeropuertolosprimos.backend.model.EstadoAvion;
import com.aeropuertolosprimos.backend.model.ModeloAvion;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import com.aeropuertolosprimos.backend.repository.AsientoUbiRepository;
import com.aeropuertolosprimos.backend.repository.AvionRepository;
import com.aeropuertolosprimos.backend.repository.EstadoAvionRepository;
import com.aeropuertolosprimos.backend.repository.ModeloAvionRepository;
import com.aeropuertolosprimos.backend.specification.AvionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AvionServiceImpl implements AvionService {

    private final AvionRepository repository;
    private final AerolineaRepository aerolineaRepository;
    private final EstadoAvionRepository estadoAvionRepository;
    private final ModeloAvionRepository modeloAvionRepository;
    private final AsientoUbiRepository asientoUbiRepository;
    private final EstadoAvionCatalogService estadoAvionCatalogService;
    private final AsientoUbiSyncService asientoUbiSyncService;

    private final CatalogoEstadoService catalogoEstadoService;

    @Override
    public Page<AvionResponse> findAll(
            String q,
            Integer aerolineaId,
            Integer estadoAvionId,
            Integer modeloAvionId,
            Integer estadoId,
            Integer anio,
            Pageable pageable
    ) {

        Integer estado =
                estadoId != null ? estadoId : catalogoEstadoService.obtenerActivoId();

        return repository.findAll(
                AvionSpecification.filters(
                        q,
                        aerolineaId,
                        estadoAvionId,
                        modeloAvionId,
                        estado,
                        anio
                ),
                pageable
        ).map(this::mapResponse);
    }

    @Override
    public AvionResponse findById(Integer id) {

        Avion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Avión no encontrado")
                );

        return mapResponse(entity);
    }

    @Override
    @Transactional
    public AvionResponse create(AvionRequest request) {

        Aerolinea aerolinea = findAerolinea(request.getAerolineaId());
        ModeloAvion modeloAvion = findModeloAvion(request.getModeloAvionId());

        findEstadoAvion(request.getEstadoAvionId());

        validateAerolineaIsActive(aerolinea);
        validateModelIsActive(modeloAvion);
        validateConfiguredRows(request.getFilasConfiguradas(), modeloAvion);

        Avion entity = new Avion();

        entity.setAerolineaId(request.getAerolineaId());
        entity.setEstadoAvionId(request.getEstadoAvionId());
        entity.setModeloAvionId(request.getModeloAvionId());

        entity.setCodigoAvion(generarCodigoAvion(aerolinea));

        entity.setNumeroSerie(request.getNumeroSerie());
        entity.setAnio(request.getAnio());
        entity.setFilasConfiguradas(request.getFilasConfiguradas());
        entity.setCantidadVuelos(0);
        entity.setEstadoId(
                request.getEstadoId() != null
                        ? request.getEstadoId()
                        : catalogoEstadoService.obtenerActivoId()
        );

        repository.save(entity);

        asientoUbiSyncService.sincronizarPorAvion(entity.getId());

        return mapResponse(entity);
    }

    @Override
    @Transactional
    public AvionResponse update(
            Integer id,
            AvionRequest request
    ) {

        Avion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Avión no encontrado")
                );

        boolean cambiandoFilas = !Objects.equals(
                entity.getFilasConfiguradas(),
                request.getFilasConfiguradas()
        );

        boolean cambiandoModelo = !Objects.equals(
                entity.getModeloAvionId(),
                request.getModeloAvionId()
        );

        boolean cambiaEstructuraAsientos = cambiandoFilas || cambiandoModelo;

        if (cambiandoFilas) {
            String nombreEstado = estadoAvionCatalogService.getNombreById(
                    entity.getEstadoAvionId()
            );

            if ("DISPONIBLE".equals(nombreEstado) || "ASIGNADO".equals(nombreEstado)) {
                throw new RuntimeException(
                        String.format(
                                "No se puede modificar las filas configuradas cuando el avión está en estado %s. Solo se permite en MANTENIMIENTO o FUERA_SERVICIO.",
                                nombreEstado
                        )
                );
            }
        }

        Aerolinea aerolinea = findAerolinea(request.getAerolineaId());
        ModeloAvion modeloAvion = findModeloAvion(request.getModeloAvionId());

        findEstadoAvion(request.getEstadoAvionId());

        validateAerolineaIsActive(aerolinea);
        validateModelIsActive(modeloAvion);
        validateConfiguredRows(request.getFilasConfiguradas(), modeloAvion);

        entity.setAerolineaId(request.getAerolineaId());
        entity.setEstadoAvionId(request.getEstadoAvionId());
        entity.setModeloAvionId(request.getModeloAvionId());

        if (entity.getCodigoAvion() == null ||
                entity.getCodigoAvion().isBlank()) {

            entity.setCodigoAvion(generarCodigoAvion(aerolinea));
        }

        entity.setNumeroSerie(request.getNumeroSerie());
        entity.setAnio(request.getAnio());
        entity.setFilasConfiguradas(request.getFilasConfiguradas());

        if (request.getEstadoId() != null) {
            entity.setEstadoId(request.getEstadoId());
        }

        repository.save(entity);

        if (cambiaEstructuraAsientos) {
            asientoUbiSyncService.sincronizarPorAvion(entity.getId());
        }

        return mapResponse(entity);
    }

    @Override
    @Transactional
    public void changeStatus(
            Integer id,
            Integer estadoId
    ) {

        Avion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Avión no encontrado")
                );

        entity.setEstadoId(estadoId);

        repository.save(entity);
    }

    @Override
    @Transactional
    public void changeOperationalStatus(
            Integer id,
            Integer estadoAvionId
    ) {

        Avion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Avión no encontrado")
                );

        findEstadoAvion(estadoAvionId);

        entity.setEstadoAvionId(estadoAvionId);

        repository.save(entity);
    }

    private Aerolinea findAerolinea(Integer id) {

        return aerolineaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Aerolínea no encontrada")
                );
    }

    private EstadoAvion findEstadoAvion(Integer id) {

        return estadoAvionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Estado de avión no encontrado")
                );
    }

    private ModeloAvion findModeloAvion(Integer id) {

        return modeloAvionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Modelo de avión no encontrado")
                );
    }

    private void validateAerolineaIsActive(Aerolinea aerolinea) {

        if (!Objects.equals(aerolinea.getEstadoId(), catalogoEstadoService.obtenerActivoId())) {
            throw new RuntimeException(
                    "No se puede usar una aerolínea inactiva"
            );
        }
    }

    private void validateModelIsActive(ModeloAvion modeloAvion) {

        if (!Objects.equals(modeloAvion.getEstadoId(), catalogoEstadoService.obtenerActivoId())) {
            throw new RuntimeException(
                    "No se puede usar un modelo de avión inactivo"
            );
        }
    }

    private void validateConfiguredRows(
            Integer filasConfiguradas,
            ModeloAvion modeloAvion
    ) {

        if (filasConfiguradas < modeloAvion.getFilasMin()) {
            throw new RuntimeException(
                    "Las filas configuradas no pueden ser menores a " +
                            modeloAvion.getFilasMin()
            );
        }

        if (filasConfiguradas > modeloAvion.getFilasMax()) {
            throw new RuntimeException(
                    "Las filas configuradas no pueden ser mayores a " +
                            modeloAvion.getFilasMax()
            );
        }
    }

    private String generarCodigoAvion(Aerolinea aerolinea) {

        String prefijo = "AVN";

        if (aerolinea.getCodigoIata() != null &&
                !aerolinea.getCodigoIata().isBlank()) {

            prefijo = aerolinea.getCodigoIata()
                    .trim()
                    .toUpperCase();
        }

        int correlativo = 1;

        String codigo;

        do {
            codigo = prefijo + "-" + String.format("%04d", correlativo);
            correlativo++;
        } while (repository.existsByCodigoAvionIgnoreCase(codigo));

        return codigo;
    }

    private void validateStructuralChange(
            Avion entity,
            AvionRequest request
    ) {

        boolean changedModel = !Objects.equals(
                entity.getModeloAvionId(),
                request.getModeloAvionId()
        );

        boolean changedRows = !Objects.equals(
                entity.getFilasConfiguradas(),
                request.getFilasConfiguradas()
        );

        if (entity.getCantidadVuelos() != null
                && entity.getCantidadVuelos() > 0
                && (changedModel || changedRows)) {

            throw new RuntimeException(
                    "No se puede modificar la estructura de un avión que ya tiene vuelos"
            );
        }
    }

    private AvionResponse mapResponse(Avion entity) {

        Aerolinea aerolinea = findAerolinea(entity.getAerolineaId());
        EstadoAvion estadoAvion = findEstadoAvion(entity.getEstadoAvionId());
        ModeloAvion modeloAvion = findModeloAvion(entity.getModeloAvionId());

        return AvionResponse.builder()
                .id(entity.getId())

                .aerolineaId(entity.getAerolineaId())
                .aerolineaNombre(aerolinea.getNombre())

                .estadoAvionId(entity.getEstadoAvionId())
                .estadoAvionNombre(estadoAvion.getNombre())

                .modeloAvionId(entity.getModeloAvionId())
                .modeloFabricante(modeloAvion.getFabricante())
                .modeloCodigo(modeloAvion.getCodigoModelo())
                .modeloNombre(modeloAvion.getNombre())

                .codigoAvion(entity.getCodigoAvion())
                .numeroSerie(entity.getNumeroSerie())
                .anio(entity.getAnio())
                .filasConfiguradas(entity.getFilasConfiguradas())
                .cantidadAsientos(asientoUbiRepository.countByAvionId(entity.getId()))
                .cantidadVuelos(entity.getCantidadVuelos())
                .estadoId(entity.getEstadoId())
                .build();
    }
}