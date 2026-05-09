package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.DestinoAutorizadoRequest;
import com.aeropuertolosprimos.backend.dto.DestinoAutorizadoResponse;
import com.aeropuertolosprimos.backend.model.Aerolinea;
import com.aeropuertolosprimos.backend.model.Aeropuerto;
import com.aeropuertolosprimos.backend.model.DestinoAutorizado;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import com.aeropuertolosprimos.backend.repository.AeropuertoRepository;
import com.aeropuertolosprimos.backend.repository.DestinoAutorizadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DestinoAutorizadoServiceImpl
        implements DestinoAutorizadoService {

    private final DestinoAutorizadoRepository repository;

    private final AerolineaRepository aerolineaRepository;

    private final AeropuertoRepository aeropuertoRepository;

    public DestinoAutorizadoServiceImpl(
            DestinoAutorizadoRepository repository,
            AerolineaRepository aerolineaRepository,
            AeropuertoRepository aeropuertoRepository
    ) {

        this.repository = repository;

        this.aerolineaRepository =
                aerolineaRepository;

        this.aeropuertoRepository =
                aeropuertoRepository;
    }

    @Override
    public DestinoAutorizadoResponse crear(
            DestinoAutorizadoRequest request
    ) {

        DestinoAutorizado destino =
                new DestinoAutorizado();

        destino.setAerolineaId(
                request.getAerolineaId()
        );

        destino.setAeropuertoId(
                request.getAeropuertoId()
        );

        destino.setEstadoId(1);

        destino =
                repository.save(destino);

        return convertirResponse(destino);
    }

    @Override
    public List<DestinoAutorizadoResponse> listar(
            Integer aerolineaId,
            Integer aeropuertoId,
            String pais,
            Integer estadoId
    ) {

        Integer estado =
                estadoId != null ? estadoId : 1;

        List<DestinoAutorizado> destinos;

        if (aerolineaId != null) {

            destinos =
                    repository
                            .findByAerolineaIdAndEstadoId(
                                    aerolineaId,
                                    estado
                            );

        } else if (aeropuertoId != null) {

            destinos =
                    repository
                            .findByAeropuertoIdAndEstadoId(
                                    aeropuertoId,
                                    estado
                            );

        } else {

            destinos =
                    repository.findByEstadoId(
                            estado
                    );
        }

        List<DestinoAutorizadoResponse> response =
                destinos.stream()
                        .map(this::convertirResponse)
                        .toList();

        if (pais != null &&
                !pais.isBlank()) {

            response =
                    response.stream()
                            .filter(r ->
                                    r.getPais()
                                            .equalsIgnoreCase(
                                                    pais
                                            )
                            )
                            .toList();
        }

        return response;
    }

    @Override
    public DestinoAutorizadoResponse obtenerPorId(
            Integer id
    ) {

        DestinoAutorizado destino =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Destino autorizado no encontrado"
                                ));

        return convertirResponse(destino);
    }

    @Override
    public DestinoAutorizadoResponse actualizar(
            Integer id,
            DestinoAutorizadoRequest request
    ) {

        DestinoAutorizado destino =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Destino autorizado no encontrado"
                                ));

        destino.setAerolineaId(
                request.getAerolineaId()
        );

        destino.setAeropuertoId(
                request.getAeropuertoId()
        );

        destino =
                repository.save(destino);

        return convertirResponse(destino);
    }

    @Override
    public void eliminar(
            Integer id
    ) {

        DestinoAutorizado destino =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Destino autorizado no encontrado"
                                ));

        destino.setEstadoId(2);

        repository.save(destino);
    }

    private DestinoAutorizadoResponse convertirResponse(
            DestinoAutorizado destino
    ) {

        Aerolinea aerolinea =
                aerolineaRepository.findById(
                        destino.getAerolineaId()
                ).orElseThrow();

        Aeropuerto aeropuerto =
                aeropuertoRepository.findById(
                        destino.getAeropuertoId()
                ).orElseThrow();

        DestinoAutorizadoResponse response =
                new DestinoAutorizadoResponse();

        response.setId(
                destino.getId()
        );

        response.setAerolineaId(
                aerolinea.getId()
        );

        response.setAerolineaNombre(
                aerolinea.getNombre()
        );

        response.setAeropuertoId(
                aeropuerto.getId()
        );

        response.setAeropuertoNombre(
                aeropuerto.getNombre()
        );

        response.setPais(
                aeropuerto.getPais()
        );

        response.setEstadoId(
                destino.getEstadoId()
        );

        response.setFechaAutorizacion(
                destino.getFechaAutorizacion()
        );

        return response;
    }
}