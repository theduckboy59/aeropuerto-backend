package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.model.Aeropuerto;
import com.aeropuertolosprimos.backend.model.PuertaEmbarque;
import com.aeropuertolosprimos.backend.repository.AeropuertoRepository;
import com.aeropuertolosprimos.backend.repository.PuertaEmbarqueRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class AeropuertoServiceImpl
        implements AeropuertoService {

    private final AeropuertoRepository aeropuertoRepository;

    private final PuertaEmbarqueRepository puertaRepository;

    private final CatalogoEstadoService catalogoEstadoService;

    public AeropuertoServiceImpl(
            AeropuertoRepository aeropuertoRepository,
            PuertaEmbarqueRepository puertaRepository,
            CatalogoEstadoService catalogoEstadoService
    ) {
        this.aeropuertoRepository = aeropuertoRepository;
        this.puertaRepository = puertaRepository;
        this.catalogoEstadoService = catalogoEstadoService;
    }

    @Override
    public AeropuertoResponse crear(
            AeropuertoRequest request
    ) {

        validarRequest(request);

        String nombreLimpio = request.getNombre().trim();
        String paisLimpio = request.getPais().trim();
        String ciudadLimpia = request.getCiudad().trim();

        Aeropuerto aeropuerto =
                new Aeropuerto();

        aeropuerto.setNombre(nombreLimpio);

        aeropuerto.setCodigoIata(
                generarCodigoIata(nombreLimpio, ciudadLimpia)
        );

        aeropuerto.setCodigoIcao(
                generarCodigoIcao(nombreLimpio, ciudadLimpia, paisLimpio)
        );

        aeropuerto.setPais(paisLimpio);
        aeropuerto.setCiudad(ciudadLimpia);
        aeropuerto.setEstadoId(catalogoEstadoService.obtenerActivoId());

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
                estadoId != null ? estadoId : catalogoEstadoService.obtenerActivoId();

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

        String nombreLimpio = request.getNombre().trim();
        String paisLimpio = request.getPais().trim();
        String ciudadLimpia = request.getCiudad().trim();

        aeropuerto.setNombre(nombreLimpio);
        aeropuerto.setPais(paisLimpio);
        aeropuerto.setCiudad(ciudadLimpia);

        /*
         * No se regeneran códigos al editar.
         * Esto evita afectar vuelos, reportes o consultas que ya muestran esos códigos.
         * Solo se generan si el registro antiguo los tiene vacíos.
         */
        if (aeropuerto.getCodigoIata() == null ||
                aeropuerto.getCodigoIata().isBlank()) {

            aeropuerto.setCodigoIata(
                    generarCodigoIata(nombreLimpio, ciudadLimpia)
            );
        }

        if (aeropuerto.getCodigoIcao() == null ||
                aeropuerto.getCodigoIcao().isBlank()) {

            aeropuerto.setCodigoIcao(
                    generarCodigoIcao(nombreLimpio, ciudadLimpia, paisLimpio)
            );
        }

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

            if (item.getCodigo() == null ||
                    item.getCodigo().isBlank()) {
                continue;
            }

            PuertaEmbarque puerta =
                    new PuertaEmbarque();

            puerta.setAeropuertoId(
                    aeropuertoId
            );

            puerta.setCodigo(
                    item.getCodigo().trim().toUpperCase(Locale.ROOT)
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

        if (request == null) {
            throw new RuntimeException(
                    "Debe ingresar los campos obligatorios"
            );
        }

        if (request.getNombre() == null ||
                request.getNombre().isBlank()) {

            throw new RuntimeException(
                    "Nombre obligatorio"
            );
        }

        if (request.getPais() == null ||
                request.getPais().isBlank()) {

            throw new RuntimeException(
                    "País obligatorio"
            );
        }

        if (request.getCiudad() == null ||
                request.getCiudad().isBlank()) {

            throw new RuntimeException(
                    "Ciudad obligatoria"
            );
        }
    }

    private String generarCodigoIata(
            String nombre,
            String ciudad
    ) {

        String base = obtenerBaseLetras(ciudad);

        if (base.length() < 3) {
            base = obtenerBaseLetras(nombre);
        }

        if (base.length() < 3) {
            base = completarDerecha(base, 3);
        }

        String codigoBase = base.substring(0, 3);
        String codigo = codigoBase;

        int contador = 1;

        while (aeropuertoRepository.existsByCodigoIataIgnoreCase(codigo)) {

            if (contador <= 9) {
                codigo = codigoBase.substring(0, 2) + contador;
            } else {
                codigo = "A" + contador;
            }

            contador++;
        }

        return codigo.toUpperCase(Locale.ROOT);
    }

    private String generarCodigoIcao(
            String nombre,
            String ciudad,
            String pais
    ) {

        String basePais = obtenerBaseLetras(pais);
        String baseCiudad = obtenerBaseLetras(ciudad);
        String baseNombre = obtenerBaseLetras(nombre);

        String base = basePais + baseCiudad + baseNombre;

        if (base.length() < 4) {
            base = completarDerecha(base, 4);
        }

        String codigoBase = base.substring(0, 4);
        String codigo = codigoBase;

        int contador = 1;

        while (aeropuertoRepository.existsByCodigoIcaoIgnoreCase(codigo)) {

            if (contador <= 9) {
                codigo = codigoBase.substring(0, 3) + contador;
            } else {
                codigo = "A" + contador;
            }

            contador++;
        }

        return codigo.toUpperCase(Locale.ROOT);
    }

    private String obtenerBaseLetras(
            String texto
    ) {

        String limpio = Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z]", "")
                .toUpperCase(Locale.ROOT);

        if (limpio.isBlank()) {
            return "AER";
        }

        return limpio;
    }

    private String completarDerecha(
            String texto,
            int longitud
    ) {

        StringBuilder builder = new StringBuilder(texto);

        while (builder.length() < longitud) {
            builder.append("X");
        }

        return builder.toString();
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