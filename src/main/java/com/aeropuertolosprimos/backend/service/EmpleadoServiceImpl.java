package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.EmpleadoRequest;
import com.aeropuertolosprimos.backend.model.Empleado;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.EmpleadoRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository,
                               UserRepository userRepository) {
        this.empleadoRepository = empleadoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Empleado crear(EmpleadoRequest request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("Email ya registrado");
                });

        empleadoRepository.findByCodigoEmpleado(request.getCodigoEmpleado())
                .ifPresent(e -> {
                    throw new RuntimeException("Código empleado ya existe");
                });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        user = userRepository.save(user);

        Empleado empleado = new Empleado();
        empleado.setUserId(user.getId());
        empleado.setTipoEmpleadoId(request.getTipoEmpleadoId());
        empleado.setAerolineaId(request.getAerolineaId());
        empleado.setCodigoEmpleado(request.getCodigoEmpleado());
        empleado.setNombreCompleto(request.getNombreCompleto());
        empleado.setFechaIngreso(request.getFechaIngreso());
        empleado.setFechaSalida(request.getFechaSalida());
        empleado.setTurnoId(request.getTurnoId());
        empleado.setNivelAccesoId(request.getNivelAccesoId());
        empleado.setRolId(request.getRolId());
        empleado.setAreaId(request.getAreaId());
        empleado.setLicenciaId(request.getLicenciaId());
        empleado.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());

        return empleadoRepository.save(empleado);
    }

    @Override
    public List<Empleado> listar() {
        return empleadoRepository.findAll();
    }

    @Override
    public Empleado obtenerPorId(Integer id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
    }

    @Override
    public Empleado actualizar(Integer id, EmpleadoRequest request) {

        Empleado empleado = obtenerPorId(id);

        empleado.setTipoEmpleadoId(request.getTipoEmpleadoId());
        empleado.setAerolineaId(request.getAerolineaId());
        empleado.setCodigoEmpleado(request.getCodigoEmpleado());
        empleado.setNombreCompleto(request.getNombreCompleto());
        empleado.setFechaIngreso(request.getFechaIngreso());
        empleado.setFechaSalida(request.getFechaSalida());
        empleado.setTurnoId(request.getTurnoId());
        empleado.setNivelAccesoId(request.getNivelAccesoId());
        empleado.setRolId(request.getRolId());
        empleado.setAreaId(request.getAreaId());
        empleado.setLicenciaId(request.getLicenciaId());
        empleado.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());

        return empleadoRepository.save(empleado);
    }

    @Override
    public void eliminar(Integer id) {
        empleadoRepository.deleteById(id);
    }
}