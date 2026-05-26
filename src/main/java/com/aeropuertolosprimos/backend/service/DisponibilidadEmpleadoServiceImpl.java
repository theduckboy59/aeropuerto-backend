package com.aeropuertolosprimos.backend.service;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.*;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadEmpleadoServiceImpl
        implements DisponibilidadEmpleadoService {

    private final DisponibilidadEmpleadoRepository repository;
    private final EmpleadoRepository empleadoRepository;


    @Override
    public DisponibilidadEmpleadoResponse crear(
            DisponibilidadEmpleadoRequest request
    ) {

        Empleado empleado =
                empleadoRepository.findById(
                        request.getEmpleadoId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Empleado no encontrado"
                        ));

        DisponibilidadEmpleado disponibilidad =
                new DisponibilidadEmpleado();

        disponibilidad.setEmpleadoId(
                empleado.getId()
        );

        disponibilidad.setFecha(
                request.getFecha()
        );

        disponibilidad.setHoraInicio(
                request.getHoraInicio()
        );

        disponibilidad.setHoraFin(
                request.getHoraFin()
        );

        disponibilidad.setDisponible(
                request.getDisponible()
        );

        disponibilidad = repository.save(
                disponibilidad
        );

        return convertirResponse(
                disponibilidad,
                empleado
        );
    }

    @Override
    public DisponibilidadEmpleadoResponse actualizar(
            Integer id,
            DisponibilidadEmpleadoRequest request
    ) {

        DisponibilidadEmpleado disponibilidad =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Disponibilidad no encontrada"
                                ));

        Empleado empleado =
                empleadoRepository.findById(
                        disponibilidad.getEmpleadoId()
                ).orElseThrow();

        disponibilidad.setFecha(
                request.getFecha()
        );

        disponibilidad.setHoraInicio(
                request.getHoraInicio()
        );

        disponibilidad.setHoraFin(
                request.getHoraFin()
        );

        disponibilidad.setDisponible(
                request.getDisponible()
        );

        disponibilidad = repository.save(
                disponibilidad
        );

        return convertirResponse(
                disponibilidad,
                empleado
        );
    }

    @Override
    public DisponibilidadEmpleadoResponse obtenerPorEmpleado(
            Integer empleadoId
    ) {

        DisponibilidadEmpleado disponibilidad =
                repository.findByEmpleadoId(
                        empleadoId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Disponibilidad no encontrada"
                        ));

        Empleado empleado =
                empleadoRepository.findById(
                        empleadoId
                ).orElseThrow();

        return convertirResponse(
                disponibilidad,
                empleado
        );
    }

    @Override
    public List<DisponibilidadEmpleadoResponse> listar() {

        return repository.findAll()
                .stream()
                .map(disponibilidad -> {

                    Empleado empleado =
                            empleadoRepository.findById(
                                    disponibilidad.getEmpleadoId()
                            ).orElseThrow();

                    return convertirResponse(
                            disponibilidad,
                            empleado
                    );
                })
                .toList();
    }

    private DisponibilidadEmpleadoResponse convertirResponse(
            DisponibilidadEmpleado disponibilidad,
            Empleado empleado
    ) {

        DisponibilidadEmpleadoResponse response =
                new DisponibilidadEmpleadoResponse();

        response.setId(
                disponibilidad.getId()
        );

        response.setEmpleadoId(
                empleado.getId()
        );

        response.setCodigoEmpleado(
                empleado.getCodigoEmpleado()
        );

        response.setNombreCompleto(
                empleado.getNombreCompleto()
        );

        response.setFecha(
                disponibilidad.getFecha()
        );

        response.setHoraInicio(
                disponibilidad.getHoraInicio()
        );

        response.setHoraFin(
                disponibilidad.getHoraFin()
        );

        response.setDisponible(
                disponibilidad.getDisponible()
        );

        return response;
    }
}