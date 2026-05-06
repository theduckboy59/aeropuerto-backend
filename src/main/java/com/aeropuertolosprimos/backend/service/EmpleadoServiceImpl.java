package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.dto.EmpleadoResponse;
import com.aeropuertolosprimos.backend.model.Empleado;
import com.aeropuertolosprimos.backend.model.TipoEmpleado;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.EmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.TipoEmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UserRepository userRepository;
    private final TipoEmpleadoRepository tipoEmpleadoRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public EmpleadoServiceImpl(
            EmpleadoRepository empleadoRepository,
            UserRepository userRepository,
            TipoEmpleadoRepository tipoEmpleadoRepository
    ) {
        this.empleadoRepository = empleadoRepository;
        this.userRepository = userRepository;
        this.tipoEmpleadoRepository = tipoEmpleadoRepository;
    }

    @Override
    public EmpleadoResponse crear(EmpleadoRequest request) {

        validarRequest(request, true);

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("Email ya registrado");
                });

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new RuntimeException("Username ya registrado");
                });

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        user = userRepository.save(user);

        Empleado empleado = new Empleado();

        empleado.setUserId(user.getId());

        empleado.setTipoEmpleadoId(request.getTipoEmpleadoId());

        empleado.setCodigoEmpleado(
                generarCodigoEmpleado(request.getTipoEmpleadoId())
        );

        empleado.setAerolineaId(request.getAerolineaId());

        empleado.setNombreCompleto(request.getNombreCompleto());

        empleado.setFechaIngreso(request.getFechaIngreso());
        empleado.setFechaSalida(request.getFechaSalida());

        empleado.setTurnoId(request.getTurnoId());
        empleado.setNivelAccesoId(request.getNivelAccesoId());
        empleado.setRolId(request.getRolId());
        empleado.setAreaId(request.getAreaId());

        empleado.setLicenciaId(request.getLicenciaId());

        empleado.setFechaVencimientoLicencia(
                request.getFechaVencimientoLicencia()
        );

        empleado = empleadoRepository.save(empleado);

        return convertirResponse(empleado, user);
    }

    @Override
    public List<EmpleadoResponse> listar(
            Integer tipoEmpleadoId,
            Integer aerolineaId,
            LocalDate fechaIngreso,
            LocalDate fechaSalida,
            Integer turnoId,
            Integer rolId,
            Integer nivelAccesoId,
            Integer areaId
    ) {

        Specification<Empleado> spec = (root, query, cb) ->
                cb.equal(root.get("estadoId"), 1);

        if (tipoEmpleadoId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("tipoEmpleadoId"), tipoEmpleadoId));
        }

        if (aerolineaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("aerolineaId"), aerolineaId));
        }

        if (fechaIngreso != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("fechaIngreso"), fechaIngreso));
        }

        if (fechaSalida != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("fechaSalida"), fechaSalida));
        }

        if (turnoId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("turnoId"), turnoId));
        }

        if (rolId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("rolId"), rolId));
        }

        if (nivelAccesoId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("nivelAccesoId"), nivelAccesoId));
        }

        if (areaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("areaId"), areaId));
        }

        return empleadoRepository.findAll(spec)
                .stream()
                .map(this::convertirResponse)
                .toList();
    }

    @Override
    public EmpleadoResponse obtenerPorId(Integer id) {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Empleado no encontrado"));

        return convertirResponse(empleado);
    }

    @Override
    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest request) {

        validarRequest(request, false);

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Empleado no encontrado"));

        User user = userRepository.findById(empleado.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    encoder.encode(request.getPassword())
            );
        }

        userRepository.save(user);

        if (!empleado.getTipoEmpleadoId()
                .equals(request.getTipoEmpleadoId())) {

            empleado.setTipoEmpleadoId(
                    request.getTipoEmpleadoId()
            );

            empleado.setCodigoEmpleado(
                    generarCodigoEmpleado(
                            request.getTipoEmpleadoId()
                    )
            );
        }

        empleado.setAerolineaId(request.getAerolineaId());

        empleado.setNombreCompleto(request.getNombreCompleto());

        empleado.setFechaIngreso(request.getFechaIngreso());

        empleado.setFechaSalida(request.getFechaSalida());

        empleado.setTurnoId(request.getTurnoId());

        empleado.setNivelAccesoId(request.getNivelAccesoId());

        empleado.setRolId(request.getRolId());

        empleado.setAreaId(request.getAreaId());

        empleado.setLicenciaId(request.getLicenciaId());

        empleado.setFechaVencimientoLicencia(
                request.getFechaVencimientoLicencia()
        );

        empleado = empleadoRepository.save(empleado);

        return convertirResponse(empleado, user);
    }

    @Override
    public void eliminar(Integer id) {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Empleado no encontrado"));

        empleado.setEstadoId(2);

        empleadoRepository.save(empleado);
    }

    private void validarRequest(
            EmpleadoRequest request,
            boolean requirePassword
    ) {

        if (request.getUsername() == null ||
                request.getUsername().isBlank()) {

            throw new RuntimeException("Username obligatorio");
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new RuntimeException("Email obligatorio");
        }

        if (!request.getEmail().contains("@")) {
            throw new RuntimeException("Email inválido");
        }

        if (requirePassword &&
                (request.getPassword() == null ||
                        request.getPassword().isBlank())) {

            throw new RuntimeException("Password obligatorio");
        }

        if (request.getPassword() != null &&
                !request.getPassword().isBlank() &&
                request.getPassword().length() < 8) {

            throw new RuntimeException(
                    "Password mínimo 8 caracteres"
            );
        }

        if (request.getTipoEmpleadoId() == null) {
            throw new RuntimeException(
                    "Tipo empleado obligatorio"
            );
        }

        if (request.getNombreCompleto() == null ||
                request.getNombreCompleto().isBlank()) {

            throw new RuntimeException(
                    "Nombre obligatorio"
            );
        }

        if (request.getFechaIngreso() == null) {
            throw new RuntimeException(
                    "Fecha ingreso obligatoria"
            );
        }

        if (request.getFechaVencimientoLicencia() != null &&
                request.getFechaVencimientoLicencia()
                        .isBefore(LocalDate.now())) {

            throw new RuntimeException(
                    "Licencia vencida"
            );
        }
    }

    private String generarCodigoEmpleado(Integer tipoEmpleadoId) {

        TipoEmpleado tipoEmpleado = tipoEmpleadoRepository
                .findById(tipoEmpleadoId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tipo empleado no encontrado"
                        ));

        String nombre = tipoEmpleado.getNombre();

        String prefijo = nombre
                .trim()
                .toUpperCase()
                .substring(0, Math.min(3, nombre.length()));

        long total = empleadoRepository
                .countByTipoEmpleadoId(tipoEmpleadoId);

        return prefijo + "-"
                + String.format("%04d", total + 1);
    }

    private EmpleadoResponse convertirResponse(
            Empleado empleado
    ) {

        User user = userRepository
                .findById(empleado.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"
                        ));

        return convertirResponse(empleado, user);
    }

    private EmpleadoResponse convertirResponse(
            Empleado empleado,
            User user
    ) {

        EmpleadoResponse response =
                new EmpleadoResponse();

        response.setId(empleado.getId());

        response.setUserId(user.getId());

        response.setUsername(user.getUsername());

        response.setEmail(user.getEmail());

        response.setTipoEmpleadoId(
                empleado.getTipoEmpleadoId()
        );

        response.setAerolineaId(
                empleado.getAerolineaId()
        );

        response.setCodigoEmpleado(
                empleado.getCodigoEmpleado()
        );

        response.setNombreCompleto(
                empleado.getNombreCompleto()
        );

        response.setFechaIngreso(
                empleado.getFechaIngreso()
        );

        response.setFechaSalida(
                empleado.getFechaSalida()
        );

        response.setTurnoId(
                empleado.getTurnoId()
        );

        response.setNivelAccesoId(
                empleado.getNivelAccesoId()
        );

        response.setRolId(
                empleado.getRolId()
        );

        response.setAreaId(
                empleado.getAreaId()
        );

        response.setLicenciaId(
                empleado.getLicenciaId()
        );

        response.setFechaVencimientoLicencia(
                empleado.getFechaVencimientoLicencia()
        );

        response.setEstadoId(
                empleado.getEstadoId()
                
        );

        return response;
    }
}