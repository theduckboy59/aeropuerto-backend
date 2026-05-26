package com.aeropuertolosprimos.backend.service;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.TripulacionDetalleResponse;
import com.aeropuertolosprimos.backend.dto.TripulacionRequest;
import com.aeropuertolosprimos.backend.dto.TripulacionResponse;
import com.aeropuertolosprimos.backend.model.DisponibilidadEmpleado;
import com.aeropuertolosprimos.backend.model.Empleado;
import com.aeropuertolosprimos.backend.model.TipoEmpleado;
import com.aeropuertolosprimos.backend.model.Tripulacion;
import com.aeropuertolosprimos.backend.model.TripulacionDetalle;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import com.aeropuertolosprimos.backend.repository.DisponibilidadEmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.EmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.EstadoTripulacionRepository;
import com.aeropuertolosprimos.backend.repository.LicenciaRepository;
import com.aeropuertolosprimos.backend.repository.TipoEmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.TripulacionDetalleRepository;
import com.aeropuertolosprimos.backend.repository.TripulacionRepository;
import com.aeropuertolosprimos.backend.specification.TripulacionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TripulacionServiceImpl implements TripulacionService {

    private static final String ESTADO_TRIPULACION_DISPONIBLE_NOMBRE = "DISPONIBLE";

    private final TripulacionRepository tripulacionRepository;
    private final TripulacionDetalleRepository detalleRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DisponibilidadEmpleadoRepository disponibilidadRepository;
    private final TipoEmpleadoRepository tipoEmpleadoRepository;
    private final LicenciaRepository licenciaRepository;
    private final AerolineaRepository aerolineaRepository;
    private final EstadoTripulacionRepository estadoTripulacionRepository;

    private final CatalogoEstadoService catalogoEstadoService;

    @Override
    public Page<TripulacionResponse> findAll(
            String q,
            Integer aerolineaId,
            Integer estadoTripulacionId,
            Pageable pageable
    ) {
        return tripulacionRepository.findAll(
                        TripulacionSpecification.filters(
                                q,
                                aerolineaId,
                                estadoTripulacionId
                        ),
                        pageable
                )
                .map(this::convertirResponse);
    }

    @Override
    @Transactional
    public TripulacionResponse crear(TripulacionRequest request) {

        validarRequestCrear(request);

        Empleado piloto = validarEmpleadoBase(
                request.getPilotoId(),
                request.getAerolineaId()
        );

        Empleado copiloto = validarEmpleadoBase(
                request.getCopilotoId(),
                request.getAerolineaId()
        );

        Empleado ingeniero = validarEmpleadoBase(
                request.getIngenieroId(),
                request.getAerolineaId()
        );

        validarTipoEmpleado(
                piloto,
                "PILOTO",
                "El empleado seleccionado como piloto no tiene tipo PILOTO"
        );

        validarTipoEmpleado(
                copiloto,
                "COPILOTO",
                "El empleado seleccionado como copiloto no tiene tipo COPILOTO"
        );

        validarTipoEmpleado(
                ingeniero,
                "INGENIERO_VUELO",
                "El empleado seleccionado como ingeniero no tiene tipo INGENIERO_VUELO"
        );

        validarLicenciaPiloto(piloto);

        for (Integer cabinaId : request.getTripulantesCabinaIds()) {

            Empleado tripulanteCabina = validarEmpleadoBase(
                    cabinaId,
                    request.getAerolineaId()
            );

            validarTipoEmpleado(
                    tripulanteCabina,
                    "CABINA",
                    "Todos los tripulantes de cabina deben tener tipo CABINA"
            );
        }

        Tripulacion tripulacion = new Tripulacion();

        tripulacion.setCodigo(generarCodigo());
        tripulacion.setAerolineaId(request.getAerolineaId());
        tripulacion.setEstadoTripulacionId(obtenerEstadoTripulacionDisponibleId());

        tripulacion = tripulacionRepository.save(tripulacion);

        guardarDetalleYBloquearDisponibilidad(
                tripulacion.getId(),
                request.getPilotoId()
        );

        guardarDetalleYBloquearDisponibilidad(
                tripulacion.getId(),
                request.getCopilotoId()
        );

        guardarDetalleYBloquearDisponibilidad(
                tripulacion.getId(),
                request.getIngenieroId()
        );

        for (Integer cabinaId : request.getTripulantesCabinaIds()) {
            guardarDetalleYBloquearDisponibilidad(
                    tripulacion.getId(),
                    cabinaId
            );
        }

        return convertirResponse(tripulacion);
    }

    @Override
    public TripulacionResponse actualizarEstado(
            Integer id,
            Integer estadoTripulacionId
    ) {

        Tripulacion tripulacion = tripulacionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Tripulación no encontrada")
                );

        tripulacion.setEstadoTripulacionId(estadoTripulacionId);

        tripulacion = tripulacionRepository.save(tripulacion);

        boolean disponible =
                estadoTripulacionId.equals(obtenerEstadoTripulacionDisponibleId());

        detalleRepository.findByTripulacionId(tripulacion.getId())
                .forEach(detalle ->
                        actualizarDisponibilidad(
                                detalle.getEmpleadoId(),
                                disponible
                        )
                );

        return convertirResponse(tripulacion);
    }

    @Override
    public TripulacionResponse obtenerPorId(Integer id) {

        Tripulacion tripulacion = tripulacionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Tripulación no encontrada")
                );

        return convertirResponse(tripulacion);
    }

    @Override
    public List<TripulacionResponse> listar() {

        return tripulacionRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public List<TripulacionResponse> listarPorAerolinea(Integer aerolineaId) {

        return tripulacionRepository
                .findByAerolineaId(aerolineaId)
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public List<TripulacionResponse> listarDisponibles(Integer aerolineaId) {

        return tripulacionRepository
                .findByAerolineaIdAndEstadoTripulacionId(
                        aerolineaId,
                        obtenerEstadoTripulacionDisponibleId()
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    @Transactional
    public TripulacionResponse actualizar(
            Integer id,
            TripulacionRequest request
    ) {

        if (id == null) {
            throw new RuntimeException("ID inválido");
        }

        validarRequestCrear(request);

        Tripulacion tripulacion = tripulacionRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Tripulación no encontrada")
                );

        if (tripulacion.getEstadoTripulacionId() == null ||
                !tripulacion.getEstadoTripulacionId().equals(obtenerEstadoTripulacionDisponibleId())) {

            throw new RuntimeException("Solo se pueden editar tripulaciones disponibles");
        }

        Empleado piloto = validarEmpleadoBaseParaActualizar(
                request.getPilotoId(),
                request.getAerolineaId(),
                tripulacion.getId()
        );

        Empleado copiloto = validarEmpleadoBaseParaActualizar(
                request.getCopilotoId(),
                request.getAerolineaId(),
                tripulacion.getId()
        );

        Empleado ingeniero = validarEmpleadoBaseParaActualizar(
                request.getIngenieroId(),
                request.getAerolineaId(),
                tripulacion.getId()
        );

        validarTipoEmpleado(
                piloto,
                "PILOTO",
                "El empleado seleccionado como piloto no tiene tipo PILOTO"
        );

        validarTipoEmpleado(
                copiloto,
                "COPILOTO",
                "El empleado seleccionado como copiloto no tiene tipo COPILOTO"
        );

        validarTipoEmpleado(
                ingeniero,
                "INGENIERO_VUELO",
                "El empleado seleccionado como ingeniero no tiene tipo INGENIERO_VUELO"
        );

        validarLicenciaPiloto(piloto);

        for (Integer cabinaId : request.getTripulantesCabinaIds()) {

            Empleado tripulanteCabina = validarEmpleadoBaseParaActualizar(
                    cabinaId,
                    request.getAerolineaId(),
                    tripulacion.getId()
            );

            validarTipoEmpleado(
                    tripulanteCabina,
                    "CABINA",
                    "Todos los tripulantes de cabina deben tener tipo CABINA"
            );
        }

        List<TripulacionDetalle> detallesActuales = detalleRepository
                .findByTripulacionId(tripulacion.getId());

        detallesActuales.forEach(detalle ->
                actualizarDisponibilidad(
                        detalle.getEmpleadoId(),
                        true
                )
        );

        detalleRepository.deleteAll(detallesActuales);
        detalleRepository.flush();

        tripulacion.setAerolineaId(request.getAerolineaId());
        tripulacion = tripulacionRepository.save(tripulacion);

        guardarDetalleYBloquearDisponibilidad(
                tripulacion.getId(),
                request.getPilotoId()
        );

        guardarDetalleYBloquearDisponibilidad(
                tripulacion.getId(),
                request.getCopilotoId()
        );

        guardarDetalleYBloquearDisponibilidad(
                tripulacion.getId(),
                request.getIngenieroId()
        );

        for (Integer cabinaId : request.getTripulantesCabinaIds()) {
            guardarDetalleYBloquearDisponibilidad(
                    tripulacion.getId(),
                    cabinaId
            );
        }

        return convertirResponse(tripulacion);
    }

    private Integer obtenerEstadoTripulacionDisponibleId() {
        return estadoTripulacionRepository
                .findByNombreIgnoreCase(ESTADO_TRIPULACION_DISPONIBLE_NOMBRE)
                .orElseThrow(() ->
                        new RuntimeException("Estado de tripulación DISPONIBLE no encontrado")
                )
                .getId();
    }

    private Empleado validarEmpleadoBaseParaActualizar(
            Integer empleadoId,
            Integer aerolineaId,
            Integer tripulacionIdActual
    ) {

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() ->
                        new RuntimeException("Empleado no encontrado")
                );

        if (empleado.getEstadoId() == null ||
                !empleado.getEstadoId().equals(catalogoEstadoService.obtenerActivoId())) {

            throw new RuntimeException("Empleado inactivo");
        }

        if (empleado.getAerolineaId() == null ||
                !empleado.getAerolineaId().equals(aerolineaId)) {

            throw new RuntimeException("Empleado pertenece a otra aerolínea");
        }

        boolean yaPerteneceAEstaTripulacion = detalleRepository
                .findByTripulacionId(tripulacionIdActual)
                .stream()
                .anyMatch(detalle ->
                        detalle.getEmpleadoId().equals(empleadoId)
                );

        if (!yaPerteneceAEstaTripulacion) {
            disponibilidadRepository
                    .findByEmpleadoId(empleadoId)
                    .ifPresent(disponibilidad -> {

                        if (Boolean.FALSE.equals(disponibilidad.getDisponible())) {
                            throw new RuntimeException("Empleado no disponible");
                        }
                    });
        }

        return empleado;
    }

    private void validarRequestCrear(TripulacionRequest request) {

        if (request.getAerolineaId() == null) {
            throw new RuntimeException("Debe seleccionar una aerolínea");
        }

        if (request.getPilotoId() == null) {
            throw new RuntimeException("Debe seleccionar un piloto");
        }

        if (request.getCopilotoId() == null) {
            throw new RuntimeException("Debe seleccionar un copiloto");
        }

        if (request.getIngenieroId() == null) {
            throw new RuntimeException("Debe seleccionar un ingeniero");
        }

        if (request.getTripulantesCabinaIds() == null ||
                request.getTripulantesCabinaIds().size() != 3) {

            throw new RuntimeException("Debe ingresar 3 tripulantes de cabina");
        }

        Set<Integer> empleados = new HashSet<>();

        empleados.add(request.getPilotoId());
        empleados.add(request.getCopilotoId());
        empleados.add(request.getIngenieroId());
        empleados.addAll(request.getTripulantesCabinaIds());

        if (empleados.size() != 6) {
            throw new RuntimeException("No puede repetir empleados");
        }
    }

    private Empleado validarEmpleadoBase(
            Integer empleadoId,
            Integer aerolineaId
    ) {

        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() ->
                        new RuntimeException("Empleado no encontrado")
                );

        if (empleado.getEstadoId() == null ||
                !empleado.getEstadoId().equals(catalogoEstadoService.obtenerActivoId())) {

            throw new RuntimeException("Empleado inactivo");
        }

        if (empleado.getAerolineaId() == null ||
                !empleado.getAerolineaId().equals(aerolineaId)) {

            throw new RuntimeException("Empleado pertenece a otra aerolínea");
        }

        disponibilidadRepository
                .findByEmpleadoId(empleadoId)
                .ifPresent(disponibilidad -> {

                    if (Boolean.FALSE.equals(disponibilidad.getDisponible())) {
                        throw new RuntimeException("Empleado no disponible");
                    }
                });

        return empleado;
    }

    private void validarTipoEmpleado(
            Empleado empleado,
            String tipoEsperado,
            String mensajeError
    ) {

        if (empleado.getTipoEmpleadoId() == null) {
            throw new RuntimeException("Empleado sin tipo asignado");
        }

        TipoEmpleado tipoEmpleado = tipoEmpleadoRepository
                .findById(empleado.getTipoEmpleadoId())
                .orElseThrow(() ->
                        new RuntimeException("Tipo de empleado no encontrado")
                );

        String nombreTipo = normalizarTexto(tipoEmpleado.getNombre());
        String esperado = normalizarTexto(tipoEsperado);

        if (!nombreTipo.contains(esperado)) {
            throw new RuntimeException(mensajeError);
        }
    }

    private void validarLicenciaPiloto(Empleado piloto) {

        if (piloto.getLicenciaId() == null) {
            throw new RuntimeException("El piloto debe tener una licencia asignada");
        }

        licenciaRepository.findById(piloto.getLicenciaId())
                .orElseThrow(() ->
                        new RuntimeException("La licencia del piloto no existe")
                );

        if (piloto.getFechaVencimientoLicencia() == null) {
            throw new RuntimeException("El piloto debe tener fecha de vencimiento de licencia");
        }

        if (piloto.getFechaVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new RuntimeException("La licencia del piloto está vencida");
        }
    }

    private void guardarDetalleYBloquearDisponibilidad(
            Integer tripulacionId,
            Integer empleadoId
    ) {

        TripulacionDetalle detalle = new TripulacionDetalle();

        detalle.setTripulacionId(tripulacionId);
        detalle.setEmpleadoId(empleadoId);

        detalleRepository.save(detalle);

        actualizarDisponibilidad(empleadoId, false);
    }

    private void actualizarDisponibilidad(
            Integer empleadoId,
            Boolean disponible
    ) {

        DisponibilidadEmpleado disponibilidad = disponibilidadRepository
                .findByEmpleadoId(empleadoId)
                .orElse(new DisponibilidadEmpleado());

        disponibilidad.setEmpleadoId(empleadoId);
        disponibilidad.setDisponible(disponible);

        disponibilidadRepository.save(disponibilidad);
    }

    private String generarCodigo() {

        long total = tripulacionRepository.count() + 1;

        return "TRIP-" + String.format("%03d", total);
    }

    private TripulacionResponse convertirResponse(Tripulacion tripulacion) {

        TripulacionResponse response = new TripulacionResponse();

        response.setId(tripulacion.getId());
        response.setCodigo(tripulacion.getCodigo());
        response.setAerolineaId(tripulacion.getAerolineaId());
        response.setEstadoTripulacionId(tripulacion.getEstadoTripulacionId());

        if (tripulacion.getAerolineaId() != null) {
            aerolineaRepository.findById(tripulacion.getAerolineaId())
                    .ifPresent(aerolinea ->
                            response.setAerolineaNombre(aerolinea.getNombre())
                    );
        }

        if (tripulacion.getEstadoTripulacionId() != null) {
            estadoTripulacionRepository.findById(tripulacion.getEstadoTripulacionId())
                    .ifPresent(estado ->
                            response.setEstadoTripulacionNombre(estado.getNombre())
                    );
        }

        List<TripulacionDetalleResponse> empleados = detalleRepository
                .findByTripulacionId(tripulacion.getId())
                .stream()
                .map(detalle -> {

                    Empleado empleado = empleadoRepository
                            .findById(detalle.getEmpleadoId())
                            .orElseThrow(() ->
                                    new RuntimeException("Empleado no encontrado")
                            );

                    TripulacionDetalleResponse d =
                            new TripulacionDetalleResponse();

                    d.setEmpleadoId(empleado.getId());
                    d.setCodigoEmpleado(empleado.getCodigoEmpleado());
                    d.setNombreCompleto(empleado.getNombreCompleto());
                    d.setTipoEmpleadoId(empleado.getTipoEmpleadoId());
                    d.setLicenciaId(empleado.getLicenciaId());
                    d.setFechaVencimientoLicencia(
                            empleado.getFechaVencimientoLicencia()
                    );

                    if (empleado.getTipoEmpleadoId() != null) {
                        tipoEmpleadoRepository
                                .findById(empleado.getTipoEmpleadoId())
                                .ifPresent(tipoEmpleado ->
                                        d.setTipoEmpleadoNombre(
                                                tipoEmpleado.getNombre()
                                        )
                                );
                    }

                    if (empleado.getLicenciaId() != null) {
                        licenciaRepository
                                .findById(empleado.getLicenciaId())
                                .ifPresent(licencia ->
                                        d.setLicenciaNombre(
                                                licencia.getNombre()
                                        )
                                );
                    }

                    return d;
                })
                .toList();

        response.setEmpleados(empleados);

        return response;
    }

    private String normalizarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");
    }
}