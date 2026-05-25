package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AsientoUbiResponse;
import com.aeropuertolosprimos.backend.model.AsientoUbi;
import com.aeropuertolosprimos.backend.model.ClaseVuelo;
import com.aeropuertolosprimos.backend.model.TipoAsiento;
import com.aeropuertolosprimos.backend.repository.AsientoUbiRepository;
import com.aeropuertolosprimos.backend.repository.ClaseVueloRepository;
import com.aeropuertolosprimos.backend.repository.TipoAsientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsientoUbiService {

    private static final String INHABILITADO = "INHABILITADO";

    private final AsientoUbiRepository asientoUbiRepository;
    private final ClaseVueloRepository claseVueloRepository;
    private final TipoAsientoRepository tipoAsientoRepository;

    private final CatalogoEstadoService catalogoEstadoService;

    public Page<AsientoUbiResponse> buscarConFiltros(
            Integer avionId,
            Integer claseVueloId,
            Integer tipoAsientoId,
            Integer nivel,
            Integer fila,
            String columna,
            String numeroAsiento,
            Boolean vendible,
            Pageable pageable
    ) {
        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return asientoUbiRepository
                .buscarConFiltros(
                        avionId,
                        claseVueloId,
                        tipoAsientoId,
                        nivel,
                        fila,
                        limpiarTexto(columna),
                        limpiarTexto(numeroAsiento),
                        vendible,
                        estadoActivoId,
                        pageable
                )
                .map(this::convertirAResponse);
    }

    public AsientoUbiResponse buscarPorId(
            Integer id
    ) {

        if (id == null) {
            throw new RuntimeException("Debe enviar el ID del asiento.");
        }

        AsientoUbi asiento = asientoUbiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El asiento no existe."));

        return convertirAResponse(asiento);
    }

    private String limpiarTexto(
            String valor
    ) {

        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        return valor.trim().toUpperCase();
    }

    private AsientoUbiResponse convertirAResponse(
            AsientoUbi asiento
    ) {

        AsientoUbiResponse response = new AsientoUbiResponse();

        response.setId(asiento.getId());
        response.setAvionId(asiento.getAvionId());

        response.setClaseVueloId(
                asiento.getClaseVueloId()
        );

        if (asiento.getClaseVueloId() == null) {

            response.setClaseVueloNombre(INHABILITADO);
            response.setVendible(false);

        } else {

            String nombreClase = claseVueloRepository.findById(asiento.getClaseVueloId())
                    .map(ClaseVuelo::getNombre)
                    .orElse("CLASE NO ENCONTRADA");

            response.setClaseVueloNombre(nombreClase);
            response.setVendible(true);
        }

        response.setTipoAsientoId(
                asiento.getTipoAsientoId()
        );

        if (asiento.getTipoAsientoId() != null) {

            String nombreTipo = tipoAsientoRepository.findById(asiento.getTipoAsientoId())
                    .map(TipoAsiento::getNombre)
                    .orElse("TIPO NO ENCONTRADO");

            response.setTipoAsientoNombre(nombreTipo);
        }

        response.setNivel(asiento.getNivel());
        response.setFila(asiento.getFila());
        response.setColumna(asiento.getColumna());
        response.setNumeroAsiento(asiento.getNumeroAsiento());
        response.setCodigoAsientoSistema(asiento.getCodigoAsientoSistema());
        response.setBloque(asiento.getBloque());
        response.setLado(asiento.getLado());
        response.setCreatedAt(asiento.getCreatedAt());
        response.setUpdatedAt(asiento.getUpdatedAt());

        return response;
    }
}