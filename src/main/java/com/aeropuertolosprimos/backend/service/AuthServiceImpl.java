package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.RegisterRequest;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.exception.ResourceNotFoundException;
import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.model.Rol;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.PasajeroRepository;
import com.aeropuertolosprimos.backend.repository.RolRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasajeroRepository pasajeroRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasajeroRepository pasajeroRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.pasajeroRepository = pasajeroRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        if (isBlank(request.getUsername()) ||
                isBlank(request.getEmail()) ||
                isBlank(request.getPassword()) ||
                isBlank(request.getPasaporte()) ||
                isBlank(request.getNombreCompleto()) ||
                isBlank(request.getNacionalidad()) ||
                isBlank(request.getTelefonoEmergencia()) ||
                request.getFechaNacimiento() == null) {

            throw new BusinessException(
                    "Debe ingresar los campos obligatorios"
            );
        }

        String pasaporte = request.getPasaporte().trim();

        if (pasaporte.length() > 15) {
            throw new BusinessException(
                    "Debe ingresar los campos obligatorios"
            );
        }

        if (pasajeroRepository.existsByPasaporte(pasaporte)) {
            throw new BusinessException(
                    "El número de pasaporte ingresado ya cuenta con usuario."
            );
        }

        if (!request.getPassword().matches(
                "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$")) {

            throw new BusinessException(
                    "El formato de la contraseña debe incluir al menos una letra mayúscula, un carácter especial y un número"
            );
        }

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new BusinessException(
                            "El correo ya está registrado"
                    );
                });

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new BusinessException(
                            "El nombre de usuario ya está registrado"
                    );
                });

        if (!isBlank(request.getTelefono()) &&
                !request.getTelefono().matches("\\d{8}")) {

            throw new BusinessException(
                    "El número de teléfono debe tener 8 dígitos"
            );
        }

        if (!request.getTelefonoEmergencia()
                .matches("\\d{8}")) {

            throw new BusinessException(
                    "El teléfono de emergencia debe tener 8 dígitos"
            );
        }

        Rol rolCliente = rolRepository
                .findByNombre("CLIENTE")
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rol CLIENTE no encontrado"
                        ));

        User user = new User();

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRolId(rolCliente.getId());

        user = userRepository.save(user);

        Pasajero pasajero = new Pasajero();

        pasajero.setUser(user);
        pasajero.setPasaporte(pasaporte);
        pasajero.setNombreCompleto(
                request.getNombreCompleto().trim()
        );

        pasajero.setFechaNacimiento(
                request.getFechaNacimiento()
        );

        pasajero.setNacionalidad(
                request.getNacionalidad().trim()
        );

        pasajero.setCodigoArea(
                trimOrNull(request.getCodigoArea())
        );

        pasajero.setTelefono(
                trimOrNull(request.getTelefono())
        );

        pasajero.setTelefonoEmergencia(
                request.getTelefonoEmergencia().trim()
        );

        pasajero.setDireccion(
                trimOrNull(request.getDireccion())
        );

        pasajeroRepository.save(pasajero);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }
}