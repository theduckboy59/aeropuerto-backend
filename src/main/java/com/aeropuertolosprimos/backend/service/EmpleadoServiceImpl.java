package com.aeropuertolosprimos.backend.service;

import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.dto.EmpleadoResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.exception.ResourceNotFoundException;
import com.aeropuertolosprimos.backend.model.Aerolinea;
import com.aeropuertolosprimos.backend.model.Empleado;
import com.aeropuertolosprimos.backend.model.TipoEmpleado;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.AerolineaRepository;
import com.aeropuertolosprimos.backend.repository.DisponibilidadEmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.EmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.TipoEmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final String TIPO_PILOTO = "PILOTO";
    private static final String TIPO_COPILOTO = "COPILOTO";
    private static final String TIPO_CABINA = "CABINA";
    private static final String TIPO_INGENIERO_VUELO = "INGENIERO_VUELO";

    private final EmpleadoRepository empleadoRepository;
    private final UserRepository userRepository;
    private final TipoEmpleadoRepository tipoEmpleadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AerolineaRepository aerolineaRepository;
    private final DisponibilidadEmpleadoRepository disponibilidadRepository;
    private final CatalogoEstadoService catalogoEstadoService;

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

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRolId(request.getRolId());
        user.setEstadoId(estadoActivoId);

        user = userRepository.save(user);

        Empleado empleado = new Empleado();
        empleado.setEstadoId(estadoActivoId);
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
                cb.equal(root.get("estadoId"), catalogoEstadoService.obtenerActivoId());

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
    public List<EmpleadoResponse> listarDisponiblesParaTripulacion(
            Integer aerolineaId
    ) {

        if (aerolineaId == null) {
            throw new BusinessException("Debe seleccionar una aerolínea");
        }

        if (!aerolineaRepository.existsById(aerolineaId)) {
            throw new ResourceNotFoundException("Aerolínea no encontrada");
        }

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        Specification<Empleado> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("estadoId"), estadoActivoId),
                        cb.equal(root.get("aerolineaId"), aerolineaId)
                );

        return empleadoRepository.findAll(spec)
                .stream()
                .filter(this::esTipoPermitidoParaTripulacion)
                .filter(this::estaDisponibleParaTripulacion)
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

        empleado.setEstadoId(catalogoEstadoService.obtenerInactivoId());

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

    private boolean esTipoPermitidoParaTripulacion(Empleado empleado) {

        if (empleado == null || empleado.getTipoEmpleadoId() == null) {
            return false;
        }

        String tipo = obtenerNombreTipoEmpleado(empleado.getTipoEmpleadoId());

        if (tipo == null) {
            return false;
        }

        String normalizado = normalizarTipo(tipo);

        return normalizado.equals(TIPO_PILOTO) ||
                normalizado.equals(TIPO_COPILOTO) ||
                normalizado.equals(TIPO_CABINA) ||
                normalizado.equals(TIPO_INGENIERO_VUELO);
    }

    private boolean estaDisponibleParaTripulacion(Empleado empleado) {

        if (empleado == null || empleado.getId() == null) {
            return false;
        }

        return obtenerDisponibilidad(empleado.getId());
    }

    private Boolean obtenerDisponibilidad(Integer empleadoId) {

        if (empleadoId == null) {
            return false;
        }

        return disponibilidadRepository.findByEmpleadoId(empleadoId)
                .map(disponibilidad ->
                        !Boolean.FALSE.equals(disponibilidad.getDisponible())
                )
                .orElse(true);
    }

    private String obtenerNombreTipoEmpleado(Integer tipoEmpleadoId) {

        if (tipoEmpleadoId == null) {
            return null;
        }

        return tipoEmpleadoRepository.findById(tipoEmpleadoId)
                .map(TipoEmpleado::getNombre)
                .orElse(null);
    }

    private String normalizarTipo(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");
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
        response.setTipoEmpleadoNombre(
                obtenerNombreTipoEmpleado(empleado.getTipoEmpleadoId())
        );

        response.setAerolineaId(empleado.getAerolineaId());
        response.setAerolineaNombre(
                obtenerNombreAerolinea(empleado.getAerolineaId())
        );

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
        response.setDisponible(
                obtenerDisponibilidad(empleado.getId())
        );

        return response;
    }

    private String obtenerNombreAerolinea(Integer aerolineaId) {

        if (aerolineaId == null) {
            return null;
        }

        return aerolineaRepository.findById(aerolineaId)
                .map(Aerolinea::getNombre)
                .orElse(null);
    }
}