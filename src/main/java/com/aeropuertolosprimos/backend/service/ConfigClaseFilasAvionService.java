package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionRequest;
import com.aeropuertolosprimos.backend.dto.ConfigClaseFilasAvionResponse;
import com.aeropuertolosprimos.backend.model.Avion;
import com.aeropuertolosprimos.backend.model.ConfigClaseFilasAvion;
import com.aeropuertolosprimos.backend.repository.AvionRepository;
import com.aeropuertolosprimos.backend.repository.ConfigClaseFilasAvionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigClaseFilasAvionService {

    private final ConfigClaseFilasAvionRepository configRepository;
    private final AvionRepository avionRepository;

    public ConfigClaseFilasAvionResponse registrar(ConfigClaseFilasAvionRequest request) {
        validarRequest(request, null);

        ConfigClaseFilasAvion config = new ConfigClaseFilasAvion();
        config.setAvionId(request.getAvionId());
        config.setClaseVueloId(request.getClaseVueloId());
        config.setFilaDesde(request.getFilaDesde());
        config.setFilaHasta(request.getFilaHasta());

        if (request.getActivo() != null) {
            config.setActivo(request.getActivo());
        }

        ConfigClaseFilasAvion guardado = configRepository.save(config);

        return convertirAResponse(guardado);
    }

    public Page<ConfigClaseFilasAvionResponse> buscarConFiltros(
            Integer avionId,
            Integer claseVueloId,
            Boolean activo,
            Pageable pageable
    ) {
        return configRepository
                .buscarConFiltros(avionId, claseVueloId, activo, pageable)
                .map(this::convertirAResponse);
    }

    public ConfigClaseFilasAvionResponse buscarPorId(Integer id) {
        ConfigClaseFilasAvion config = obtenerConfigPorId(id);
        return convertirAResponse(config);
    }

    public ConfigClaseFilasAvionResponse actualizar(Integer id, ConfigClaseFilasAvionRequest request) {
        ConfigClaseFilasAvion config = obtenerConfigPorId(id);

        validarRequest(request, id);

        config.setAvionId(request.getAvionId());
        config.setClaseVueloId(request.getClaseVueloId());
        config.setFilaDesde(request.getFilaDesde());
        config.setFilaHasta(request.getFilaHasta());

        if (request.getActivo() != null) {
            config.setActivo(request.getActivo());
        }

        ConfigClaseFilasAvion actualizado = configRepository.save(config);

        return convertirAResponse(actualizado);
    }

    public ConfigClaseFilasAvionResponse desactivar(Integer id) {
        ConfigClaseFilasAvion config = obtenerConfigPorId(id);

        if (Boolean.FALSE.equals(config.getActivo())) {
            throw new RuntimeException("La configuración ya se encuentra inactiva.");
        }

        config.setActivo(false);

        ConfigClaseFilasAvion actualizado = configRepository.save(config);

        return convertirAResponse(actualizado);
    }

    private void validarRequest(ConfigClaseFilasAvionRequest request, Integer idExcluir) {
        if (request == null) {
            throw new RuntimeException("Debe enviar los datos de la configuración.");
        }

        if (request.getAvionId() == null) {
            throw new RuntimeException("Debe seleccionar un avión.");
        }

        if (request.getClaseVueloId() == null) {
            throw new RuntimeException("Debe seleccionar una clase de vuelo.");
        }

        if (request.getFilaDesde() == null) {
            throw new RuntimeException("Debe ingresar la fila inicial.");
        }

        if (request.getFilaHasta() == null) {
            throw new RuntimeException("Debe ingresar la fila final.");
        }

        if (request.getFilaDesde() <= 0) {
            throw new RuntimeException("La fila inicial debe ser mayor a 0.");
        }

        if (request.getFilaHasta() < request.getFilaDesde()) {
            throw new RuntimeException("La fila final no puede ser menor que la fila inicial.");
        }

        Avion avion = avionRepository.findById(request.getAvionId())
                .orElseThrow(() -> new RuntimeException("El avión seleccionado no existe."));

        if (avion.getFilasConfiguradas() == null || avion.getFilasConfiguradas() <= 0) {
            throw new RuntimeException("El avión no tiene filas configuradas correctamente.");
        }

        if (request.getFilaHasta() > avion.getFilasConfiguradas()) {
            throw new RuntimeException(
                    "La fila final no puede ser mayor a las filas configuradas del avión. Filas configuradas: "
                            + avion.getFilasConfiguradas()
            );
        }

        boolean seVaActivar = request.getActivo() == null || Boolean.TRUE.equals(request.getActivo());

        if (seVaActivar) {
            boolean claseYaConfigurada = configRepository.existeClaseActivaParaAvion(
                    request.getAvionId(),
                    request.getClaseVueloId(),
                    idExcluir
            );

            if (claseYaConfigurada) {
                throw new RuntimeException("El avión ya tiene una configuración activa para esa clase de vuelo.");
            }

            boolean existeCruce = configRepository.existeCruceDeFilas(
                    request.getAvionId(),
                    request.getFilaDesde(),
                    request.getFilaHasta(),
                    idExcluir
            );

            if (existeCruce) {
                throw new RuntimeException("El rango de filas se cruza con otra clase ya configurada para este avión.");
            }
        }
    }

    private ConfigClaseFilasAvion obtenerConfigPorId(Integer id) {
        if (id == null) {
            throw new RuntimeException("Debe enviar el ID de la configuración.");
        }

        return configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La configuración de filas no existe."));
    }

    private ConfigClaseFilasAvionResponse convertirAResponse(ConfigClaseFilasAvion config) {
        ConfigClaseFilasAvionResponse response = new ConfigClaseFilasAvionResponse();

        response.setId(config.getId());
        response.setAvionId(config.getAvionId());
        response.setClaseVueloId(config.getClaseVueloId());
        response.setFilaDesde(config.getFilaDesde());
        response.setFilaHasta(config.getFilaHasta());
        response.setActivo(config.getActivo());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());

        return response;
    }
}