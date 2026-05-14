package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AerolineaRequest;
import com.aeropuertolosprimos.backend.dto.AerolineaResponse;
import com.aeropuertolosprimos.backend.model.Aerolinea;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

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

        String nombreLimpio = request.getNombre().trim();
        String paisLimpio = request.getPais().trim();

        String codigoIata = generarCodigoIata(nombreLimpio);
        String codigoIcao = generarCodigoIcao(nombreLimpio, paisLimpio);

        Aerolinea aerolinea =
                new Aerolinea();

        aerolinea.setNombre(nombreLimpio);
        aerolinea.setCodigoIata(codigoIata);
        aerolinea.setCodigoIcao(codigoIcao);
        aerolinea.setPais(paisLimpio);
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

        String nombreLimpio = request.getNombre().trim();
        String paisLimpio = request.getPais().trim();

        aerolinea.setNombre(nombreLimpio);
        aerolinea.setPais(paisLimpio);

        /*
         * Importante:
         * No se regeneran los códigos al editar para no afectar reportes,
         * vuelos, aviones u otras referencias visuales que ya usen esos códigos.
         * Solo se generan si por algún motivo antiguo vienen vacíos.
         */
        if (aerolinea.getCodigoIata() == null ||
                aerolinea.getCodigoIata().isBlank()) {

            aerolinea.setCodigoIata(
                    generarCodigoIata(nombreLimpio)
            );
        }

        if (aerolinea.getCodigoIcao() == null ||
                aerolinea.getCodigoIcao().isBlank()) {

            aerolinea.setCodigoIcao(
                    generarCodigoIcao(nombreLimpio, paisLimpio)
            );
        }

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
    }

    private String generarCodigoIata(
            String nombre
    ) {

        String base = obtenerBaseLetras(nombre);

        if (base.length() < 2) {
            base = completarDerecha(base, 2);
        }

        String codigoBase = base.substring(0, 2);
        String codigo = codigoBase;

        int contador = 1;

        while (repository.existsByCodigoIataIgnoreCase(codigo)) {

            if (contador <= 9) {
                codigo = codigoBase.substring(0, 1) + contador;
            } else {
                codigo = "A" + contador;
            }

            contador++;
        }

        return codigo.toUpperCase(Locale.ROOT);
    }

    private String generarCodigoIcao(
            String nombre,
            String pais
    ) {

        String baseNombre = obtenerBaseLetras(nombre);
        String basePais = obtenerBaseLetras(pais);

        String base = baseNombre + basePais;

        if (base.length() < 3) {
            base = completarDerecha(base, 3);
        }

        String codigoBase = base.substring(0, 3);
        String codigo = codigoBase;

        int contador = 1;

        while (repository.existsByCodigoIcaoIgnoreCase(codigo)) {

            if (contador <= 9) {
                codigo = codigoBase.substring(0, 2) + contador;
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