package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.VueloRequest;
import com.aeropuertolosprimos.backend.dto.VueloResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.Aerolinea;
import com.aeropuertolosprimos.backend.model.Aeropuerto;
import com.aeropuertolosprimos.backend.model.StatusCatalog;
import com.aeropuertolosprimos.backend.model.Vuelo;
import com.aeropuertolosprimos.backend.model.VueloProgramado;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import com.aeropuertolosprimos.backend.repository.AeropuertoRepository;
import com.aeropuertolosprimos.backend.repository.DestinoAutorizadoRepository;
import com.aeropuertolosprimos.backend.repository.PuertaEmbarqueRepository;
import com.aeropuertolosprimos.backend.repository.StatusCatalogRepository;
import com.aeropuertolosprimos.backend.repository.VueloProgramadoRepository;
import com.aeropuertolosprimos.backend.repository.VueloRepository;
import com.aeropuertolosprimos.backend.specification.VueloProgramadoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VueloServiceImpl implements VueloService {

    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_INACTIVO = "INACTIVO";

    private final VueloRepository vueloRepository;
    private final VueloProgramadoRepository vueloProgramadoRepository;
    private final AerolineaRepository aerolineaRepository;
    private final AeropuertoRepository aeropuertoRepository;
    private final StatusCatalogRepository statusCatalogRepository;
    private final DestinoAutorizadoRepository destinoAutorizadoRepository;
    private final PuertaEmbarqueRepository puertaEmbarqueRepository;

    @Override
    public Page<VueloResponse> findAll(
            String q,
            String buscarSalida,
            String buscarLlegada,
            Integer aerolineaId,
            Integer aeropuertoSalidaId,
            Integer aeropuertoLlegadaId,
            LocalDate fechaSalida,
            LocalTime horaSalida,
            LocalDate fechaLlegada,
            LocalTime horaLlegada,
            Pageable pageable
    ) {

        Integer estadoActivoId = obtenerEstadoIdPorNombre(
                ESTADO_ACTIVO
        );

        return vueloProgramadoRepository
                .findAll(
                        VueloProgramadoSpecification.filters(
                                q,
                                buscarSalida,
                                buscarLlegada,
                                aerolineaId,
                                estadoActivoId,
                                aeropuertoSalidaId,
                                aeropuertoLlegadaId,
                                fechaSalida,
                                horaSalida,
                                fechaLlegada,
                                horaLlegada
                        ),
                        pageable
                )
                .map(this::mapResponse);
    }

    @Override
    public VueloResponse findById(
            Integer id
    ) {

        Vuelo vuelo = findVuelo(id);

        validarVueloActivo(vuelo);

        VueloProgramado programado = vueloProgramadoRepository
                .findByVueloId(vuelo.getId())
                .orElseThrow(() ->
                        new BusinessException("Programación del vuelo no encontrada")
                );

        return mapResponse(vuelo, programado);
    }

    @Override
    public VueloResponse findByCodigo(
            String codigoVuelo
    ) {

        String codigo = normalizarCodigo(codigoVuelo);

        if (codigo == null) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        Vuelo vuelo = vueloRepository
                .findByCodigoVueloIgnoreCase(codigo)
                .orElseThrow(() ->
                        new BusinessException("El número de vuelo ingresado no se encontró")
                );

        validarVueloActivo(vuelo);

        VueloProgramado programado = vueloProgramadoRepository
                .findByVueloId(vuelo.getId())
                .orElseThrow(() ->
                        new BusinessException("Programación del vuelo no encontrada")
                );

        return mapResponse(vuelo, programado);
    }

    @Override
    @Transactional
    public VueloResponse create(
            VueloRequest request
    ) {

        if (request == null) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        normalizarRequest(request);

        validarCamposObligatorios(request);
        validarReferenciasActivas(request);
        validarDestinosAutorizados(request);
        validarPuertasEmbarque(request);
        validarReglasProgramacion(request);
        validarProgramacionDuplicadaActiva(request, null);

        Vuelo vuelo = new Vuelo();

        vuelo.setAerolineaId(
                request.getAerolineaId()
        );

        vuelo.setCodigoVuelo(
                generarCodigoVuelo()
        );

        vuelo.setEstadoId(
                obtenerEstadoIdPorNombre(ESTADO_ACTIVO)
        );

        Vuelo vueloGuardado = vueloRepository.save(vuelo);

        VueloProgramado programado = new VueloProgramado();

        programado.setVueloId(
                vueloGuardado.getId()
        );

        programado.setAeropuertoSalidaId(
                request.getAeropuertoSalidaId()
        );

        programado.setAeropuertoLlegadaId(
                request.getAeropuertoLlegadaId()
        );

        programado.setPuertaEmbarqueSalida(
                request.getPuertaEmbarqueSalida()
        );

        programado.setPuertaEmbarqueLlegada(
                request.getPuertaEmbarqueLlegada()
        );

        programado.setFechaSalida(
                request.getFechaSalida()
        );

        programado.setHoraSalida(
                request.getHoraSalida()
        );

        programado.setFechaLlegada(
                request.getFechaLlegada()
        );

        programado.setHoraLlegada(
                request.getHoraLlegada()
        );

        VueloProgramado programadoGuardado =
                vueloProgramadoRepository.save(programado);

        return mapResponse(
                vueloGuardado,
                programadoGuardado
        );
    }

    @Override
    @Transactional
    public VueloResponse update(
            Integer id,
            VueloRequest request
    ) {

        if (request == null) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        normalizarRequest(request);

        validarCamposObligatorios(request);

        Vuelo vuelo = findVuelo(id);

        validarVueloActivo(vuelo);

        VueloProgramado programado = vueloProgramadoRepository
                .findByVueloId(vuelo.getId())
                .orElseThrow(() ->
                        new BusinessException("Programación del vuelo no encontrada")
                );

        validarReferenciasActivas(request);
        validarDestinosAutorizados(request);
        validarPuertasEmbarque(request);
        validarReglasProgramacion(request);
        validarProgramacionDuplicadaActiva(request, vuelo.getId());

        vuelo.setAerolineaId(
                request.getAerolineaId()
        );

        programado.setAeropuertoSalidaId(
                request.getAeropuertoSalidaId()
        );

        programado.setAeropuertoLlegadaId(
                request.getAeropuertoLlegadaId()
        );

        programado.setPuertaEmbarqueSalida(
                request.getPuertaEmbarqueSalida()
        );

        programado.setPuertaEmbarqueLlegada(
                request.getPuertaEmbarqueLlegada()
        );

        programado.setFechaSalida(
                request.getFechaSalida()
        );

        programado.setHoraSalida(
                request.getHoraSalida()
        );

        programado.setFechaLlegada(
                request.getFechaLlegada()
        );

        programado.setHoraLlegada(
                request.getHoraLlegada()
        );

        Vuelo vueloActualizado = vueloRepository.save(vuelo);

        VueloProgramado programadoActualizado =
                vueloProgramadoRepository.save(programado);

        return mapResponse(
                vueloActualizado,
                programadoActualizado
        );
    }

    @Override
    @Transactional
    public void delete(
            Integer id
    ) {

        Vuelo vuelo = findVuelo(id);

        vuelo.setEstadoId(
                obtenerEstadoIdPorNombre(ESTADO_INACTIVO)
        );

        vueloRepository.save(vuelo);
    }

    private void validarCamposObligatorios(
            VueloRequest request
    ) {

        if (request.getAerolineaId() == null ||
                request.getAeropuertoSalidaId() == null ||
                request.getAeropuertoLlegadaId() == null ||
                request.getPuertaEmbarqueSalida() == null ||
                request.getPuertaEmbarqueSalida().isBlank() ||
                request.getPuertaEmbarqueLlegada() == null ||
                request.getPuertaEmbarqueLlegada().isBlank() ||
                request.getFechaSalida() == null ||
                request.getHoraSalida() == null ||
                request.getFechaLlegada() == null ||
                request.getHoraLlegada() == null) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }
    }

    private void validarReferenciasActivas(
            VueloRequest request
    ) {

        Integer estadoActivoId = obtenerEstadoIdPorNombre(
                ESTADO_ACTIVO
        );

        Aerolinea aerolinea = aerolineaRepository
                .findById(request.getAerolineaId())
                .orElseThrow(() ->
                        new BusinessException("Aerolínea no encontrada")
                );

        if (aerolinea.getEstadoId() == null ||
                !aerolinea.getEstadoId().equals(estadoActivoId)) {

            throw new BusinessException("La aerolínea está inactiva");
        }

        Aeropuerto salida = aeropuertoRepository
                .findById(request.getAeropuertoSalidaId())
                .orElseThrow(() ->
                        new BusinessException("Aeropuerto de salida no encontrado")
                );

        if (salida.getEstadoId() == null ||
                !salida.getEstadoId().equals(estadoActivoId)) {

            throw new BusinessException("El aeropuerto de salida está inactivo");
        }

        Aeropuerto llegada = aeropuertoRepository
                .findById(request.getAeropuertoLlegadaId())
                .orElseThrow(() ->
                        new BusinessException("Aeropuerto de llegada no encontrado")
                );

        if (llegada.getEstadoId() == null ||
                !llegada.getEstadoId().equals(estadoActivoId)) {

            throw new BusinessException("El aeropuerto de llegada está inactivo");
        }
    }

    private void validarDestinosAutorizados(
            VueloRequest request
    ) {

        Integer estadoActivoId = obtenerEstadoIdPorNombre(
                ESTADO_ACTIVO
        );

        boolean salidaAutorizada = destinoAutorizadoRepository
                .existsByAerolineaIdAndAeropuertoIdAndEstadoId(
                        request.getAerolineaId(),
                        request.getAeropuertoSalidaId(),
                        estadoActivoId
                );

        boolean llegadaAutorizada = destinoAutorizadoRepository
                .existsByAerolineaIdAndAeropuertoIdAndEstadoId(
                        request.getAerolineaId(),
                        request.getAeropuertoLlegadaId(),
                        estadoActivoId
                );

        if (!salidaAutorizada || !llegadaAutorizada) {
            throw new BusinessException("No se encontraron aeropuertos autorizados para la aerolínea.");
        }
    }

    private void validarPuertasEmbarque(
            VueloRequest request
    ) {

        Integer estadoActivoId = obtenerEstadoIdPorNombre(
                ESTADO_ACTIVO
        );

        boolean puertaSalidaValida =
                puertaEmbarqueRepository
                        .existsByAeropuertoIdAndCodigoIgnoreCaseAndEstadoId(
                                request.getAeropuertoSalidaId(),
                                request.getPuertaEmbarqueSalida(),
                                estadoActivoId
                        );

        if (!puertaSalidaValida) {
            throw new BusinessException("La puerta de embarque de salida no pertenece al aeropuerto de salida o está inactiva.");
        }

        boolean puertaLlegadaValida =
                puertaEmbarqueRepository
                        .existsByAeropuertoIdAndCodigoIgnoreCaseAndEstadoId(
                                request.getAeropuertoLlegadaId(),
                                request.getPuertaEmbarqueLlegada(),
                                estadoActivoId
                        );

        if (!puertaLlegadaValida) {
            throw new BusinessException("La puerta de embarque de llegada no pertenece al aeropuerto de llegada o está inactiva.");
        }
    }

    private void validarReglasProgramacion(
            VueloRequest request
    ) {

        if (request.getAeropuertoSalidaId().equals(request.getAeropuertoLlegadaId())) {
            throw new BusinessException("No se puede seleccionar el mismo aeropuerto de salida y llegada.");
        }

        LocalDateTime salida = LocalDateTime.of(
                request.getFechaSalida(),
                request.getHoraSalida()
        );

        LocalDateTime llegada = LocalDateTime.of(
                request.getFechaLlegada(),
                request.getHoraLlegada()
        );

        if (!llegada.isAfter(salida)) {
            throw new BusinessException("La fecha y hora de llegada debe ser mayor a la fecha y hora de salida.");
        }

        LocalDateTime minimoPermitido = LocalDateTime.now()
                .plusHours(5);

        if (salida.isBefore(minimoPermitido)) {
            throw new BusinessException("Tiempo mínimo para la preparación 5 horas a partir de la hora actual.");
        }
    }

    private void validarProgramacionDuplicadaActiva(
            VueloRequest request,
            Integer vueloIdExcluir
    ) {

        Integer estadoActivoId = obtenerEstadoIdPorNombre(
                ESTADO_ACTIVO
        );

        long duplicados = vueloProgramadoRepository
                .countProgramacionActivaDuplicada(
                        request.getAerolineaId(),
                        request.getAeropuertoSalidaId(),
                        request.getAeropuertoLlegadaId(),
                        request.getFechaSalida(),
                        request.getHoraSalida(),
                        request.getFechaLlegada(),
                        request.getHoraLlegada(),
                        estadoActivoId,
                        vueloIdExcluir
                );

        if (duplicados > 0) {
            throw new BusinessException("Ya existe un vuelo activo con la misma programación.");
        }
    }

    private void validarVueloActivo(
            Vuelo vuelo
    ) {

        Integer estadoActivoId = obtenerEstadoIdPorNombre(
                ESTADO_ACTIVO
        );

        if (vuelo.getEstadoId() == null ||
                !vuelo.getEstadoId().equals(estadoActivoId)) {

            throw new BusinessException("Vuelo no encontrado o inactivo");
        }
    }

    private Vuelo findVuelo(
            Integer id
    ) {

        if (id == null) {
            throw new BusinessException("ID inválido");
        }

        return vueloRepository
                .findById(id)
                .orElseThrow(() ->
                        new BusinessException("Vuelo no encontrado")
                );
    }

    private Integer obtenerEstadoIdPorNombre(
            String nombre
    ) {

        return statusCatalogRepository
                .findByNameIgnoreCase(nombre)
                .map(StatusCatalog::getId)
                .orElseThrow(() ->
                        new BusinessException("No existe el estado " + nombre + " en status_catalog")
                );
    }

    private String generarCodigoVuelo() {

        long correlativo = vueloRepository.count() + 1;

        String codigo;

        do {
            codigo = "VUE-" + String.format("%05d", correlativo);
            correlativo++;
        } while (vueloRepository.existsByCodigoVueloIgnoreCase(codigo));

        return codigo;
    }

    private VueloResponse mapResponse(
            VueloProgramado programado
    ) {

        Vuelo vuelo = vueloRepository
                .findById(programado.getVueloId())
                .orElseThrow(() ->
                        new BusinessException("Vuelo no encontrado")
                );

        return mapResponse(
                vuelo,
                programado
        );
    }

    private VueloResponse mapResponse(
            Vuelo vuelo,
            VueloProgramado programado
    ) {

        VueloResponse response = new VueloResponse();

        response.setId(vuelo.getId());
        response.setVueloId(vuelo.getId());
        response.setVueloProgramadoId(programado.getId());

        response.setAerolineaId(vuelo.getAerolineaId());
        response.setCodigoVuelo(vuelo.getCodigoVuelo());
        response.setEstadoId(vuelo.getEstadoId());

        response.setAeropuertoSalidaId(programado.getAeropuertoSalidaId());
        response.setAeropuertoLlegadaId(programado.getAeropuertoLlegadaId());

        response.setPuertaEmbarqueSalida(programado.getPuertaEmbarqueSalida());
        response.setPuertaEmbarqueLlegada(programado.getPuertaEmbarqueLlegada());

        response.setFechaSalida(programado.getFechaSalida());
        response.setHoraSalida(programado.getHoraSalida());
        response.setFechaLlegada(programado.getFechaLlegada());
        response.setHoraLlegada(programado.getHoraLlegada());

        response.setCreatedAt(programado.getCreatedAt());
        response.setUpdatedAt(programado.getUpdatedAt());

        if (vuelo.getAerolineaId() != null) {
            aerolineaRepository
                    .findById(vuelo.getAerolineaId())
                    .ifPresent(a ->
                            response.setAerolineaNombre(a.getNombre())
                    );
        }

        if (vuelo.getEstadoId() != null) {
            statusCatalogRepository
                    .findById(vuelo.getEstadoId())
                    .ifPresent(e ->
                            response.setEstadoNombre(e.getName())
                    );
        }

        if (programado.getAeropuertoSalidaId() != null) {
            aeropuertoRepository
                    .findById(programado.getAeropuertoSalidaId())
                    .ifPresent(a -> {
                        response.setAeropuertoSalidaNombre(a.getNombre());
                        response.setAeropuertoSalidaCodigoIata(a.getCodigoIata());
                        response.setAeropuertoSalidaCodigoIcao(a.getCodigoIcao());
                    });
        }

        if (programado.getAeropuertoLlegadaId() != null) {
            aeropuertoRepository
                    .findById(programado.getAeropuertoLlegadaId())
                    .ifPresent(a -> {
                        response.setAeropuertoLlegadaNombre(a.getNombre());
                        response.setAeropuertoLlegadaCodigoIata(a.getCodigoIata());
                        response.setAeropuertoLlegadaCodigoIcao(a.getCodigoIcao());
                    });
        }

        return response;
    }

    private void normalizarRequest(
            VueloRequest request
    ) {

        request.setPuertaEmbarqueSalida(
                normalizarPuerta(request.getPuertaEmbarqueSalida())
        );

        request.setPuertaEmbarqueLlegada(
                normalizarPuerta(request.getPuertaEmbarqueLlegada())
        );
    }

    private String normalizarPuerta(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String limpio = value.trim();

        if (limpio.isBlank()) {
            return null;
        }

        return limpio.toUpperCase(Locale.ROOT);
    }

    private String normalizarCodigo(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String limpio = value.trim();

        return limpio.isBlank() ? null : limpio;
    }
}