package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AerolineaRequest;
import com.aeropuertolosprimos.backend.dto.AerolineaResponse;
import com.aeropuertolosprimos.backend.model.Aerolinea;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AerolineaServiceImpl
        implements AerolineaService {

    private final AerolineaRepository repository;

    public AerolineaServiceImpl(
            AerolineaRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public AerolineaResponse crear(
            AerolineaRequest request
    ) {

        validarRequest(request);

        if (repository.existsByCodigoIataIgnoreCase(
                request.getCodigoIata()
        )) {

            throw new RuntimeException(
                    "Código IATA ya registrado"
            );
        }

        if (repository.existsByCodigoIcaoIgnoreCase(
                request.getCodigoIcao()
        )) {

            throw new RuntimeException(
                    "Código ICAO ya registrado"
            );
        }

        Aerolinea aerolinea =
                new Aerolinea();

        aerolinea.setNombre(
                request.getNombre()
        );

        aerolinea.setCodigoIata(
                request.getCodigoIata()
        );

        aerolinea.setCodigoIcao(
                request.getCodigoIcao()
        );

        aerolinea.setPais(
                request.getPais()
        );

        aerolinea.setEstadoId(1);

        aerolinea = repository.save(
                aerolinea
        );

        return convertirResponse(
                aerolinea
        );
    }

    @Override
    public List<AerolineaResponse> listar(
            String nombre
    ) {

        List<Aerolinea> aerolineas;

        if (nombre != null &&
                !nombre.isBlank()) {

            aerolineas =
                    repository
                            .findByEstadoIdAndNombreContainingIgnoreCase(
                                    1,
                                    nombre
                            );

        } else {

            aerolineas =
                    repository.findByEstadoId(1);
        }

        return aerolineas.stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public AerolineaResponse obtenerPorId(
            Integer id
    ) {

        Aerolinea aerolinea =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aerolínea no encontrada"
                                ));

        return convertirResponse(
                aerolinea
        );
    }

    @Override
    public AerolineaResponse actualizar(
            Integer id,
            AerolineaRequest request
    ) {

        validarRequest(request);

        Aerolinea aerolinea =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aerolínea no encontrada"
                                ));

        aerolinea.setNombre(
                request.getNombre()
        );

        aerolinea.setCodigoIata(
                request.getCodigoIata()
        );

        aerolinea.setCodigoIcao(
                request.getCodigoIcao()
        );

        aerolinea.setPais(
                request.getPais()
        );

        aerolinea = repository.save(
                aerolinea
        );

        return convertirResponse(
                aerolinea
        );
    }

    @Override
    public void eliminar(
            Integer id
    ) {

        Aerolinea aerolinea =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aerolínea no encontrada"
                                ));

        aerolinea.setEstadoId(2);

        repository.save(
                aerolinea
        );
    }

    private void validarRequest(
            AerolineaRequest request
    ) {

        if (request.getNombre() == null ||
                request.getNombre().isBlank()) {

            throw new RuntimeException(
                    "Nombre obligatorio"
            );
        }

        if (request.getCodigoIata() == null ||
                request.getCodigoIata().isBlank()) {

            throw new RuntimeException(
                    "Código IATA obligatorio"
            );
        }

        if (request.getCodigoIcao() == null ||
                request.getCodigoIcao().isBlank()) {

            throw new RuntimeException(
                    "Código ICAO obligatorio"
            );
        }

        if (request.getPais() == null ||
                request.getPais().isBlank()) {

            throw new RuntimeException(
                    "País obligatorio"
            );
        }
    }

    private AerolineaResponse convertirResponse(
            Aerolinea aerolinea
    ) {

        AerolineaResponse response =
                new AerolineaResponse();

        response.setId(
                aerolinea.getId()
        );

        response.setNombre(
                aerolinea.getNombre()
        );

        response.setCodigoIata(
                aerolinea.getCodigoIata()
        );

        response.setCodigoIcao(
                aerolinea.getCodigoIcao()
        );

        response.setPais(
                aerolinea.getPais()
        );

        response.setEstadoId(
                aerolinea.getEstadoId()
        );

        return response;
    }
}