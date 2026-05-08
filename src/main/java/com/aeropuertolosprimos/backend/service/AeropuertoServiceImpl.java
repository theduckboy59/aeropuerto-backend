package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.model.Aeropuerto;
import com.aeropuertolosprimos.backend.model.PuertaEmbarque;
import com.aeropuertolosprimos.backend.repository.AeropuertoRepository;
import com.aeropuertolosprimos.backend.repository.PuertaEmbarqueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AeropuertoServiceImpl
        implements AeropuertoService {

    private final AeropuertoRepository aeropuertoRepository;

    private final PuertaEmbarqueRepository puertaRepository;

    public AeropuertoServiceImpl(
            AeropuertoRepository aeropuertoRepository,
            PuertaEmbarqueRepository puertaRepository
    ) {

        this.aeropuertoRepository = aeropuertoRepository;
        this.puertaRepository = puertaRepository;
    }

    @Override
    public AeropuertoResponse crear(
            AeropuertoRequest request
    ) {

        validarRequest(request);

        Aeropuerto aeropuerto =
                new Aeropuerto();

        aeropuerto.setNombre(
                request.getNombre()
        );

        aeropuerto.setCodigoIata(
                request.getCodigoIata()
        );

        aeropuerto.setCodigoIcao(
                request.getCodigoIcao()
        );

        aeropuerto.setPais(
                request.getPais()
        );

        aeropuerto.setCiudad(
                request.getCiudad()
        );

        aeropuerto.setEstadoId(1);

        aeropuerto =
                aeropuertoRepository.save(
                        aeropuerto
                );

        guardarPuertas(
                aeropuerto.getId(),
                request.getPuertas()
        );

        return convertirResponse(
                aeropuerto
        );
    }

    @Override
    public List<AeropuertoResponse> listar(
            String nombre,
            String pais,
            Integer estadoId
    ) {

        Integer estado =
                estadoId != null ? estadoId : 1;

        List<Aeropuerto> aeropuertos;

        if (nombre != null &&
                !nombre.isBlank()) {

            aeropuertos =
                    aeropuertoRepository
                            .findByEstadoIdAndNombreContainingIgnoreCase(
                                    estado,
                                    nombre
                            );

        } else if (pais != null &&
                !pais.isBlank()) {

            aeropuertos =
                    aeropuertoRepository
                            .findByEstadoIdAndPaisContainingIgnoreCase(
                                    estado,
                                    pais
                            );

        } else {

            aeropuertos =
                    aeropuertoRepository
                            .findByEstadoId(
                                    estado
                            );
        }

        return aeropuertos.stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public AeropuertoResponse obtenerPorId(
            Integer id
    ) {

        Aeropuerto aeropuerto =
                aeropuertoRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aeropuerto no encontrado"
                                ));

        return convertirResponse(
                aeropuerto
        );
    }

    @Override
    public AeropuertoResponse actualizar(
            Integer id,
            AeropuertoRequest request
    ) {

        validarRequest(request);

        Aeropuerto aeropuerto =
                aeropuertoRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aeropuerto no encontrado"
                                ));

        aeropuerto.setNombre(
                request.getNombre()
        );

        aeropuerto.setCodigoIata(
                request.getCodigoIata()
        );

        aeropuerto.setCodigoIcao(
                request.getCodigoIcao()
        );

        aeropuerto.setPais(
                request.getPais()
        );

        aeropuerto.setCiudad(
                request.getCiudad()
        );

        aeropuerto =
                aeropuertoRepository.save(
                        aeropuerto
                );

        List<PuertaEmbarque> puertasActuales =
                puertaRepository
                        .findByAeropuertoIdAndEstadoId(
                                id,
                                1
                        );

        puertasActuales.forEach(p -> {
            p.setEstadoId(2);
            puertaRepository.save(p);
        });

        guardarPuertas(
                aeropuerto.getId(),
                request.getPuertas()
        );

        return convertirResponse(
                aeropuerto
        );
    }

    @Override
    public void eliminar(
            Integer id
    ) {

        Aeropuerto aeropuerto =
                aeropuertoRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aeropuerto no encontrado"
                                ));

        aeropuerto.setEstadoId(2);

        aeropuertoRepository.save(
                aeropuerto
        );

        List<PuertaEmbarque> puertas =
                puertaRepository
                        .findByAeropuertoIdAndEstadoId(
                                id,
                                1
                        );

        puertas.forEach(p -> {
            p.setEstadoId(2);
            puertaRepository.save(p);
        });
    }

    private void guardarPuertas(
            Integer aeropuertoId,
            List<PuertaEmbarqueRequest> puertas
    ) {

        if (puertas == null) {
            return;
        }

        for (PuertaEmbarqueRequest item : puertas) {

            PuertaEmbarque puerta =
                    new PuertaEmbarque();

            puerta.setAeropuertoId(
                    aeropuertoId
            );

            puerta.setCodigo(
                    item.getCodigo()
            );

            puerta.setEstadoId(1);

            puertaRepository.save(
                    puerta
            );
        }
    }

    private void validarRequest(
            AeropuertoRequest request
    ) {

        if (request.getNombre() == null ||
                request.getNombre().isBlank()) {

            throw new RuntimeException(
                    "Nombre obligatorio"
            );
        }

        if (request.getPais() == null ||
                request.getPais().isBlank()) {

            throw new RuntimeException(
                    "Pais obligatorio"
            );
        }

        if (request.getCiudad() == null ||
                request.getCiudad().isBlank()) {

            throw new RuntimeException(
                    "Ciudad obligatoria"
            );
        }
    }

    private AeropuertoResponse convertirResponse(
            Aeropuerto aeropuerto
    ) {

        AeropuertoResponse response =
                new AeropuertoResponse();

        response.setId(
                aeropuerto.getId()
        );

        response.setNombre(
                aeropuerto.getNombre()
        );

        response.setCodigoIata(
                aeropuerto.getCodigoIata()
        );

        response.setCodigoIcao(
                aeropuerto.getCodigoIcao()
        );

        response.setPais(
                aeropuerto.getPais()
        );

        response.setCiudad(
                aeropuerto.getCiudad()
        );

        response.setEstadoId(
                aeropuerto.getEstadoId()
        );

        List<PuertaEmbarqueResponse> puertas =
                puertaRepository
                        .findByAeropuertoIdAndEstadoId(
                                aeropuerto.getId(),
                                1
                        )
                        .stream()
                        .map(p -> {

                            PuertaEmbarqueResponse r =
                                    new PuertaEmbarqueResponse();

                            r.setId(p.getId());

                            r.setCodigo(
                                    p.getCodigo()
                            );

                            return r;
                        })
                        .toList();

        response.setPuertas(
                puertas
        );

        return response;
    }
}