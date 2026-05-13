package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.dto.EmpleadoResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.exception.ResourceNotFoundException;
import com.aeropuertolosprimos.backend.model.Empleado;
import com.aeropuertolosprimos.backend.model.TipoEmpleado;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.EmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.TipoEmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UserRepository userRepository;
    private final TipoEmpleadoRepository tipoEmpleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoServiceImpl(
            EmpleadoRepository empleadoRepository,
            UserRepository userRepository,
            TipoEmpleadoRepository tipoEmpleadoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.empleadoRepository = empleadoRepository;
        this.userRepository = userRepository;
        this.tipoEmpleadoRepository = tipoEmpleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmpleadoResponse crear(EmpleadoRequest request) {

        validarRequest(request, true);

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new BusinessException("El correo ya está registrado");
                });

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new BusinessException("El nombre de usuario ya está registrado");
                });

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRolId(request.getRolId());

        user = userRepository.save(user);

        Empleado empleado = new Empleado();
        empleado.setUser(user);
        empleado.setTipoEmpleadoId(request.getTipoEmpleadoId());
        empleado.setCodigoEmpleado(generarCodigoEmpleado(request.getTipoEmpleadoId()));
        empleado.setAerolineaId(request.getAerolineaId());
        empleado.setNombreCompleto(request.getNombreCompleto().trim());
        empleado.setFechaIngreso(request.getFechaIngreso());
        empleado.setFechaSalida(request.getFechaSalida());
        empleado.setTurnoId(request.getTurnoId());
        empleado.setNivelAccesoId(request.getNivelAccesoId());
        empleado.setRolId(request.getRolId());
        empleado.setAreaId(request.getAreaId());
        empleado.setLicenciaId(request.getLicenciaId());
        empleado.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());

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
                        new ResourceNotFoundException("Empleado no encontrado"));

        return convertirResponse(empleado);
    }

    @Override
    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest request) {

        validarRequest(request, false);

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Empleado no encontrado"));

        User user = empleado.getUser();

        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setRolId(request.getRolId());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        if (!empleado.getTipoEmpleadoId().equals(request.getTipoEmpleadoId())) {
            empleado.setTipoEmpleadoId(request.getTipoEmpleadoId());
            empleado.setCodigoEmpleado(generarCodigoEmpleado(request.getTipoEmpleadoId()));
        }

        empleado.setAerolineaId(request.getAerolineaId());
        empleado.setNombreCompleto(request.getNombreCompleto().trim());
        empleado.setFechaIngreso(request.getFechaIngreso());
        empleado.setFechaSalida(request.getFechaSalida());
        empleado.setTurnoId(request.getTurnoId());
        empleado.setNivelAccesoId(request.getNivelAccesoId());
        empleado.setRolId(request.getRolId());
        empleado.setAreaId(request.getAreaId());
        empleado.setLicenciaId(request.getLicenciaId());
        empleado.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());

        empleado = empleadoRepository.save(empleado);

        return convertirResponse(empleado, user);
    }

    @Override
    public void eliminar(Integer id) {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Empleado no encontrado"));

        empleado.setEstadoId(2);

        empleadoRepository.save(empleado);
    }

    private void validarRequest(EmpleadoRequest request, boolean requirePassword) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (!request.getEmail().contains("@")) {
            throw new BusinessException("Correo electrónico inválido");
        }

        if (requirePassword && (request.getPassword() == null || request.getPassword().isBlank())) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getPassword() != null &&
                !request.getPassword().isBlank() &&
                request.getPassword().length() < 8) {
            throw new BusinessException("La contraseña debe tener mínimo 8 caracteres");
        }

        if (request.getTipoEmpleadoId() == null ||
                request.getAerolineaId() == null ||
                request.getNombreCompleto() == null ||
                request.getNombreCompleto().isBlank() ||
                request.getFechaIngreso() == null ||
                request.getTurnoId() == null ||
                request.getNivelAccesoId() == null ||
                request.getRolId() == null ||
                request.getAreaId() == null) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getFechaSalida() != null &&
                request.getFechaSalida().isBefore(request.getFechaIngreso())) {
            throw new BusinessException("La fecha de salida no puede ser menor que la fecha de ingreso");
        }

        if (request.getFechaVencimientoLicencia() != null &&
                request.getFechaVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new BusinessException("La licencia se encuentra vencida");
        }
    }

    private String generarCodigoEmpleado(Integer tipoEmpleadoId) {

        TipoEmpleado tipoEmpleado = tipoEmpleadoRepository
                .findById(tipoEmpleadoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tipo empleado no encontrado"));

        String nombre = tipoEmpleado.getNombre();

        String prefijo = nombre
                .trim()
                .toUpperCase()
                .substring(0, Math.min(3, nombre.length()));

        long total = empleadoRepository.countByTipoEmpleadoId(tipoEmpleadoId);

        return prefijo + "-" + String.format("%04d", total + 1);
    }

    private EmpleadoResponse convertirResponse(Empleado empleado) {

        User user = empleado.getUser();

        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        return convertirResponse(empleado, user);
    }

    private EmpleadoResponse convertirResponse(Empleado empleado, User user) {

        EmpleadoResponse response = new EmpleadoResponse();

        response.setId(empleado.getId());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setTipoEmpleadoId(empleado.getTipoEmpleadoId());
        response.setAerolineaId(empleado.getAerolineaId());
        response.setCodigoEmpleado(empleado.getCodigoEmpleado());
        response.setNombreCompleto(empleado.getNombreCompleto());
        response.setFechaIngreso(empleado.getFechaIngreso());
        response.setFechaSalida(empleado.getFechaSalida());
        response.setTurnoId(empleado.getTurnoId());
        response.setNivelAccesoId(empleado.getNivelAccesoId());
        response.setRolId(empleado.getRolId());
        response.setAreaId(empleado.getAreaId());
        response.setLicenciaId(empleado.getLicenciaId());
        response.setFechaVencimientoLicencia(empleado.getFechaVencimientoLicencia());
        response.setEstadoId(empleado.getEstadoId());

        return response;
    }
}