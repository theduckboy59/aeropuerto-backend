package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.VueloOperadoRequest;
import com.aeropuertolosprimos.backend.dto.VueloOperadoResponse;
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

@Service
@RequiredArgsConstructor
public class VueloOperadoServiceImpl implements VueloOperadoService {

    private static final String STATUS_ACTIVO_NOMBRE = "ACTIVO";
    private static final String STATUS_INACTIVO_NOMBRE = "INACTIVO";

    private static final String ESTADO_AVION_DISPONIBLE_NOMBRE = "DISPONIBLE";
    private static final String ESTADO_AVION_ASIGNADO_NOMBRE = "ASIGNADO";

    private static final String ESTADO_TRIPULACION_DISPONIBLE_NOMBRE = "DISPONIBLE";
    private static final String ESTADO_TRIPULACION_ASIGNADA_NOMBRE = "ASIGNADA";
    private static final String ESTADO_TRIPULACION_ASIGNADO_NOMBRE = "ASIGNADO";

    private static final String ESTADO_VUELO_PROGRAMADO_NOMBRE = "PROGRAMADO";
    private static final String ESTADO_VUELO_ABORDANDO_NOMBRE = "ABORDANDO";
    private static final String ESTADO_VUELO_EN_VUELO_NOMBRE = "EN_VUELO";
    private static final String ESTADO_VUELO_ATERRIZADO_NOMBRE = "ATERRIZADO";
    private static final String ESTADO_VUELO_RETRASADO_NOMBRE = "RETRASADO";
    private static final String ESTADO_VUELO_CANCELADO_NOMBRE = "CANCELADO";

    private final VueloOperadoRepository vueloOperadoRepository;
    private final VueloProgramadoRepository vueloProgramadoRepository;
    private final VueloRepository vueloRepository;
    private final AvionRepository avionRepository;
    private final TripulacionRepository tripulacionRepository;
    private final EstadoVueloRepository estadoVueloRepository;
    private final AerolineaRepository aerolineaRepository;
    private final AeropuertoRepository aeropuertoRepository;

    private final EstadoAvionRepository estadoAvionRepository;
    private final EstadoTripulacionRepository estadoTripulacionRepository;
    private final StatusCatalogRepository statusCatalogRepository;

    @Override
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
    public VueloOperadoResponse findById(Integer id) {

        VueloOperado vueloOperado = findVueloOperado(id);

        return mapResponse(vueloOperado);
    }

    @Override
    @Transactional
    public VueloOperadoResponse create(VueloOperadoRequest request) {

        validarRequestCrear(request);

        VueloProgramado programado = vueloProgramadoRepository
                .findById(request.getVueloProgramadoId())
                .orElseThrow(() ->
                        new BusinessException("Vuelo programado no encontrado")
                );

        Vuelo vuelo = vueloRepository
                .findById(programado.getVueloId())
                .orElseThrow(() ->
                        new BusinessException("Vuelo no encontrado")
                );

        validarVueloActivo(vuelo);

        if (vueloOperadoRepository.existsByVueloProgramadoId(programado.getId())) {
            throw new BusinessException("El vuelo programado ya tiene una operación registrada");
        }

        Avion avion = avionRepository
                .findById(request.getAvionId())
                .orElseThrow(() ->
                        new BusinessException("Avión no encontrado")
                );

        validarAvionDisponible(avion, vuelo);

        Tripulacion tripulacion = tripulacionRepository
                .findById(request.getTripulacionId())
                .orElseThrow(() ->
                        new BusinessException("Tripulación no encontrada")
                );

        validarTripulacionDisponible(tripulacion, vuelo);

        VueloOperado vueloOperado = new VueloOperado();

        vueloOperado.setVueloProgramadoId(programado.getId());
        vueloOperado.setAvionId(avion.getId());
        vueloOperado.setTripulacionId(tripulacion.getId());
        vueloOperado.setEstadoVueloId(estadoVueloId(ESTADO_VUELO_PROGRAMADO_NOMBRE));

        vueloOperado.setFechaSalidaReal(null);
        vueloOperado.setHoraSalidaReal(null);
        vueloOperado.setFechaLlegadaReal(null);
        vueloOperado.setHoraLlegadaReal(null);

        VueloOperado guardado = vueloOperadoRepository.save(vueloOperado);

        avion.setEstadoAvionId(estadoAvionId(ESTADO_AVION_ASIGNADO_NOMBRE));
        avionRepository.save(avion);

        tripulacion.setEstadoTripulacionId(
                estadoTripulacionId(
                        ESTADO_TRIPULACION_ASIGNADA_NOMBRE,
                        ESTADO_TRIPULACION_ASIGNADO_NOMBRE
                )
        );
        tripulacionRepository.save(tripulacion);

        return mapResponse(guardado);
    }

    @Override
    @Transactional
    public VueloOperadoResponse cambiarEstado(
            Integer id,
            Integer estadoVueloId
    ) {

        if (estadoVueloId == null) {
            throw new BusinessException("Debe ingresar el estado del vuelo");
        }

        VueloOperado vueloOperado = findVueloOperado(id);

        estadoVueloRepository
                .findById(estadoVueloId)
                .orElseThrow(() ->
                        new BusinessException("Estado de vuelo no encontrado")
                );

        validarTransicion(
                vueloOperado.getEstadoVueloId(),
                estadoVueloId
        );

        Integer estadoAbordandoId = estadoVueloId(ESTADO_VUELO_ABORDANDO_NOMBRE);
        Integer estadoRetrasadoId = estadoVueloId(ESTADO_VUELO_RETRASADO_NOMBRE);
        Integer estadoEnVueloId = estadoVueloId(ESTADO_VUELO_EN_VUELO_NOMBRE);
        Integer estadoAterrizadoId = estadoVueloId(ESTADO_VUELO_ATERRIZADO_NOMBRE);
        Integer estadoCanceladoId = estadoVueloId(ESTADO_VUELO_CANCELADO_NOMBRE);

        if (estadoVueloId.equals(estadoAbordandoId)) {
            vueloOperado.setEstadoVueloId(estadoAbordandoId);
        }

        if (estadoVueloId.equals(estadoRetrasadoId)) {
            vueloOperado.setEstadoVueloId(estadoRetrasadoId);
        }

        if (estadoVueloId.equals(estadoEnVueloId)) {

            vueloOperado.setEstadoVueloId(estadoEnVueloId);

            if (vueloOperado.getFechaSalidaReal() == null) {
                vueloOperado.setFechaSalidaReal(LocalDate.now());
            }

            if (vueloOperado.getHoraSalidaReal() == null) {
                vueloOperado.setHoraSalidaReal(LocalTime.now());
            }
        }

        if (estadoVueloId.equals(estadoAterrizadoId)) {

            vueloOperado.setEstadoVueloId(estadoAterrizadoId);

            if (vueloOperado.getFechaLlegadaReal() == null) {
                vueloOperado.setFechaLlegadaReal(LocalDate.now());
            }

            if (vueloOperado.getHoraLlegadaReal() == null) {
                vueloOperado.setHoraLlegadaReal(LocalTime.now());
            }

            cerrarOperacion(vueloOperado);
        }

        if (estadoVueloId.equals(estadoCanceladoId)) {

            vueloOperado.setEstadoVueloId(estadoCanceladoId);

            cerrarOperacion(vueloOperado);
        }

        VueloOperado actualizado = vueloOperadoRepository.save(vueloOperado);

        return mapResponse(actualizado);
    }

    @Override
    @Transactional
    public void delete(Integer id) {

        cambiarEstado(
                id,
                estadoVueloId(ESTADO_VUELO_CANCELADO_NOMBRE)
        );
    }

    private void cerrarOperacion(VueloOperado vueloOperado) {

        VueloProgramado programado = vueloProgramadoRepository
                .findById(vueloOperado.getVueloProgramadoId())
                .orElseThrow(() ->
                        new BusinessException("Vuelo programado no encontrado")
                );

        Vuelo vuelo = vueloRepository
                .findById(programado.getVueloId())
                .orElseThrow(() ->
                        new BusinessException("Vuelo no encontrado")
                );

        vuelo.setEstadoId(statusId(STATUS_INACTIVO_NOMBRE));
        vueloRepository.save(vuelo);

        Avion avion = avionRepository
                .findById(vueloOperado.getAvionId())
                .orElseThrow(() ->
                        new BusinessException("Avión no encontrado")
                );

        avion.setEstadoAvionId(estadoAvionId(ESTADO_AVION_DISPONIBLE_NOMBRE));
        avionRepository.save(avion);

        Tripulacion tripulacion = tripulacionRepository
                .findById(vueloOperado.getTripulacionId())
                .orElseThrow(() ->
                        new BusinessException("Tripulación no encontrada")
                );

        tripulacion.setEstadoTripulacionId(estadoTripulacionId(ESTADO_TRIPULACION_DISPONIBLE_NOMBRE));
        tripulacionRepository.save(tripulacion);
    }

    private void validarRequestCrear(VueloOperadoRequest request) {

        if (request == null ||
                request.getVueloProgramadoId() == null ||
                request.getAvionId() == null ||
                request.getTripulacionId() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }
    }

    private void validarVueloActivo(Vuelo vuelo) {

        if (vuelo.getEstadoId() == null ||
                !vuelo.getEstadoId().equals(statusId(STATUS_ACTIVO_NOMBRE))) {

            throw new BusinessException("El vuelo programado no está activo");
        }
    }

    private void validarAvionDisponible(
            Avion avion,
            Vuelo vuelo
    ) {

        if (avion.getEstadoId() == null ||
                !avion.getEstadoId().equals(statusId(STATUS_ACTIVO_NOMBRE))) {

            throw new BusinessException("El avión está inactivo");
        }

        if (avion.getEstadoAvionId() == null ||
                !avion.getEstadoAvionId().equals(estadoAvionId(ESTADO_AVION_DISPONIBLE_NOMBRE))) {

            throw new BusinessException("El avión no está disponible");
        }

        if (avion.getAerolineaId() == null ||
                !avion.getAerolineaId().equals(vuelo.getAerolineaId())) {

            throw new BusinessException("El avión no pertenece a la aerolínea del vuelo");
        }
    }

    private void validarTripulacionDisponible(
            Tripulacion tripulacion,
            Vuelo vuelo
    ) {

        if (tripulacion.getEstadoTripulacionId() == null ||
                !tripulacion.getEstadoTripulacionId().equals(estadoTripulacionId(ESTADO_TRIPULACION_DISPONIBLE_NOMBRE))) {

            throw new BusinessException("La tripulación no está disponible");
        }

        if (tripulacion.getAerolineaId() == null ||
                !tripulacion.getAerolineaId().equals(vuelo.getAerolineaId())) {

            throw new BusinessException("La tripulación no pertenece a la aerolínea del vuelo");
        }
    }

    private void validarTransicion(
            Integer estadoActual,
            Integer estadoNuevo
    ) {

        if (estadoActual == null) {
            throw new BusinessException("El vuelo operado no tiene estado actual");
        }

        Integer estadoProgramadoId = estadoVueloId(ESTADO_VUELO_PROGRAMADO_NOMBRE);
        Integer estadoAbordandoId = estadoVueloId(ESTADO_VUELO_ABORDANDO_NOMBRE);
        Integer estadoEnVueloId = estadoVueloId(ESTADO_VUELO_EN_VUELO_NOMBRE);
        Integer estadoAterrizadoId = estadoVueloId(ESTADO_VUELO_ATERRIZADO_NOMBRE);
        Integer estadoRetrasadoId = estadoVueloId(ESTADO_VUELO_RETRASADO_NOMBRE);
        Integer estadoCanceladoId = estadoVueloId(ESTADO_VUELO_CANCELADO_NOMBRE);

        if (estadoActual.equals(estadoCanceladoId) ||
                estadoActual.equals(estadoAterrizadoId)) {

            throw new BusinessException("No se puede cambiar el estado de un vuelo cerrado");
        }

        if (estadoActual.equals(estadoProgramadoId)) {

            if (estadoNuevo.equals(estadoAbordandoId) ||
                    estadoNuevo.equals(estadoRetrasadoId) ||
                    estadoNuevo.equals(estadoCanceladoId)) {

                return;
            }
        }

        if (estadoActual.equals(estadoRetrasadoId)) {

            if (estadoNuevo.equals(estadoAbordandoId) ||
                    estadoNuevo.equals(estadoCanceladoId)) {

                return;
            }
        }

        if (estadoActual.equals(estadoAbordandoId)) {

            if (estadoNuevo.equals(estadoEnVueloId) ||
                    estadoNuevo.equals(estadoRetrasadoId) ||
                    estadoNuevo.equals(estadoCanceladoId)) {

                return;
            }
        }

        if (estadoActual.equals(estadoEnVueloId)) {

            if (estadoNuevo.equals(estadoAterrizadoId)) {
                return;
            }
        }

        throw new BusinessException("Transición de estado no permitida");
    }

    private Integer statusId(String nombre) {

        return statusCatalogRepository
                .findAll()
                .stream()
                .filter(e -> normalizarTexto(e.getName()).equals(normalizarTexto(nombre)))
                .map(StatusCatalog::getId)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("Status no encontrado: " + nombre)
                );
    }

    private Integer estadoAvionId(String nombre) {

        return estadoAvionRepository
                .findAll()
                .stream()
                .filter(e -> normalizarTexto(e.getNombre()).equals(normalizarTexto(nombre)))
                .map(EstadoAvion::getId)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("Estado de avión no encontrado: " + nombre)
                );
    }

    private Integer estadoTripulacionId(String... nombres) {

        for (String nombre : nombres) {

            Integer id = estadoTripulacionRepository
                    .findAll()
                    .stream()
                    .filter(e -> normalizarTexto(e.getNombre()).equals(normalizarTexto(nombre)))
                    .map(EstadoTripulacion::getId)
                    .findFirst()
                    .orElse(null);

            if (id != null) {
                return id;
            }
        }

        throw new BusinessException("Estado de tripulación no encontrado");
    }

    private Integer estadoVueloId(String nombre) {

        return estadoVueloRepository
                .findAll()
                .stream()
                .filter(e -> normalizarTexto(e.getNombre()).equals(normalizarTexto(nombre)))
                .map(EstadoVuelo::getId)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("Estado de vuelo no encontrado: " + nombre)
                );
    }

    private String normalizarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        String normalizado = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalizado
                .trim()
                .toUpperCase()
                .replace("-", "_")
                .replace(" ", "_")
                .replaceAll("_+", "_");
    }

    private VueloOperado findVueloOperado(Integer id) {

        if (id == null) {
            throw new BusinessException("ID inválido");
        }

        return vueloOperadoRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException("Vuelo operado no encontrado")
                );
    }

    private VueloOperadoResponse mapResponse(VueloOperado vueloOperado) {

        VueloProgramado programado = vueloProgramadoRepository
                .findById(vueloOperado.getVueloProgramadoId())
                .orElse(null);

        Vuelo vuelo = null;

        if (programado != null && programado.getVueloId() != null) {
            vuelo = vueloRepository
                    .findById(programado.getVueloId())
                    .orElse(null);
        }

        VueloOperadoResponse response = new VueloOperadoResponse();

        response.setId(vueloOperado.getId());
        response.setVueloProgramadoId(vueloOperado.getVueloProgramadoId());

        response.setAvionId(vueloOperado.getAvionId());
        response.setTripulacionId(vueloOperado.getTripulacionId());
        response.setEstadoVueloId(vueloOperado.getEstadoVueloId());

        response.setFechaSalidaReal(vueloOperado.getFechaSalidaReal());
        response.setHoraSalidaReal(vueloOperado.getHoraSalidaReal());
        response.setFechaLlegadaReal(vueloOperado.getFechaLlegadaReal());
        response.setHoraLlegadaReal(vueloOperado.getHoraLlegadaReal());

        response.setCreatedAt(vueloOperado.getCreatedAt());
        response.setUpdatedAt(vueloOperado.getUpdatedAt());

        if (programado != null) {

            response.setVueloId(programado.getVueloId());

            response.setAeropuertoSalidaId(programado.getAeropuertoSalidaId());
            response.setAeropuertoLlegadaId(programado.getAeropuertoLlegadaId());

            response.setPuertaEmbarqueSalida(programado.getPuertaEmbarqueSalida());
            response.setPuertaEmbarqueLlegada(programado.getPuertaEmbarqueLlegada());

            response.setFechaSalidaProgramada(programado.getFechaSalida());
            response.setHoraSalidaProgramada(programado.getHoraSalida());
            response.setFechaLlegadaProgramada(programado.getFechaLlegada());
            response.setHoraLlegadaProgramada(programado.getHoraLlegada());

            if (programado.getAeropuertoSalidaId() != null) {
                aeropuertoRepository
                        .findById(programado.getAeropuertoSalidaId())
                        .ifPresent(a -> {
                            response.setAeropuertoSalidaNombre(a.getNombre());
                            response.setAeropuertoSalidaCodigoIata(a.getCodigoIata());
                        });
            }

            if (programado.getAeropuertoLlegadaId() != null) {
                aeropuertoRepository
                        .findById(programado.getAeropuertoLlegadaId())
                        .ifPresent(a -> {
                            response.setAeropuertoLlegadaNombre(a.getNombre());
                            response.setAeropuertoLlegadaCodigoIata(a.getCodigoIata());
                        });
            }
        }

        if (vuelo != null) {

            response.setCodigoVuelo(vuelo.getCodigoVuelo());
            response.setAerolineaId(vuelo.getAerolineaId());

            if (vuelo.getAerolineaId() != null) {
                aerolineaRepository
                        .findById(vuelo.getAerolineaId())
                        .ifPresent(a ->
                                response.setAerolineaNombre(a.getNombre())
                        );
            }
        }

        if (vueloOperado.getAvionId() != null) {
            avionRepository
                    .findById(vueloOperado.getAvionId())
                    .ifPresent(a ->
                            response.setCodigoAvion(a.getCodigoAvion())
                    );
        }

        if (vueloOperado.getTripulacionId() != null) {
            tripulacionRepository
                    .findById(vueloOperado.getTripulacionId())
                    .ifPresent(t ->
                            response.setCodigoTripulacion(t.getCodigo())
                    );
        }

        if (vueloOperado.getEstadoVueloId() != null) {
            estadoVueloRepository
                    .findById(vueloOperado.getEstadoVueloId())
                    .ifPresent(e ->
                            response.setEstadoVueloNombre(e.getNombre())
                    );
        }

        return response;
    }
}