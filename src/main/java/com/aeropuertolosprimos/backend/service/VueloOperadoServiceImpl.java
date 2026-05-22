package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.SegmentoOperadoResponse;
import com.aeropuertolosprimos.backend.dto.VueloOperadoRequest;
import com.aeropuertolosprimos.backend.dto.VueloOperadoResponse;
import com.aeropuertolosprimos.backend.dto.VueloOperadoSegmentoRequest;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import com.aeropuertolosprimos.backend.specification.VueloOperadoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VueloOperadoServiceImpl implements VueloOperadoService {

    private static final String ACTIVO = "ACTIVO";
    private static final String INACTIVO = "INACTIVO";

    private static final String DISPONIBLE = "DISPONIBLE";
    private static final String ASIGNADO = "ASIGNADO";
    private static final String ASIGNADA = "ASIGNADA";

    private static final String PROGRAMADO = "PROGRAMADO";
    private static final String ABORDANDO = "ABORDANDO";
    private static final String EN_VUELO = "EN_VUELO";
    private static final String ATERRIZADO = "ATERRIZADO";
    private static final String RETRASADO = "RETRASADO";
    private static final String CANCELADO = "CANCELADO";
    private static final String EN_ESCALA = "EN_ESCALA";
    private static final String FINALIZADO = "FINALIZADO";

    private static final String DIRECTO = "DIRECTO";
    private static final String TECNICO = "TECNICO";
    private static final String CAMBIO_AVION = "CAMBIO_AVION";

    private final VueloOperadoRepository vueloOperadoRepository;
    private final SegmentoVueloRepository segmentoVueloRepository;
    private final SegmentoOperadoRepository segmentoOperadoRepository;

    private final VueloProgramadoRepository vueloProgramadoRepository;
    private final VueloRepository vueloRepository;
    private final AvionRepository avionRepository;
    private final TripulacionRepository tripulacionRepository;

    private final EstadoVueloRepository estadoVueloRepository;
    private final EstadoAvionRepository estadoAvionRepository;
    private final EstadoTripulacionRepository estadoTripulacionRepository;
    private final TipoSegmentoVueloRepository tipoSegmentoVueloRepository;
    private final StatusCatalogRepository statusCatalogRepository;

    private final AerolineaRepository aerolineaRepository;
    private final AeropuertoRepository aeropuertoRepository;

    private final AsientoVueloService asientoVueloService;

    @Override
    @Transactional(readOnly = true)
    public Page<VueloOperadoResponse> findAll(
            Integer vueloProgramadoId,
            Integer avionId,
            Integer tripulacionId,
            Integer estadoVueloId,
            LocalDate fechaSalidaReal,
            LocalDate fechaLlegadaReal,
            Pageable pageable
    ) {
        return vueloOperadoRepository
                .findAll(
                        VueloOperadoSpecification.filters(
                                vueloProgramadoId,
                                avionId,
                                tripulacionId,
                                estadoVueloId,
                                fechaSalidaReal,
                                fechaLlegadaReal
                        ),
                        pageable
                )
                .map(this::mapResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VueloOperadoResponse findById(Integer id) {
        VueloOperado vueloOperado = obtenerVueloOperado(id);
        return mapResponse(vueloOperado);
    }

    @Override
    @Transactional
    public VueloOperadoResponse create(VueloOperadoRequest request) {
        validarRequest(request);

        VueloProgramado programado = obtenerVueloProgramado(request.getVueloProgramadoId());
        Vuelo vuelo = obtenerVuelo(programado.getVueloId());

        validarVueloActivo(vuelo);

        if (vueloOperadoRepository.existsByVueloProgramadoId(programado.getId())) {
            throw new BusinessException("El vuelo programado ya tiene una operación registrada");
        }

        TipoSegmentoVuelo tipo = obtenerTipoSegmento(request.getTipoSegmentoVueloId());
        String tipoNombre = normalizar(tipo.getNombre());

        validarSegmentosRequest(request, programado, tipoNombre);

        VueloOperado vueloOperado = new VueloOperado();
        vueloOperado.setVueloProgramadoId(programado.getId());
        vueloOperado.setTipoSegmentoVueloId(tipo.getId());
        vueloOperado.setEstadoVueloId(estadoVueloId(PROGRAMADO));
        vueloOperado.setCantidadSegmentos(request.getCantidadSegmentos());
        vueloOperado.setSegmentoActualOrden(1);
        vueloOperado.setTuvoEscala(false);

        VueloOperado guardado = vueloOperadoRepository.save(vueloOperado);

        crearSegmentos(
                guardado,
                programado,
                vuelo,
                tipo,
                request.getSegmentos()
        );

        asignarRecursosDeSegmentos(guardado.getId());

        asientoVueloService.generarAsientosParaVueloOperado(
                guardado.getId()
        );

        return mapResponse(guardado);
    }

    @Override
    @Transactional
    public VueloOperadoResponse update(
            Integer id,
            VueloOperadoRequest request
    ) {
        validarRequest(request);

        VueloOperado actual = obtenerVueloOperado(id);

        String estadoActual = nombreEstadoVuelo(actual.getEstadoVueloId());

        if (!estadoActual.equals(PROGRAMADO) && !estadoActual.equals(CANCELADO)) {
            throw new BusinessException("Solo se puede editar un vuelo operado en estado PROGRAMADO o CANCELADO");
        }

        VueloProgramado programado = obtenerVueloProgramado(request.getVueloProgramadoId());
        Vuelo vuelo = obtenerVuelo(programado.getVueloId());

        if (!estadoActual.equals(CANCELADO)) {
            validarVueloActivo(vuelo);
        }

        if (vueloOperadoRepository.existsByVueloProgramadoIdAndIdNot(
                programado.getId(),
                actual.getId()
        )) {
            throw new BusinessException("El vuelo programado ya tiene una operación registrada");
        }

        TipoSegmentoVuelo tipo = obtenerTipoSegmento(request.getTipoSegmentoVueloId());
        String tipoNombre = normalizar(tipo.getNombre());

        validarSegmentosRequest(request, programado, tipoNombre);

        if (estadoActual.equals(PROGRAMADO)) {
            liberarRecursosDeSegmentos(actual.getId());
        }

        segmentoOperadoRepository.deleteByVueloOperadoId(actual.getId());
        segmentoVueloRepository.deleteByVueloProgramadoId(actual.getVueloProgramadoId());

        actual.setVueloProgramadoId(programado.getId());
        actual.setTipoSegmentoVueloId(tipo.getId());
        actual.setCantidadSegmentos(request.getCantidadSegmentos());
        actual.setSegmentoActualOrden(1);
        actual.setTuvoEscala(false);

        VueloOperado actualizado = vueloOperadoRepository.save(actual);

        crearSegmentos(
                actualizado,
                programado,
                vuelo,
                tipo,
                request.getSegmentos()
        );

        if (estadoActual.equals(PROGRAMADO)) {
            asignarRecursosDeSegmentos(actualizado.getId());

            asientoVueloService.generarAsientosParaVueloOperado(
                    actualizado.getId()
            );
        }

        return mapResponse(actualizado);
    }

    @Override
    @Transactional
    public VueloOperadoResponse cambiarEstado(
            Integer id,
            Integer estadoVueloId
    ) {
        if (estadoVueloId == null) {
            throw new BusinessException("Debe seleccionar un estado de vuelo");
        }

        VueloOperado vueloOperado = obtenerVueloOperado(id);

        EstadoVuelo estadoNuevo = estadoVueloRepository
                .findById(estadoVueloId)
                .orElseThrow(() -> new BusinessException("Estado de vuelo no encontrado"));

        String nuevo = normalizar(estadoNuevo.getNombre());

        validarTransicion(vueloOperado, nuevo);
        aplicarEstado(vueloOperado, nuevo);

        VueloOperado actualizado = vueloOperadoRepository.save(vueloOperado);

        return mapResponse(actualizado);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        cambiarEstado(id, estadoVueloId(CANCELADO));
    }

    private void aplicarEstado(
            VueloOperado vueloOperado,
            String nuevo
    ) {
        if (nuevo.equals(PROGRAMADO)) {
            reactivarCancelado(vueloOperado);
            return;
        }

        if (nuevo.equals(CANCELADO)) {
            cancelarVuelo(vueloOperado);
            return;
        }

        if (nuevo.equals(FINALIZADO)) {
            finalizarVuelo(vueloOperado);
            return;
        }

        if (nuevo.equals(ABORDANDO)) {
            SegmentoOperado segmento = obtenerSegmentoParaCambio(vueloOperado, nuevo);
            segmento.setEstadoVueloId(estadoVueloId(ABORDANDO));
            segmentoOperadoRepository.save(segmento);

            vueloOperado.setEstadoVueloId(estadoVueloId(ABORDANDO));
            return;
        }

        if (nuevo.equals(RETRASADO)) {
            SegmentoOperado segmento = obtenerSegmentoActual(vueloOperado);
            segmento.setEstadoVueloId(estadoVueloId(RETRASADO));
            segmentoOperadoRepository.save(segmento);

            vueloOperado.setEstadoVueloId(estadoVueloId(RETRASADO));
            return;
        }

        if (nuevo.equals(EN_VUELO)) {
            SegmentoOperado segmento = obtenerSegmentoParaCambio(vueloOperado, nuevo);

            segmento.setEstadoVueloId(estadoVueloId(EN_VUELO));

            if (segmento.getFechaSalidaReal() == null) {
                segmento.setFechaSalidaReal(LocalDate.now());
            }

            if (segmento.getHoraSalidaReal() == null) {
                segmento.setHoraSalidaReal(LocalTime.now());
            }

            segmentoOperadoRepository.save(segmento);

            vueloOperado.setEstadoVueloId(estadoVueloId(EN_VUELO));
            return;
        }

        if (nuevo.equals(ATERRIZADO)) {
            SegmentoOperado segmento = obtenerSegmentoActual(vueloOperado);

            segmento.setEstadoVueloId(estadoVueloId(ATERRIZADO));
            segmento.setFechaLlegadaReal(LocalDate.now());
            segmento.setHoraLlegadaReal(LocalTime.now());

            segmentoOperadoRepository.save(segmento);

            if (vueloOperado.getSegmentoActualOrden() < vueloOperado.getCantidadSegmentos()) {
                vueloOperado.setTuvoEscala(true);
                vueloOperado.setEstadoVueloId(estadoVueloId(EN_ESCALA));
            } else {
                vueloOperado.setEstadoVueloId(estadoVueloId(ATERRIZADO));
            }
        }
    }

    private SegmentoOperado obtenerSegmentoParaCambio(
            VueloOperado vueloOperado,
            String nuevoEstado
    ) {
        String estadoActual = nombreEstadoVuelo(vueloOperado.getEstadoVueloId());

        if (estadoActual.equals(EN_ESCALA)) {
            if (vueloOperado.getSegmentoActualOrden() >= vueloOperado.getCantidadSegmentos()) {
                throw new BusinessException("No hay más segmentos pendientes");
            }

            Integer nuevoOrden = vueloOperado.getSegmentoActualOrden() + 1;
            vueloOperado.setSegmentoActualOrden(nuevoOrden);

            return obtenerSegmentoPorOrden(vueloOperado.getId(), nuevoOrden);
        }

        return obtenerSegmentoActual(vueloOperado);
    }

    private void validarTransicion(
            VueloOperado vueloOperado,
            String nuevo
    ) {
        String actual = nombreEstadoVuelo(vueloOperado.getEstadoVueloId());

        if (actual.equals(FINALIZADO)) {
            throw new BusinessException("No se puede cambiar el estado de un vuelo finalizado");
        }

        if (actual.equals(CANCELADO)) {
            if (nuevo.equals(PROGRAMADO)) {
                return;
            }

            throw new BusinessException("El vuelo cancelado solo puede reactivarse a PROGRAMADO");
        }

        if (nuevo.equals(CANCELADO)) {
            if (
                    actual.equals(PROGRAMADO) ||
                            actual.equals(RETRASADO) ||
                            actual.equals(ABORDANDO)
            ) {
                return;
            }

            if (actual.equals(EN_VUELO)) {
                throw new BusinessException("No se puede cancelar un vuelo que ya está en vuelo");
            }

            if (actual.equals(EN_ESCALA)) {
                throw new BusinessException("No se puede cancelar un vuelo que está en escala");
            }

            if (actual.equals(ATERRIZADO)) {
                throw new BusinessException("No se puede cancelar un vuelo aterrizado");
            }

            throw new BusinessException("No se puede cancelar el vuelo en el estado actual");
        }

        if (actual.equals(PROGRAMADO)) {
            if (nuevo.equals(ABORDANDO) || nuevo.equals(RETRASADO)) {
                return;
            }
        }

        if (actual.equals(RETRASADO)) {
            if (
                    nuevo.equals(PROGRAMADO) ||
                            nuevo.equals(ABORDANDO) ||
                            nuevo.equals(EN_VUELO)
            ) {
                return;
            }
        }

        if (actual.equals(ABORDANDO)) {
            if (nuevo.equals(EN_VUELO) || nuevo.equals(RETRASADO)) {
                return;
            }
        }

        if (actual.equals(EN_VUELO)) {
            if (nuevo.equals(ATERRIZADO)) {
                return;
            }
        }

        if (actual.equals(EN_ESCALA)) {
            TipoSegmentoVuelo tipo = obtenerTipoSegmento(vueloOperado.getTipoSegmentoVueloId());
            String tipoNombre = normalizar(tipo.getNombre());

            if (tipoNombre.equals(TECNICO) && nuevo.equals(EN_VUELO)) {
                return;
            }

            if (tipoNombre.equals(CAMBIO_AVION) && nuevo.equals(ABORDANDO)) {
                return;
            }

            throw new BusinessException("La transición desde EN_ESCALA no es válida para el tipo de vuelo");
        }

        if (actual.equals(ATERRIZADO)) {
            if (nuevo.equals(FINALIZADO)) {
                if (vueloOperado.getSegmentoActualOrden() < vueloOperado.getCantidadSegmentos()) {
                    throw new BusinessException("No se puede finalizar porque hay segmentos pendientes");
                }

                return;
            }
        }

        throw new BusinessException("Transición de estado no permitida");
    }

    private void cancelarVuelo(VueloOperado vueloOperado) {
        List<SegmentoOperado> segmentos = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(vueloOperado.getId());

        Integer canceladoId = estadoVueloId(CANCELADO);

        for (SegmentoOperado segmento : segmentos) {
            segmento.setEstadoVueloId(canceladoId);
            segmentoOperadoRepository.save(segmento);
        }

        vueloOperado.setEstadoVueloId(canceladoId);

        inactivarVueloBase(vueloOperado);
        liberarRecursosDeSegmentos(vueloOperado.getId());
    }

    private void finalizarVuelo(VueloOperado vueloOperado) {
        vueloOperado.setEstadoVueloId(estadoVueloId(FINALIZADO));

        inactivarVueloBase(vueloOperado);
        liberarRecursosDeSegmentos(vueloOperado.getId());
    }

    private void reactivarCancelado(VueloOperado vueloOperado) {
        VueloProgramado programado = obtenerVueloProgramado(vueloOperado.getVueloProgramadoId());
        Vuelo vuelo = obtenerVuelo(programado.getVueloId());

        List<SegmentoOperado> segmentos = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(vueloOperado.getId());

        for (SegmentoOperado segmento : segmentos) {
            validarAvionDisponible(obtenerAvion(segmento.getAvionId()), vuelo);
            validarTripulacionDisponible(obtenerTripulacion(segmento.getTripulacionId()), vuelo);
        }

        Integer programadoId = estadoVueloId(PROGRAMADO);

        for (SegmentoOperado segmento : segmentos) {
            segmento.setEstadoVueloId(programadoId);
            segmento.setFechaSalidaReal(null);
            segmento.setHoraSalidaReal(null);
            segmento.setFechaLlegadaReal(null);
            segmento.setHoraLlegadaReal(null);
            segmentoOperadoRepository.save(segmento);
        }

        vuelo.setEstadoId(statusId(ACTIVO));
        vueloRepository.save(vuelo);

        asignarRecursosDeSegmentos(vueloOperado.getId());

        vueloOperado.setEstadoVueloId(programadoId);
        vueloOperado.setSegmentoActualOrden(1);
        vueloOperado.setTuvoEscala(false);
    }

    private void crearSegmentos(
            VueloOperado vueloOperado,
            VueloProgramado programado,
            Vuelo vuelo,
            TipoSegmentoVuelo tipo,
            List<VueloOperadoSegmentoRequest> segmentosRequest
    ) {
        for (VueloOperadoSegmentoRequest sr : segmentosRequest) {
            Avion avion = obtenerAvion(sr.getAvionId());
            Tripulacion tripulacion = obtenerTripulacion(sr.getTripulacionId());

            validarAvionDisponible(avion, vuelo);
            validarTripulacionDisponible(tripulacion, vuelo);

            SegmentoVuelo segmentoVuelo = new SegmentoVuelo();
            segmentoVuelo.setVueloProgramadoId(programado.getId());
            segmentoVuelo.setOrdenSegmento(sr.getOrdenSegmento());
            segmentoVuelo.setAeropuertoSalidaId(sr.getAeropuertoSalidaId());
            segmentoVuelo.setAeropuertoLlegadaId(sr.getAeropuertoLlegadaId());
            segmentoVuelo.setTipoSegmentoVueloId(tipo.getId());
            segmentoVuelo.setFechaSalida(sr.getFechaSalida());
            segmentoVuelo.setHoraSalida(sr.getHoraSalida());
            segmentoVuelo.setFechaLlegada(sr.getFechaLlegada());
            segmentoVuelo.setHoraLlegada(sr.getHoraLlegada());
            segmentoVuelo.setEstadoId(statusId(ACTIVO));

            SegmentoVuelo svGuardado = segmentoVueloRepository.save(segmentoVuelo);

            SegmentoOperado segmentoOperado = new SegmentoOperado();
            segmentoOperado.setVueloOperadoId(vueloOperado.getId());
            segmentoOperado.setSegmentoVueloId(svGuardado.getId());
            segmentoOperado.setOrdenSegmento(sr.getOrdenSegmento());
            segmentoOperado.setAvionId(avion.getId());
            segmentoOperado.setTripulacionId(tripulacion.getId());
            segmentoOperado.setEstadoVueloId(estadoVueloId(PROGRAMADO));

            segmentoOperadoRepository.save(segmentoOperado);
        }
    }

    private void validarRequest(VueloOperadoRequest request) {
        if (
                request == null ||
                        request.getVueloProgramadoId() == null ||
                        request.getTipoSegmentoVueloId() == null ||
                        request.getCantidadSegmentos() == null ||
                        request.getSegmentos() == null ||
                        request.getSegmentos().isEmpty()
        ) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getCantidadSegmentos() < 1 || request.getCantidadSegmentos() > 3) {
            throw new BusinessException("El vuelo operado solo puede tener entre 1 y 3 segmentos");
        }

        if (!request.getCantidadSegmentos().equals(request.getSegmentos().size())) {
            throw new BusinessException("La cantidad de segmentos no coincide con los segmentos ingresados");
        }
    }

    private void validarSegmentosRequest(
            VueloOperadoRequest request,
            VueloProgramado programado,
            String tipoNombre
    ) {
        List<VueloOperadoSegmentoRequest> segmentos = request.getSegmentos()
                .stream()
                .sorted(Comparator.comparing(VueloOperadoSegmentoRequest::getOrdenSegmento))
                .toList();

        Set<Integer> ordenes = new HashSet<>();

        for (VueloOperadoSegmentoRequest s : segmentos) {
            if (
                    s.getOrdenSegmento() == null ||
                            s.getAeropuertoSalidaId() == null ||
                            s.getAeropuertoLlegadaId() == null ||
                            s.getAvionId() == null ||
                            s.getTripulacionId() == null
            ) {
                throw new BusinessException("Debe ingresar los campos obligatorios de cada segmento");
            }

            if (s.getOrdenSegmento() < 1 || s.getOrdenSegmento() > 3) {
                throw new BusinessException("El orden de segmento debe estar entre 1 y 3");
            }

            if (!ordenes.add(s.getOrdenSegmento())) {
                throw new BusinessException("No puede repetir el orden de segmento");
            }

            if (s.getAeropuertoSalidaId().equals(s.getAeropuertoLlegadaId())) {
                throw new BusinessException("No se puede seleccionar el mismo aeropuerto de salida y llegada en un segmento");
            }
        }

        if (tipoNombre.equals(DIRECTO) && request.getCantidadSegmentos() != 1) {
            throw new BusinessException("Un vuelo directo solo puede tener 1 segmento");
        }

        if (
                (tipoNombre.equals(TECNICO) || tipoNombre.equals(CAMBIO_AVION)) &&
                        (request.getCantidadSegmentos() < 2 || request.getCantidadSegmentos() > 3)
        ) {
            throw new BusinessException("Los vuelos con escala deben tener entre 2 y 3 segmentos");
        }

        VueloOperadoSegmentoRequest primero = segmentos.get(0);
        VueloOperadoSegmentoRequest ultimo = segmentos.get(segmentos.size() - 1);

        if (!primero.getAeropuertoSalidaId().equals(programado.getAeropuertoSalidaId())) {
            throw new BusinessException("El primer segmento debe iniciar en el aeropuerto de salida del vuelo programado");
        }

        if (!ultimo.getAeropuertoLlegadaId().equals(programado.getAeropuertoLlegadaId())) {
            throw new BusinessException("El último segmento debe llegar al aeropuerto de llegada del vuelo programado");
        }

        for (int i = 0; i < segmentos.size() - 1; i++) {
            VueloOperadoSegmentoRequest actual = segmentos.get(i);
            VueloOperadoSegmentoRequest siguiente = segmentos.get(i + 1);

            if (!actual.getAeropuertoLlegadaId().equals(siguiente.getAeropuertoSalidaId())) {
                throw new BusinessException("Los segmentos deben estar conectados por aeropuerto");
            }
        }

        if (tipoNombre.equals(TECNICO)) {
            Integer avionBase = primero.getAvionId();
            Integer tripulacionBase = primero.getTripulacionId();

            boolean distintoAvion = segmentos.stream()
                    .anyMatch(s -> !s.getAvionId().equals(avionBase));

            boolean distintaTripulacion = segmentos.stream()
                    .anyMatch(s -> !s.getTripulacionId().equals(tripulacionBase));

            if (distintoAvion) {
                throw new BusinessException("Una escala técnica debe usar el mismo avión en todos los segmentos");
            }

            if (distintaTripulacion) {
                throw new BusinessException("Una escala técnica debe usar la misma tripulación en todos los segmentos");
            }
        }

        if (tipoNombre.equals(CAMBIO_AVION)) {
            Integer avionBase = primero.getAvionId();

            boolean existeCambioAvion = segmentos.stream()
                    .anyMatch(s -> !s.getAvionId().equals(avionBase));

            if (!existeCambioAvion) {
                throw new BusinessException("Un vuelo con cambio de avión debe tener al menos un segmento con avión distinto");
            }
        }
    }

    private void validarVueloActivo(Vuelo vuelo) {
        if (vuelo.getEstadoId() == null || !vuelo.getEstadoId().equals(statusId(ACTIVO))) {
            throw new BusinessException("El vuelo programado no está activo");
        }
    }

    private void validarAvionDisponible(
            Avion avion,
            Vuelo vuelo
    ) {
        if (avion.getEstadoId() == null || !avion.getEstadoId().equals(statusId(ACTIVO))) {
            throw new BusinessException("El avión está inactivo");
        }

        if (avion.getEstadoAvionId() == null || !avion.getEstadoAvionId().equals(estadoAvionId(DISPONIBLE))) {
            throw new BusinessException("El avión no está disponible");
        }

        if (avion.getAerolineaId() == null || !avion.getAerolineaId().equals(vuelo.getAerolineaId())) {
            throw new BusinessException("El avión no pertenece a la aerolínea del vuelo");
        }
    }

    private void validarTripulacionDisponible(
            Tripulacion tripulacion,
            Vuelo vuelo
    ) {
        if (
                tripulacion.getEstadoTripulacionId() == null ||
                        !tripulacion.getEstadoTripulacionId().equals(estadoTripulacionDisponibleId())
        ) {
            throw new BusinessException("La tripulación no está disponible");
        }

        if (
                tripulacion.getAerolineaId() == null ||
                        !tripulacion.getAerolineaId().equals(vuelo.getAerolineaId())
        ) {
            throw new BusinessException("La tripulación no pertenece a la aerolínea del vuelo");
        }
    }

    private void asignarRecursosDeSegmentos(Integer vueloOperadoId) {
        List<SegmentoOperado> segmentos = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(vueloOperadoId);

        Set<Integer> aviones = segmentos.stream()
                .map(SegmentoOperado::getAvionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Integer> tripulaciones = segmentos.stream()
                .map(SegmentoOperado::getTripulacionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Integer avionId : aviones) {
            Avion avion = obtenerAvion(avionId);
            avion.setEstadoAvionId(estadoAvionId(ASIGNADO));
            avionRepository.save(avion);
        }

        for (Integer tripulacionId : tripulaciones) {
            Tripulacion tripulacion = obtenerTripulacion(tripulacionId);
            tripulacion.setEstadoTripulacionId(estadoTripulacionAsignadaId());
            tripulacionRepository.save(tripulacion);
        }
    }

    private void liberarRecursosDeSegmentos(Integer vueloOperadoId) {
        List<SegmentoOperado> segmentos = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(vueloOperadoId);

        Set<Integer> aviones = segmentos.stream()
                .map(SegmentoOperado::getAvionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Integer> tripulaciones = segmentos.stream()
                .map(SegmentoOperado::getTripulacionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Integer avionId : aviones) {
            Avion avion = obtenerAvion(avionId);
            avion.setEstadoAvionId(estadoAvionId(DISPONIBLE));
            avionRepository.save(avion);
        }

        for (Integer tripulacionId : tripulaciones) {
            Tripulacion tripulacion = obtenerTripulacion(tripulacionId);
            tripulacion.setEstadoTripulacionId(estadoTripulacionDisponibleId());
            tripulacionRepository.save(tripulacion);
        }
    }

    private void inactivarVueloBase(VueloOperado vueloOperado) {
        VueloProgramado programado = obtenerVueloProgramado(vueloOperado.getVueloProgramadoId());
        Vuelo vuelo = obtenerVuelo(programado.getVueloId());

        vuelo.setEstadoId(statusId(INACTIVO));
        vueloRepository.save(vuelo);
    }

    private VueloOperadoResponse mapResponse(VueloOperado vueloOperado) {
        VueloOperadoResponse response = new VueloOperadoResponse();

        response.setId(vueloOperado.getId());
        response.setVueloProgramadoId(vueloOperado.getVueloProgramadoId());
        response.setTipoSegmentoVueloId(vueloOperado.getTipoSegmentoVueloId());
        response.setEstadoVueloId(vueloOperado.getEstadoVueloId());
        response.setCantidadSegmentos(vueloOperado.getCantidadSegmentos());
        response.setSegmentoActualOrden(vueloOperado.getSegmentoActualOrden());
        response.setTuvoEscala(vueloOperado.getTuvoEscala());
        response.setCreatedAt(vueloOperado.getCreatedAt());
        response.setUpdatedAt(vueloOperado.getUpdatedAt());

        mapVueloProgramado(response, vueloOperado);
        mapTipoSegmento(response, vueloOperado);
        mapEstadoVuelo(response, vueloOperado);
        mapPermisos(response, vueloOperado);

        List<SegmentoOperadoResponse> segmentos = segmentoOperadoRepository
                .findByVueloOperadoIdOrderByOrdenSegmentoAsc(vueloOperado.getId())
                .stream()
                .map(this::mapSegmentoResponse)
                .toList();

        response.setSegmentos(segmentos);

        return response;
    }

    private SegmentoOperadoResponse mapSegmentoResponse(SegmentoOperado segmentoOperado) {
        SegmentoOperadoResponse response = new SegmentoOperadoResponse();

        response.setId(segmentoOperado.getId());
        response.setVueloOperadoId(segmentoOperado.getVueloOperadoId());
        response.setSegmentoVueloId(segmentoOperado.getSegmentoVueloId());
        response.setOrdenSegmento(segmentoOperado.getOrdenSegmento());
        response.setAvionId(segmentoOperado.getAvionId());
        response.setTripulacionId(segmentoOperado.getTripulacionId());
        response.setEstadoVueloId(segmentoOperado.getEstadoVueloId());
        response.setFechaSalidaReal(segmentoOperado.getFechaSalidaReal());
        response.setHoraSalidaReal(segmentoOperado.getHoraSalidaReal());
        response.setFechaLlegadaReal(segmentoOperado.getFechaLlegadaReal());
        response.setHoraLlegadaReal(segmentoOperado.getHoraLlegadaReal());
        response.setCreatedAt(segmentoOperado.getCreatedAt());
        response.setUpdatedAt(segmentoOperado.getUpdatedAt());

        segmentoVueloRepository.findById(segmentoOperado.getSegmentoVueloId())
                .ifPresent(segmentoVuelo -> {
                    response.setAeropuertoSalidaId(segmentoVuelo.getAeropuertoSalidaId());
                    response.setAeropuertoLlegadaId(segmentoVuelo.getAeropuertoLlegadaId());
                    response.setTipoSegmentoVueloId(segmentoVuelo.getTipoSegmentoVueloId());
                    response.setFechaSalida(segmentoVuelo.getFechaSalida());
                    response.setHoraSalida(segmentoVuelo.getHoraSalida());
                    response.setFechaLlegada(segmentoVuelo.getFechaLlegada());
                    response.setHoraLlegada(segmentoVuelo.getHoraLlegada());

                    aeropuertoRepository.findById(segmentoVuelo.getAeropuertoSalidaId())
                            .ifPresent(a -> {
                                response.setAeropuertoSalidaNombre(a.getNombre());
                                response.setAeropuertoSalidaCodigoIata(a.getCodigoIata());
                            });

                    aeropuertoRepository.findById(segmentoVuelo.getAeropuertoLlegadaId())
                            .ifPresent(a -> {
                                response.setAeropuertoLlegadaNombre(a.getNombre());
                                response.setAeropuertoLlegadaCodigoIata(a.getCodigoIata());
                            });

                    tipoSegmentoVueloRepository.findById(segmentoVuelo.getTipoSegmentoVueloId())
                            .ifPresent(t -> response.setTipoSegmentoVueloNombre(t.getNombre()));
                });

        avionRepository.findById(segmentoOperado.getAvionId())
                .ifPresent(a -> response.setCodigoAvion(a.getCodigoAvion()));

        tripulacionRepository.findById(segmentoOperado.getTripulacionId())
                .ifPresent(t -> response.setCodigoTripulacion(t.getCodigo()));

        estadoVueloRepository.findById(segmentoOperado.getEstadoVueloId())
                .ifPresent(e -> response.setEstadoVueloNombre(e.getNombre()));

        response.setPuedeCambiarEstado(true);

        return response;
    }

    private void mapVueloProgramado(
            VueloOperadoResponse response,
            VueloOperado vueloOperado
    ) {
        vueloProgramadoRepository.findById(vueloOperado.getVueloProgramadoId())
                .ifPresent(programado -> {
                    response.setVueloId(programado.getVueloId());
                    response.setAeropuertoSalidaId(programado.getAeropuertoSalidaId());
                    response.setAeropuertoLlegadaId(programado.getAeropuertoLlegadaId());
                    response.setPuertaEmbarqueSalida(programado.getPuertaEmbarqueSalida());
                    response.setPuertaEmbarqueLlegada(programado.getPuertaEmbarqueLlegada());
                    response.setFechaSalidaProgramada(programado.getFechaSalida());
                    response.setHoraSalidaProgramada(programado.getHoraSalida());
                    response.setFechaLlegadaProgramada(programado.getFechaLlegada());
                    response.setHoraLlegadaProgramada(programado.getHoraLlegada());

                    vueloRepository.findById(programado.getVueloId())
                            .ifPresent(vuelo -> {
                                response.setCodigoVuelo(vuelo.getCodigoVuelo());
                                response.setAerolineaId(vuelo.getAerolineaId());

                                aerolineaRepository.findById(vuelo.getAerolineaId())
                                        .ifPresent(a -> response.setAerolineaNombre(a.getNombre()));
                            });

                    aeropuertoRepository.findById(programado.getAeropuertoSalidaId())
                            .ifPresent(a -> {
                                response.setAeropuertoSalidaNombre(a.getNombre());
                                response.setAeropuertoSalidaCodigoIata(a.getCodigoIata());
                            });

                    aeropuertoRepository.findById(programado.getAeropuertoLlegadaId())
                            .ifPresent(a -> {
                                response.setAeropuertoLlegadaNombre(a.getNombre());
                                response.setAeropuertoLlegadaCodigoIata(a.getCodigoIata());
                            });
                });
    }

    private void mapTipoSegmento(
            VueloOperadoResponse response,
            VueloOperado vueloOperado
    ) {
        tipoSegmentoVueloRepository.findById(vueloOperado.getTipoSegmentoVueloId())
                .ifPresent(tipo -> {
                    response.setTipoSegmentoVueloNombre(tipo.getNombre());
                    response.setRequiereNuevoAsiento(tipo.getRequiereNuevoAsiento());
                    response.setPermiteEmbarque(tipo.getPermiteEmbarque());
                    response.setDetieneFlujoSiCancela(tipo.getDetieneFlujoSiCancela());
                });
    }

    private void mapEstadoVuelo(
            VueloOperadoResponse response,
            VueloOperado vueloOperado
    ) {
        estadoVueloRepository.findById(vueloOperado.getEstadoVueloId())
                .ifPresent(e -> response.setEstadoVueloNombre(e.getNombre()));
    }

    private void mapPermisos(
            VueloOperadoResponse response,
            VueloOperado vueloOperado
    ) {
        String estado = nombreEstadoVuelo(vueloOperado.getEstadoVueloId());

        response.setPuedeEditarDatos(
                estado.equals(PROGRAMADO) || estado.equals(CANCELADO)
        );

        response.setPuedeCancelar(
                estado.equals(PROGRAMADO) ||
                        estado.equals(RETRASADO) ||
                        estado.equals(ABORDANDO)
        );

        response.setPuedeFinalizar(
                estado.equals(ATERRIZADO) &&
                        vueloOperado.getSegmentoActualOrden().equals(vueloOperado.getCantidadSegmentos())
        );
    }

    private SegmentoOperado obtenerSegmentoActual(VueloOperado vueloOperado) {
        return obtenerSegmentoPorOrden(
                vueloOperado.getId(),
                vueloOperado.getSegmentoActualOrden()
        );
    }

    private SegmentoOperado obtenerSegmentoPorOrden(
            Integer vueloOperadoId,
            Integer orden
    ) {
        return segmentoOperadoRepository
                .findByVueloOperadoIdAndOrdenSegmento(vueloOperadoId, orden)
                .orElseThrow(() -> new BusinessException("Segmento operado no encontrado"));
    }

    private VueloOperado obtenerVueloOperado(Integer id) {
        return vueloOperadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Vuelo operado no encontrado"));
    }

    private VueloProgramado obtenerVueloProgramado(Integer id) {
        return vueloProgramadoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Vuelo programado no encontrado"));
    }

    private Vuelo obtenerVuelo(Integer id) {
        return vueloRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Vuelo no encontrado"));
    }

    private Avion obtenerAvion(Integer id) {
        return avionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Avión no encontrado"));
    }

    private Tripulacion obtenerTripulacion(Integer id) {
        return tripulacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tripulación no encontrada"));
    }

    private TipoSegmentoVuelo obtenerTipoSegmento(Integer id) {
        return tipoSegmentoVueloRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tipo de segmento de vuelo no encontrado"));
    }

    private Integer estadoVueloId(String nombre) {
        return estadoVueloRepository.findByNombreIgnoreCase(nombre)
                .map(EstadoVuelo::getId)
                .orElseThrow(() -> new BusinessException("Estado de vuelo no encontrado: " + nombre));
    }

    private String nombreEstadoVuelo(Integer id) {
        return estadoVueloRepository.findById(id)
                .map(EstadoVuelo::getNombre)
                .map(this::normalizar)
                .orElse("");
    }

    private Integer estadoAvionId(String nombre) {
        return estadoAvionRepository.findByNombreIgnoreCase(nombre)
                .map(EstadoAvion::getId)
                .orElseThrow(() -> new BusinessException("Estado de avión no encontrado: " + nombre));
    }

    private Integer estadoTripulacionDisponibleId() {
        return estadoTripulacionRepository.findByNombreIgnoreCase(DISPONIBLE)
                .map(EstadoTripulacion::getId)
                .orElseThrow(() -> new BusinessException("Estado de tripulación DISPONIBLE no encontrado"));
    }

    private Integer estadoTripulacionAsignadaId() {
        return estadoTripulacionRepository.findByNombreIgnoreCase(ASIGNADA)
                .map(EstadoTripulacion::getId)
                .orElseGet(() ->
                        estadoTripulacionRepository.findByNombreIgnoreCase(ASIGNADO)
                                .map(EstadoTripulacion::getId)
                                .orElseThrow(() -> new BusinessException("Estado de tripulación ASIGNADA no encontrado"))
                );
    }

    private Integer statusId(String nombre) {
        return statusCatalogRepository.findAll()
                .stream()
                .filter(s -> s.getName() != null)
                .filter(s -> normalizar(s.getName()).equals(normalizar(nombre)))
                .findFirst()
                .map(StatusCatalog::getId)
                .orElseThrow(() -> new BusinessException("Estado general no encontrado: " + nombre));
    }

    private String normalizar(String value) {
        if (value == null) return "";

        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase()
                .replace(" ", "_");
    }
}