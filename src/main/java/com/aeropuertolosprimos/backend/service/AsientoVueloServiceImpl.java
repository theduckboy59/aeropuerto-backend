package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AsientoVueloResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import com.aeropuertolosprimos.backend.specification.AsientoVueloSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AsientoVueloServiceImpl implements AsientoVueloService {

    private static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    private static final String ESTADO_RESERVADO = "RESERVADO";
    private static final String ESTADO_OCUPADO = "OCUPADO";
    private static final String ESTADO_BLOQUEADO = "BLOQUEADO";

    private final AsientoVueloRepository asientoVueloRepository;
    private final EstadoAsientoRepository estadoAsientoRepository;
    private final AsientoUbiRepository asientoUbiRepository;

    private final SegmentoOperadoRepository segmentoOperadoRepository;
    private final VueloOperadoRepository vueloOperadoRepository;
    private final VueloProgramadoRepository vueloProgramadoRepository;
    private final VueloRepository vueloRepository;

    private final AvionRepository avionRepository;
    private final ClaseVueloRepository claseVueloRepository;
    private final TipoAsientoRepository tipoAsientoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AsientoVueloResponse> findAll(
            Integer vueloOperadoId,
            Integer segmentoOperadoId,
            Integer estadoAsientoId,
            Integer claseVueloId,
            Integer tipoAsientoId,
            Integer nivel,
            Integer fila,
            String columna,
            String numeroAsiento,
            Pageable pageable
    ) {

        return asientoVueloRepository
                .findAll(
                        AsientoVueloSpecification.filters(
                                vueloOperadoId,
                                segmentoOperadoId,
                                estadoAsientoId,
                                claseVueloId,
                                tipoAsientoId,
                                nivel,
                                fila,
                                limpiarTexto(columna),
                                limpiarTexto(numeroAsiento)
                        ),
                        pageable
                )
                .map(this::mapResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AsientoVueloResponse findById(Integer id) {

        AsientoVuelo asientoVuelo = asientoVueloRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException("Asiento de vuelo no encontrado")
                );

        return mapResponse(asientoVuelo);
    }

    @Override
    @Transactional
    public void generarAsientosParaVueloOperado(Integer vueloOperadoId) {

        if (vueloOperadoId == null) {
            throw new BusinessException("ID de vuelo operado inválido");
        }

        VueloOperado vueloOperado = vueloOperadoRepository
                .findById(vueloOperadoId)
                .orElseThrow(() ->
                        new BusinessException("Vuelo operado no encontrado")
                );

        List<SegmentoOperado> segmentos = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(
                        vueloOperado.getId()
                );

        if (segmentos.isEmpty()) {
            throw new BusinessException("El vuelo operado no tiene segmentos operados");
        }

        for (SegmentoOperado segmento : segmentos) {
            generarAsientosParaSegmentoOperado(
                    segmento.getId()
            );
        }
    }

    @Override
    @Transactional
    public void generarAsientosParaSegmentoOperado(Integer segmentoOperadoId) {

        if (segmentoOperadoId == null) {
            throw new BusinessException("ID de segmento operado inválido");
        }

        SegmentoOperado segmentoOperado = segmentoOperadoRepository
                .findById(segmentoOperadoId)
                .orElseThrow(() ->
                        new BusinessException("Segmento operado no encontrado")
                );

        if (segmentoOperado.getAvionId() == null) {
            throw new BusinessException("El segmento operado no tiene avión asignado");
        }

        Integer estadoDisponibleId = obtenerEstadoAsientoIdPorNombre(
                ESTADO_DISPONIBLE
        );

        List<AsientoUbi> asientos = asientoUbiRepository
                .findByAvionId(
                        segmentoOperado.getAvionId()
                );

        for (AsientoUbi asiento : asientos) {

            if (asiento.getClaseVueloId() == null) {
                continue;
            }

            if (asiento.getCodigoAsientoSistema() == null ||
                    asiento.getCodigoAsientoSistema().isBlank()) {
                continue;
            }

            boolean existe = asientoVueloRepository
                    .existsBySegmentoOperadoIdAndCodigoAsientoSistema(
                            segmentoOperado.getId(),
                            asiento.getCodigoAsientoSistema()
                    );

            if (existe) {
                continue;
            }

            AsientoVuelo asientoVuelo = new AsientoVuelo();

            asientoVuelo.setSegmentoOperadoId(
                    segmentoOperado.getId()
            );

            asientoVuelo.setCodigoAsientoSistema(
                    asiento.getCodigoAsientoSistema()
            );

            asientoVuelo.setEstadoAsientoId(
                    estadoDisponibleId
            );

            asientoVueloRepository.save(
                    asientoVuelo
            );
        }
    }

    @Override
    @Transactional
    public AsientoVueloResponse cambiarEstado(
            Integer id,
            Integer estadoAsientoId
    ) {

        if (estadoAsientoId == null) {
            throw new BusinessException("Debe ingresar el estado del asiento");
        }

        EstadoAsiento estadoAsiento = estadoAsientoRepository
                .findById(estadoAsientoId)
                .orElseThrow(() ->
                        new BusinessException("Estado de asiento no encontrado")
                );

        validarEstadoAsientoPermitido(
                estadoAsiento.getNombre()
        );

        AsientoVuelo asientoVuelo = asientoVueloRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException("Asiento de vuelo no encontrado")
                );

        asientoVuelo.setEstadoAsientoId(
                estadoAsiento.getId()
        );

        AsientoVuelo actualizado = asientoVueloRepository.save(
                asientoVuelo
        );

        return mapResponse(
                actualizado
        );
    }

    @Override
    @Transactional
    public AsientoVueloResponse cambiarEstadoPorNombre(
            Integer id,
            String estadoAsientoNombre
    ) {

        String estadoNormalizado = limpiarTexto(
                estadoAsientoNombre
        );

        if (estadoNormalizado == null) {
            throw new BusinessException("Debe ingresar el estado del asiento");
        }

        validarEstadoAsientoPermitido(
                estadoNormalizado
        );

        Integer estadoAsientoId = obtenerEstadoAsientoIdPorNombre(
                estadoNormalizado
        );

        AsientoVuelo asientoVuelo = asientoVueloRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException("Asiento de vuelo no encontrado")
                );

        asientoVuelo.setEstadoAsientoId(
                estadoAsientoId
        );

        AsientoVuelo actualizado = asientoVueloRepository.save(
                asientoVuelo
        );

        return mapResponse(
                actualizado
        );
    }

    private AsientoVueloResponse mapResponse(
            AsientoVuelo asientoVuelo
    ) {

        AsientoVueloResponse response = new AsientoVueloResponse();

        response.setId(
                asientoVuelo.getId()
        );

        response.setSegmentoOperadoId(
                asientoVuelo.getSegmentoOperadoId()
        );

        response.setCodigoAsientoSistema(
                asientoVuelo.getCodigoAsientoSistema()
        );

        response.setEstadoAsientoId(
                asientoVuelo.getEstadoAsientoId()
        );

        response.setCreatedAt(
                asientoVuelo.getCreatedAt()
        );

        response.setUpdatedAt(
                asientoVuelo.getUpdatedAt()
        );

        mapSegmentoOperado(
                response,
                asientoVuelo
        );

        mapAsientoUbiPorCodigo(
                response,
                asientoVuelo
        );

        mapEstadoAsiento(
                response,
                asientoVuelo
        );

        return response;
    }

    private void mapSegmentoOperado(
            AsientoVueloResponse response,
            AsientoVuelo asientoVuelo
    ) {

        if (asientoVuelo.getSegmentoOperadoId() == null) {
            return;
        }

        segmentoOperadoRepository
                .findById(asientoVuelo.getSegmentoOperadoId())
                .ifPresent(segmentoOperado -> {

                    response.setVueloOperadoId(
                            segmentoOperado.getVueloOperadoId()
                    );

                    response.setOrdenSegmento(
                            segmentoOperado.getOrdenSegmento()
                    );

                    response.setAvionId(
                            segmentoOperado.getAvionId()
                    );

                    avionRepository
                            .findById(segmentoOperado.getAvionId())
                            .ifPresent(avion ->
                                    response.setCodigoAvion(
                                            avion.getCodigoAvion()
                                    )
                            );

                    vueloOperadoRepository
                            .findById(segmentoOperado.getVueloOperadoId())
                            .ifPresent(vueloOperado -> {

                                response.setVueloProgramadoId(
                                        vueloOperado.getVueloProgramadoId()
                                );

                                vueloProgramadoRepository
                                        .findById(vueloOperado.getVueloProgramadoId())
                                        .ifPresent(programado -> {

                                            vueloRepository
                                                    .findById(programado.getVueloId())
                                                    .ifPresent(vuelo ->
                                                            response.setCodigoVuelo(
                                                                    vuelo.getCodigoVuelo()
                                                            )
                                                    );
                                        });
                            });
                });
    }

    private void mapAsientoUbiPorCodigo(
            AsientoVueloResponse response,
            AsientoVuelo asientoVuelo
    ) {

        if (asientoVuelo.getCodigoAsientoSistema() == null ||
                asientoVuelo.getCodigoAsientoSistema().isBlank()) {
            return;
        }

        asientoUbiRepository
                .findFirstByCodigoAsientoSistemaOrderByIdAsc(
                        asientoVuelo.getCodigoAsientoSistema()
                )
                .ifPresent(asiento -> {

                    response.setClaseVueloId(
                            asiento.getClaseVueloId()
                    );

                    response.setTipoAsientoId(
                            asiento.getTipoAsientoId()
                    );

                    response.setNivel(
                            asiento.getNivel()
                    );

                    response.setFila(
                            asiento.getFila()
                    );

                    response.setColumna(
                            asiento.getColumna()
                    );

                    response.setNumeroAsiento(
                            asiento.getNumeroAsiento()
                    );

                    response.setBloque(
                            asiento.getBloque()
                    );

                    response.setLado(
                            asiento.getLado()
                    );

                    if (asiento.getClaseVueloId() != null) {
                        claseVueloRepository
                                .findById(asiento.getClaseVueloId())
                                .ifPresent(clase ->
                                        response.setClaseVueloNombre(
                                                clase.getNombre()
                                        )
                                );
                    }

                    if (asiento.getTipoAsientoId() != null) {
                        tipoAsientoRepository
                                .findById(asiento.getTipoAsientoId())
                                .ifPresent(tipo ->
                                        response.setTipoAsientoNombre(
                                                tipo.getNombre()
                                        )
                                );
                    }
                });
    }

    private void mapEstadoAsiento(
            AsientoVueloResponse response,
            AsientoVuelo asientoVuelo
    ) {

        if (asientoVuelo.getEstadoAsientoId() == null) {
            return;
        }

        estadoAsientoRepository
                .findById(asientoVuelo.getEstadoAsientoId())
                .ifPresent(estado ->
                        response.setEstadoAsientoNombre(
                                estado.getNombre()
                        )
                );
    }

    private Integer obtenerEstadoAsientoIdPorNombre(
            String nombre
    ) {

        return estadoAsientoRepository
                .findByNombreIgnoreCase(nombre)
                .map(EstadoAsiento::getId)
                .orElseThrow(() ->
                        new BusinessException("Estado de asiento no encontrado: " + nombre)
                );
    }

    private void validarEstadoAsientoPermitido(
            String estado
    ) {

        String normalizado = limpiarTexto(
                estado
        );

        if (
                ESTADO_DISPONIBLE.equals(normalizado) ||
                        ESTADO_RESERVADO.equals(normalizado) ||
                        ESTADO_OCUPADO.equals(normalizado) ||
                        ESTADO_BLOQUEADO.equals(normalizado)
        ) {
            return;
        }

        throw new BusinessException("Estado de asiento no permitido");
    }

    private String limpiarTexto(
            String texto
    ) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        return Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}