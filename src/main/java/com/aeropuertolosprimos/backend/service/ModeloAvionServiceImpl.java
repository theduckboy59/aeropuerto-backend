package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ModeloAvionPreviewResponse;
import com.aeropuertolosprimos.backend.dto.ModeloAvionRequest;
import com.aeropuertolosprimos.backend.dto.ModeloAvionResponse;
import com.aeropuertolosprimos.backend.model.ModeloAvion;
import com.aeropuertolosprimos.backend.repository.ModeloAvionRepository;
import com.aeropuertolosprimos.backend.service.ModeloAvionService;
import com.aeropuertolosprimos.backend.specification.ModeloAvionSpecification;
import com.aeropuertolosprimos.backend.util.SeatConfigurationParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModeloAvionServiceImpl implements ModeloAvionService {

    private final ModeloAvionRepository repository;
    private final SeatConfigurationParser parser;
    private final CatalogoEstadoService catalogoEstadoService;

    @Override
    public Page<ModeloAvionResponse> findAll(
            String q,
            Integer niveles,
            Integer pasillos,
            String configuracion,
            Integer totalColumnas,
            Pageable pageable
    ) {

        return repository.findAll(
                        ModeloAvionSpecification.filters(
                                q,
                                niveles,
                                pasillos,
                                configuracion,
                                totalColumnas
                        ),
                        pageable
                )
                .map(this::mapResponse);
    }

    @Override
    public ModeloAvionResponse findById(Integer id) {

        ModeloAvion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Modelo avión no encontrado")
                );

        return mapResponse(entity);
    }

    @Override
    public ModeloAvionResponse create(ModeloAvionRequest request) {

        validateCreate(request);

        parser.parse(
                request.getConfiguracion(),
                request.getNiveles(),
                request.getTotalColumnas(),
                request.getPasillos()
        );

        ModeloAvion entity = new ModeloAvion();

        entity.setFabricante(request.getFabricante());
        entity.setCodigoModelo(request.getCodigoModelo());
        entity.setNombre(request.getNombre());
        entity.setNiveles(request.getNiveles());
        entity.setPasillos(request.getPasillos());
        entity.setConfiguracion(request.getConfiguracion());
        entity.setTotalColumnas(request.getTotalColumnas());
        entity.setFilasMin(request.getFilasMin());
        entity.setFilasMax(request.getFilasMax());
        entity.setEstadoId(
                request.getEstadoId() != null
                        ? request.getEstadoId()
                        : catalogoEstadoService.obtenerActivoId()
        );

        repository.save(entity);

        return mapResponse(entity);
    }

    @Override
    public ModeloAvionResponse update(Integer id, ModeloAvionRequest request) {

        ModeloAvion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Modelo avión no encontrado")
                );

        validateUpdate(id, request);

        parser.parse(
                request.getConfiguracion(),
                request.getNiveles(),
                request.getTotalColumnas(),
                request.getPasillos()
        );

        entity.setFabricante(request.getFabricante());
        entity.setCodigoModelo(request.getCodigoModelo());
        entity.setNombre(request.getNombre());
        entity.setNiveles(request.getNiveles());
        entity.setPasillos(request.getPasillos());
        entity.setConfiguracion(request.getConfiguracion());
        entity.setTotalColumnas(request.getTotalColumnas());
        entity.setFilasMin(request.getFilasMin());
        entity.setFilasMax(request.getFilasMax());
        if (request.getEstadoId() != null) {
            entity.setEstadoId(request.getEstadoId());
        }

        repository.save(entity);

        return mapResponse(entity);
    }

    @Override
    public void changeStatus(Integer id, Integer estadoId) {

        ModeloAvion entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Modelo avión no encontrado")
                );

        entity.setEstadoId(estadoId);

        repository.save(entity);
    }

    @Override
    public ModeloAvionPreviewResponse preview(
            ModeloAvionRequest request
    ) {

        return parser.parse(
                request.getConfiguracion(),
                request.getNiveles(),
                request.getTotalColumnas(),
                request.getPasillos()
        );
    }

    private void validateCreate(ModeloAvionRequest request) {

        if (request.getFilasMax() < request.getFilasMin()) {

            throw new RuntimeException(
                    "Filas máximas no puede ser menor a filas mínimas"
            );
        }

        if (repository.existsByFabricanteIgnoreCaseAndCodigoModeloIgnoreCase(
                request.getFabricante(),
                request.getCodigoModelo()
        )) {

            throw new RuntimeException(
                    "Ya existe un modelo con ese fabricante y código"
            );
        }
    }

    private void validateUpdate(
            Integer id,
            ModeloAvionRequest request
    ) {

        if (request.getFilasMax() < request.getFilasMin()) {

            throw new RuntimeException(
                    "Filas máximas no puede ser menor a filas mínimas"
            );
        }

        if (repository.existsDuplicateForUpdate(
                request.getFabricante(),
                request.getCodigoModelo(),
                id
        )) {

            throw new RuntimeException(
                    "Ya existe un modelo con ese fabricante y código"
            );
        }
    }

    private ModeloAvionResponse mapResponse(ModeloAvion entity) {

        return ModeloAvionResponse.builder()
                .id(entity.getId())
                .fabricante(entity.getFabricante())
                .codigoModelo(entity.getCodigoModelo())
                .nombre(entity.getNombre())
                .niveles(entity.getNiveles())
                .pasillos(entity.getPasillos())
                .configuracion(entity.getConfiguracion())
                .totalColumnas(entity.getTotalColumnas())
                .filasMin(entity.getFilasMin())
                .filasMax(entity.getFilasMax())
                .estadoId(entity.getEstadoId())
                .build();
    }
}