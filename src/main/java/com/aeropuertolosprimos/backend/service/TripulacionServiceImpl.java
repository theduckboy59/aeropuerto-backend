package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TripulacionServiceImpl
        implements TripulacionService {

    private final TripulacionRepository tripulacionRepository;
    private final TripulacionDetalleRepository detalleRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DisponibilidadEmpleadoRepository disponibilidadRepository;

    public TripulacionServiceImpl(
            TripulacionRepository tripulacionRepository,
            TripulacionDetalleRepository detalleRepository,
            EmpleadoRepository empleadoRepository,
            DisponibilidadEmpleadoRepository disponibilidadRepository
    ) {
        this.tripulacionRepository = tripulacionRepository;
        this.detalleRepository = detalleRepository;
        this.empleadoRepository = empleadoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
    }

    @Override
    @Transactional
    public TripulacionResponse crear(
            TripulacionRequest request
    ) {

        if (request.getTripulantesCabinaIds() == null ||
                request.getTripulantesCabinaIds().size() != 3) {

            throw new RuntimeException(
                    "Debe ingresar 3 tripulantes de cabina"
            );
        }

        Set<Integer> empleados =
                new HashSet<>();

        empleados.add(
                request.getPilotoId()
        );

        empleados.add(
                request.getCopilotoId()
        );

        empleados.add(
                request.getIngenieroId()
        );

        empleados.addAll(
                request.getTripulantesCabinaIds()
        );

        if (empleados.size() != 6) {

            throw new RuntimeException(
                    "No puede repetir empleados"
            );
        }

        empleados.forEach(id ->
                validarEmpleado(
                        id,
                        request.getAerolineaId()
                ));

        Tripulacion tripulacion =
                new Tripulacion();

        tripulacion.setCodigo(
                generarCodigo()
        );

        tripulacion.setAerolineaId(
                request.getAerolineaId()
        );

        tripulacion.setEstadoTripulacionId(1);

        tripulacion =
                tripulacionRepository.save(
                        tripulacion
                );

        for (Integer empleadoId : empleados) {

            TripulacionDetalle detalle =
                    new TripulacionDetalle();

            detalle.setTripulacionId(
                    tripulacion.getId()
            );

            detalle.setEmpleadoId(
                    empleadoId
            );

            detalleRepository.save(
                    detalle
            );

            actualizarDisponibilidad(
                    empleadoId,
                    false
            );
        }

        return convertirResponse(
                tripulacion
        );
    }

    @Override
    public TripulacionResponse actualizarEstado(
            Integer id,
            Integer estadoTripulacionId
    ) {

        Tripulacion tripulacion =
                tripulacionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tripulación no encontrada"
                                ));

        tripulacion.setEstadoTripulacionId(
                estadoTripulacionId
        );

        tripulacion =
                tripulacionRepository.save(
                        tripulacion
                );

        boolean disponible =
                estadoTripulacionId == 1;

        detalleRepository.findByTripulacionId(
                        tripulacion.getId()
                )
                .forEach(detalle ->
                        actualizarDisponibilidad(
                                detalle.getEmpleadoId(),
                                disponible
                        ));

        return convertirResponse(
                tripulacion
        );
    }

    @Override
    public TripulacionResponse obtenerPorId(
            Integer id
    ) {

        Tripulacion tripulacion =
                tripulacionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Tripulación no encontrada"
                                ));

        return convertirResponse(
                tripulacion
        );
    }

    @Override
    public List<TripulacionResponse> listar() {

        return tripulacionRepository.findAll()
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public List<TripulacionResponse> listarPorAerolinea(
            Integer aerolineaId
    ) {

        return tripulacionRepository
                .findByAerolineaId(
                        aerolineaId
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public List<TripulacionResponse> listarDisponibles(
            Integer aerolineaId
    ) {

        return tripulacionRepository
                .findByAerolineaIdAndEstadoTripulacionId(
                        aerolineaId,
                        1
                )
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    private void validarEmpleado(
            Integer empleadoId,
            Integer aerolineaId
    ) {

        Empleado empleado =
                empleadoRepository.findById(
                        empleadoId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Empleado no encontrado"
                        ));

        if (!empleado.getEstadoId().equals(1)) {

            throw new RuntimeException(
                    "Empleado inactivo"
            );
        }

        if (!empleado.getAerolineaId()
                .equals(aerolineaId)) {

            throw new RuntimeException(
                    "Empleado pertenece a otra aerolínea"
            );
        }

        disponibilidadRepository
                .findByEmpleadoId(
                        empleadoId
                )
                .ifPresent(disponibilidad -> {

                    if (!disponibilidad.getDisponible()) {

                        throw new RuntimeException(
                                "Empleado no disponible"
                        );
                    }
                });
    }

    private void actualizarDisponibilidad(
            Integer empleadoId,
            Boolean disponible
    ) {

        DisponibilidadEmpleado disponibilidad =
                disponibilidadRepository
                        .findByEmpleadoId(
                                empleadoId
                        )
                        .orElse(
                                new DisponibilidadEmpleado()
                        );

        disponibilidad.setEmpleadoId(
                empleadoId
        );

        disponibilidad.setDisponible(
                disponible
        );

        disponibilidadRepository.save(
                disponibilidad
        );
    }

    private String generarCodigo() {

        long total =
                tripulacionRepository.count() + 1;

        return "TRIP-"
                + String.format("%03d", total);
    }

    private TripulacionResponse convertirResponse(
            Tripulacion tripulacion
    ) {

        TripulacionResponse response =
                new TripulacionResponse();

        response.setId(
                tripulacion.getId()
        );

        response.setCodigo(
                tripulacion.getCodigo()
        );

        response.setAerolineaId(
                tripulacion.getAerolineaId()
        );

        response.setEstadoTripulacionId(
                tripulacion.getEstadoTripulacionId()
        );

        List<TripulacionDetalleResponse> empleados =
                detalleRepository
                        .findByTripulacionId(
                                tripulacion.getId()
                        )
                        .stream()
                        .map(detalle -> {

                            Empleado empleado =
                                    empleadoRepository
                                            .findById(
                                                    detalle.getEmpleadoId()
                                            )
                                            .orElseThrow();

                            TripulacionDetalleResponse d =
                                    new TripulacionDetalleResponse();

                            d.setEmpleadoId(
                                    empleado.getId()
                            );

                            d.setCodigoEmpleado(
                                    empleado.getCodigoEmpleado()
                            );

                            d.setNombreCompleto(
                                    empleado.getNombreCompleto()
                            );

                            d.setTipoEmpleadoId(
                                    empleado.getTipoEmpleadoId()
                            );

                            return d;
                        })
                        .toList();

        response.setEmpleados(
                empleados
        );

        return response;
    }
}