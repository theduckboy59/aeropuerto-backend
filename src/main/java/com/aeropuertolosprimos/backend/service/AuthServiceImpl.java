package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.RegisterRequest;
import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.PasajeroRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasajeroRepository pasajeroRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserRepository userRepository,
                           PasajeroRepository pasajeroRepository) {
        this.userRepository = userRepository;
        this.pasajeroRepository = pasajeroRepository;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {

        // 1. Campos obligatorios (fail-safe)
        if (isBlank(request.getUsername()) ||
                isBlank(request.getEmail()) ||
                isBlank(request.getPassword()) ||
                isBlank(request.getDpi()) ||
                isBlank(request.getNombreCompleto()) ||
                isBlank(request.getTelefono()) ||
                isBlank(request.getDireccion()) ||
                isBlank(request.getNacionalidad()) ||
                isBlank(request.getCodigoArea()) ||
                request.getFechaNacimiento() == null) {

            throw new RuntimeException("Debe ingresar los campos obligatorios");
        }

        // 2. DPI validación
        String dpi = request.getDpi();

        if (!dpi.matches("\\d+")) {
            throw new RuntimeException("El DPI debe contener solo números");
        }

        if (pasajeroRepository.existsByDpi(dpi)) {
            throw new RuntimeException("El DPI ya cuenta con usuario");
        }

        // 3. Correo y username duplicados
        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("El correo ya está registrado");
                });

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new RuntimeException("El nombre de usuario ya está registrado");
                });

        // 4. Teléfono principal
        if (!request.getTelefono().matches("\\d+")) {
            throw new RuntimeException("El número de teléfono debe contener solo dígitos");
        }

        if (request.getTelefono().length() != 8) {
            throw new RuntimeException("El número de teléfono debe tener 8 dígitos");
        }

        // 5. Teléfono de emergencia (opcional, pero si viene debe cumplir 8 dígitos)
        if (request.getTelefonoEmergencia() != null && !request.getTelefonoEmergencia().isBlank()) {

            if (!request.getTelefonoEmergencia().matches("\\d+")) {
                throw new RuntimeException("El teléfono de emergencia debe contener solo dígitos");
            }

            if (request.getTelefonoEmergencia().length() != 8) {
                throw new RuntimeException("El teléfono de emergencia debe tener 8 dígitos");
            }
        }

        // 6. Guardar usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        user = userRepository.save(user);

        // 7. Guardar pasajero
        Pasajero pasajero = new Pasajero();
        pasajero.setUserId(user.getId());
        pasajero.setDpi(dpi);
        pasajero.setNombreCompleto(request.getNombreCompleto());
        pasajero.setFechaNacimiento(request.getFechaNacimiento());
        pasajero.setNacionalidad(request.getNacionalidad());
        pasajero.setCodigoArea(request.getCodigoArea());
        pasajero.setTelefono(request.getTelefono());
        pasajero.setTelefonoEmergencia(request.getTelefonoEmergencia());
        pasajero.setDireccion(request.getDireccion());

        pasajeroRepository.save(pasajero);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}