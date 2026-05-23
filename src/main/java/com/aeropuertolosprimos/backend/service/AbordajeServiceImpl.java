package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.AbordajeRequest;
import com.aeropuertolosprimos.backend.dto.AbordajeResponse;
import com.aeropuertolosprimos.backend.dto.FinalizarAbordajeResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AbordajeServiceImpl implements AbordajeService {

    private static final BigDecimal RECARGO_MALETA_EXTRA = new BigDecimal("50.00");

    private static final String ESTADO_BOLETO_PENDIENTE = "PENDIENTE_ABORDAR";
    private static final String ESTADO_BOLETO_ABORDADO = "ABORDADO";
    private static final String ESTADO_BOLETO_CANCELADO = "CANCELADO";

    private static final String ESTADO_ASIENTO_OCUPADO = "OCUPADO";
    private static final String ESTADO_ASIENTO_BLOQUEADO = "BLOQUEADO";

    private static final String ESTADO_EQUIPAJE_REGISTRADO = "REGISTRADO";
    private static final String ESTADO_EQUIPAJE_ABORDADO = "ABORDADO";
    private static final String ESTADO_EQUIPAJE_CANCELADO = "CANCELADO";

    private static final String TIPO_EQUIPAJE_MALETA = "MALETA";

    private static final String ESTADO_VUELO_ABORDADO = "ABORDANDO";

    private final PasajeroRepository pasajeroRepository;
    private final BoletoRepository boletoRepository;
    private final BoletoSegmentoRepository boletoSegmentoRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final EquipajeRepository equipajeRepository;

    private final VueloOperadoRepository vueloOperadoRepository;
    private final SegmentoOperadoRepository segmentoOperadoRepository;

    private final AsientoVueloRepository asientoVueloRepository;
    private final AsientoUbiRepository asientoUbiRepository;

    private final EstadoBoletoRepository estadoBoletoRepository;
    private final EstadoAsientoRepository estadoAsientoRepository;
    private final EstadoEquipajeRepository estadoEquipajeRepository;
    private final TipoEquipajeRepository tipoEquipajeRepository;
    private final EstadoVueloRepository estadoVueloRepository;

    @Override
    @Transactional(readOnly = true)
    public AbordajeResponse buscar(
            Integer vueloOperadoId,
            String pasaporte
    ) {

        Boleto boleto = obtenerBoletoPorPasaporteYVuelo(
                vueloOperadoId,
                pasaporte
        );

        return mapResponse(
                boleto,
                null,
                "Boleto encontrado"
        );
    }

    @Override
    @Transactional
    public AbordajeResponse registrarAbordaje(
            AbordajeRequest request
    ) {

        validarRequest(request);

        Boleto boleto = obtenerBoletoPorPasaporteYVuelo(
                request.getVueloOperadoId(),
                request.getPasaporte()
        );

        EstadoBoleto estadoPendiente = obtenerEstadoBoleto(
                ESTADO_BOLETO_PENDIENTE
        );

        EstadoBoleto estadoAbordado = obtenerEstadoBoleto(
                ESTADO_BOLETO_ABORDADO
        );

        EstadoAsiento estadoOcupado = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_OCUPADO)
                .orElseThrow(() -> new BusinessException("Estado de asiento OCUPADO no encontrado"));

        EstadoEquipaje estadoEquipajeRegistrado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_REGISTRADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje REGISTRADO no encontrado"));

        EstadoEquipaje estadoEquipajeAbordado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_ABORDADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje ABORDADO no encontrado"));

        EstadoEquipaje estadoEquipajeCancelado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje CANCELADO no encontrado"));

        TipoEquipaje tipoMaleta = tipoEquipajeRepository
                .findByNombreIgnoreCase(TIPO_EQUIPAJE_MALETA)
                .orElseThrow(() -> new BusinessException("Tipo de equipaje MALETA no encontrado"));

        if (!Objects.equals(boleto.getEstadoBoletoId(), estadoPendiente.getId())) {
            throw new BusinessException("El boleto no está pendiente de abordar");
        }

        int cantidadPresentada = request.getCantidadMaletasPresentadas();

        List<Equipaje> equipajes = equipajeRepository
                .findByBoletoIdOrderByNumeroMaletaAsc(boleto.getId());

        int cantidadRegistrada = equipajes.size();

        BigDecimal recargo = BigDecimal.ZERO;

        if (cantidadPresentada > cantidadRegistrada) {

            int extras = cantidadPresentada - cantidadRegistrada;

            recargo = RECARGO_MALETA_EXTRA.multiply(
                    BigDecimal.valueOf(extras)
            );

            for (int i = cantidadRegistrada + 1; i <= cantidadPresentada; i++) {

                Equipaje equipaje = new Equipaje();

                equipaje.setBoletoId(boleto.getId());
                equipaje.setPasajeroId(boleto.getPasajeroId());
                equipaje.setSegmentoOperadoId(obtenerPrimerSegmentoOperadoId(boleto));
                equipaje.setTipoEquipajeId(tipoMaleta.getId());
                equipaje.setEstadoEquipajeId(estadoEquipajeRegistrado.getId());
                equipaje.setNumeroMaleta(i);
                equipaje.setMontoRecargo(RECARGO_MALETA_EXTRA);

                equipaje = equipajeRepository.save(equipaje);

                equipajes.add(equipaje);
            }
        }

        for (int i = 0; i < equipajes.size(); i++) {

            Equipaje equipaje = equipajes.get(i);

            if ((i + 1) <= cantidadPresentada) {
                equipaje.setEstadoEquipajeId(estadoEquipajeAbordado.getId());
            } else {
                equipaje.setEstadoEquipajeId(estadoEquipajeCancelado.getId());
            }

            equipajeRepository.save(equipaje);
        }

        boleto.setEstadoBoletoId(estadoAbordado.getId());
        boleto.setRecargoEquipaje(recargo);

        BigDecimal precioBase = boleto.getPrecioBase() != null
                ? boleto.getPrecioBase()
                : BigDecimal.ZERO;

        boleto.setTotal(
                precioBase.add(recargo)
        );

        boleto = boletoRepository.save(boleto);

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

        for (BoletoSegmento boletoSegmento : segmentos) {

            boletoSegmento.setEstadoBoletoId(
                    estadoAbordado.getId()
            );

            boletoSegmentoRepository.save(boletoSegmento);

            List<BoletoAsiento> asientos = boletoAsientoRepository
                    .findByBoletoSegmentoId(boletoSegmento.getId());

            for (BoletoAsiento boletoAsiento : asientos) {

                if (boletoAsiento.getAsientoVueloId() == null) {
                    continue;
                }

                asientoVueloRepository.findById(boletoAsiento.getAsientoVueloId())
                        .ifPresent(asientoVuelo -> {
                            asientoVuelo.setEstadoAsientoId(estadoOcupado.getId());
                            asientoVueloRepository.save(asientoVuelo);
                        });
            }
        }

        return mapResponse(
                boleto,
                cantidadPresentada,
                "Pasajero abordado correctamente"
        );
    }

    @Override
    @Transactional
    public FinalizarAbordajeResponse finalizarAbordaje(
            Integer vueloOperadoId
    ) {

        if (vueloOperadoId == null) {
            throw new BusinessException("Debe ingresar el vuelo operado");
        }

        VueloOperado vueloOperado = vueloOperadoRepository.findById(vueloOperadoId)
                .orElseThrow(() -> new BusinessException("Vuelo operado no encontrado"));

        EstadoBoleto estadoPendiente = obtenerEstadoBoleto(
                ESTADO_BOLETO_PENDIENTE
        );

        EstadoBoleto estadoAbordado = obtenerEstadoBoleto(
                ESTADO_BOLETO_ABORDADO
        );

        EstadoBoleto estadoCancelado = obtenerEstadoBoleto(
                ESTADO_BOLETO_CANCELADO
        );

        EstadoAsiento estadoBloqueado = estadoAsientoRepository
                .findByNombreIgnoreCase(ESTADO_ASIENTO_BLOQUEADO)
                .orElseThrow(() -> new BusinessException("Estado de asiento BLOQUEADO no encontrado"));

        EstadoEquipaje estadoEquipajeCancelado = estadoEquipajeRepository
                .findByNombreIgnoreCase(ESTADO_EQUIPAJE_CANCELADO)
                .orElseThrow(() -> new BusinessException("Estado de equipaje CANCELADO no encontrado"));

        EstadoVuelo estadoVueloAbordado = estadoVueloRepository
                .findByNombreIgnoreCase(ESTADO_VUELO_ABORDADO)
                .orElseThrow(() -> new BusinessException("Estado de vuelo ABORDANDO no encontrado"));

        List<Boleto> boletos = boletoRepository
                .findByVueloOperadoIdAndEstadoId(
                        vueloOperadoId,
                        1
                );

        int abordados = 0;
        int cancelados = 0;

        for (Boleto boleto : boletos) {

            if (Objects.equals(boleto.getEstadoBoletoId(), estadoAbordado.getId())) {
                abordados++;
                continue;
            }

            if (Objects.equals(boleto.getEstadoBoletoId(), estadoPendiente.getId())) {

                boleto.setEstadoBoletoId(
                        estadoCancelado.getId()
                );

                boletoRepository.save(boleto);

                cancelados++;

                List<BoletoSegmento> segmentos = boletoSegmentoRepository
                        .findByBoletoIdOrderByOrdenSegmentoAsc(boleto.getId());

                for (BoletoSegmento boletoSegmento : segmentos) {

                    boletoSegmento.setEstadoBoletoId(
                            estadoCancelado.getId()
                    );

                    boletoSegmentoRepository.save(boletoSegmento);

                    List<BoletoAsiento> asientos = boletoAsientoRepository
                            .findByBoletoSegmentoId(boletoSegmento.getId());

                    for (BoletoAsiento boletoAsiento : asientos) {

                        if (boletoAsiento.getAsientoVueloId() == null) {
                            continue;
                        }

                        asientoVueloRepository.findById(boletoAsiento.getAsientoVueloId())
                                .ifPresent(asientoVuelo -> {
                                    asientoVuelo.setEstadoAsientoId(estadoBloqueado.getId());
                                    asientoVueloRepository.save(asientoVuelo);
                                });
                    }
                }

                List<Equipaje> equipajes = equipajeRepository
                        .findByBoletoId(boleto.getId());

                for (Equipaje equipaje : equipajes) {
                    equipaje.setEstadoEquipajeId(estadoEquipajeCancelado.getId());
                    equipajeRepository.save(equipaje);
                }
            }
        }

        vueloOperado.setEstadoVueloId(
                estadoVueloAbordado.getId()
        );

        vueloOperadoRepository.save(vueloOperado);

        List<SegmentoOperado> segmentosOperados = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(
                        vueloOperadoId
                );

        for (SegmentoOperado segmentoOperado : segmentosOperados) {
            segmentoOperado.setEstadoVueloId(
                    estadoVueloAbordado.getId()
            );

            segmentoOperadoRepository.save(segmentoOperado);
        }

        FinalizarAbordajeResponse response = new FinalizarAbordajeResponse();

        response.setVueloOperadoId(vueloOperado.getId());
        response.setEstadoVuelo(estadoVueloAbordado.getNombre());
        response.setBoletosAbordados(abordados);
        response.setBoletosCancelados(cancelados);
        response.setMensaje("Se completó el abordaje");

        return response;
    }

    private void validarRequest(
            AbordajeRequest request
    ) {

        if (request == null ||
                request.getVueloOperadoId() == null ||
                request.getPasaporte() == null ||
                request.getPasaporte().isBlank() ||
                request.getCantidadMaletasPresentadas() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getCantidadMaletasPresentadas() < 0) {
            throw new BusinessException("La cantidad de maletas no puede ser negativa");
        }
    }

    private Boleto obtenerBoletoPorPasaporteYVuelo(
            Integer vueloOperadoId,
            String pasaporte
    ) {

        if (vueloOperadoId == null ||
                pasaporte == null ||
                pasaporte.isBlank()) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        vueloOperadoRepository.findById(vueloOperadoId)
                .orElseThrow(() -> new BusinessException("Vuelo operado no encontrado"));

        Pasajero pasajero = pasajeroRepository
                .findByPasaporte(pasaporte.trim())
                .orElseThrow(() -> new BusinessException("Pasajero no encontrado"));

        EstadoBoleto estadoPendiente = obtenerEstadoBoleto(
                ESTADO_BOLETO_PENDIENTE
        );

        return boletoRepository
                .findFirstByPasajeroIdAndVueloOperadoIdAndEstadoBoletoIdAndEstadoIdOrderByIdDesc(
                        pasajero.getId(),
                        vueloOperadoId,
                        estadoPendiente.getId(),
                        1
                )
                .orElseThrow(() ->
                        new BusinessException("El pasajero no se encuentra registrado en el vuelo o no tiene boleto pendiente de abordar")
                );
    }

    private EstadoBoleto obtenerEstadoBoleto(
            String nombre
    ) {

        return estadoBoletoRepository
                .findByNombreIgnoreCase(nombre)
                .orElseThrow(() ->
                        new BusinessException("Estado de boleto no encontrado: " + nombre)
                );
    }

    private Integer obtenerPrimerSegmentoOperadoId(
            Boleto boleto
    ) {

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(
                        boleto.getId()
                );

        if (segmentos.isEmpty()) {
            return null;
        }

        return segmentos.get(0).getSegmentoOperadoId();
    }

    private AbordajeResponse mapResponse(
            Boleto boleto,
            Integer cantidadMaletasPresentadas,
            String mensaje
    ) {

        AbordajeResponse response = new AbordajeResponse();

        response.setBoletoId(boleto.getId());
        response.setCodigoBoleto(boleto.getCodigoBoleto());
        response.setCodigoPaseAbordar(boleto.getCodigoPaseAbordar());
        response.setPasajeroId(boleto.getPasajeroId());
        response.setVueloOperadoId(boleto.getVueloOperadoId());
        response.setRecargoEquipaje(boleto.getRecargoEquipaje());
        response.setTotal(boleto.getTotal());
        response.setMensaje(mensaje);

        pasajeroRepository.findById(boleto.getPasajeroId())
                .ifPresent(pasajero -> {
                    response.setNombrePasajero(pasajero.getNombreCompleto());
                    response.setPasaporte(pasajero.getPasaporte());
                });

        if (boleto.getEstadoBoletoId() != null) {
            estadoBoletoRepository.findById(boleto.getEstadoBoletoId())
                    .ifPresent(estado ->
                            response.setEstadoBoleto(estado.getNombre())
                    );
        }

        List<Equipaje> equipajes = equipajeRepository
                .findByBoletoId(boleto.getId());

        response.setCantidadMaletasRegistradas(
                equipajes.size()
        );

        response.setCantidadMaletasPresentadas(
                cantidadMaletasPresentadas
        );

        List<BoletoSegmento> segmentos = boletoSegmentoRepository
                .findByBoletoIdOrderByOrdenSegmentoAsc(
                        boleto.getId()
                );

        if (!segmentos.isEmpty()) {

            List<BoletoAsiento> asientos = boletoAsientoRepository
                    .findByBoletoSegmentoId(
                            segmentos.get(0).getId()
                    );

            if (!asientos.isEmpty()) {

                Integer asientoVueloId = asientos.get(0).getAsientoVueloId();

                if (asientoVueloId != null) {

                    asientoVueloRepository.findById(asientoVueloId)
                            .ifPresent(asientoVuelo -> {

                                if (asientoVuelo.getCodigoAsientoSistema() != null) {

                                    asientoUbiRepository
                                            .findFirstByCodigoAsientoSistemaOrderByIdAsc(
                                                    asientoVuelo.getCodigoAsientoSistema()
                                            )
                                            .ifPresent(asientoUbi ->
                                                    response.setAsiento(asientoUbi.getNumeroAsiento())
                                            );
                                }
                            });
                }
            }
        }

        return response;
    }
}