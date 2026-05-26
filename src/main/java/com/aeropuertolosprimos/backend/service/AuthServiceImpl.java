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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$";

    private final UserRepository userRepository;
    private final PasajeroRepository pasajeroRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final CatalogoEstadoService catalogoEstadoService;


    @Override
    @Transactional
    public void register(RegisterRequest request) {

        validarRequestNoNulo(request);
        validarCamposObligatorios(request);

        String username = normalizar(request.getUsername());
        String email = normalizar(request.getEmail()).toLowerCase();
        String password = request.getPassword();
        String pasaporte = normalizar(request.getPasaporte());
        String nombreCompleto = normalizar(request.getNombreCompleto());
        String nacionalidad = normalizar(request.getNacionalidad());
        String codigoArea = normalizar(request.getCodigoArea());
        String telefono = normalizar(request.getTelefono());
        String telefonoEmergencia = normalizar(request.getTelefonoEmergencia());
        String direccion = normalizar(request.getDireccion());

        validarPasaporte(pasaporte);
        validarPassword(password);
        validarTelefonos(codigoArea, telefono, telefonoEmergencia);
        validarDuplicados(username, email, pasaporte);

        Rol rolCliente = rolRepository
                .findByNombreIgnoreCase(ROL_CLIENTE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Rol CLIENTE no encontrado"
                        )
                );

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRolId(rolCliente.getId());
        user.setEstadoId(estadoActivoId);

        user = userRepository.save(user);

        Pasajero pasajero = new Pasajero();
        pasajero.setUser(user);
        pasajero.setPasaporte(pasaporte);
        pasajero.setNombreCompleto(nombreCompleto);
        pasajero.setFechaNacimiento(request.getFechaNacimiento());
        pasajero.setNacionalidad(nacionalidad);
        pasajero.setCodigoArea(codigoArea);
        pasajero.setTelefono(telefono);
        pasajero.setTelefonoEmergencia(telefonoEmergencia);
        pasajero.setDireccion(direccion);
        pasajero.setEstadoId(estadoActivoId);

        pasajeroRepository.save(pasajero);
    }

    private void validarRequestNoNulo(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException(
                    "Debe ingresar los campos obligatorios"
            );
        }
    }

    private void validarCamposObligatorios(RegisterRequest request) {

        if (isBlank(request.getUsername()) ||
                isBlank(request.getEmail()) ||
                isBlank(request.getPassword()) ||
                isBlank(request.getPasaporte()) ||
                isBlank(request.getNombreCompleto()) ||
                request.getFechaNacimiento() == null ||
                isBlank(request.getNacionalidad()) ||
                isBlank(request.getCodigoArea()) ||
                isBlank(request.getTelefono()) ||
                isBlank(request.getTelefonoEmergencia()) ||
                isBlank(request.getDireccion())) {

            throw new BusinessException(
                    "Debe ingresar los campos obligatorios"
            );
        }
    }

    private void validarPasaporte(String pasaporte) {

        if (pasaporte.length() > 15) {
            throw new BusinessException(
                    "El número de pasaporte no debe exceder 15 caracteres"
            );
        }
    }

    private void validarPassword(String password) {

        if (!password.matches(PASSWORD_REGEX)) {
            throw new BusinessException(
                    "El formato de la contraseña debe incluir al menos una letra mayúscula, un carácter especial y un número"
            );
        }
    }

    private void validarTelefonos(
            String codigoArea,
            String telefono,
            String telefonoEmergencia
    ) {

        if (!codigoArea.matches("^\\+?\\d{1,10}$")) {
            throw new BusinessException(
                    "El código de área telefónico es inválido"
            );
        }

        if (!telefono.matches("\\d{8}")) {
            throw new BusinessException(
                    "El número de teléfono debe tener 8 dígitos"
            );
        }

        if (!telefonoEmergencia.matches("\\d{8}")) {
            throw new BusinessException(
                    "El teléfono de emergencia debe tener 8 dígitos"
            );
        }
    }

    private void validarDuplicados(
            String username,
            String email,
            String pasaporte
    ) {

        if (pasajeroRepository.existsByPasaporte(pasaporte)) {
            throw new BusinessException(
                    "El número de pasaporte ingresado ya cuenta con usuario."
            );
        }

        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new BusinessException(
                            "El correo ya está registrado"
                    );
                });

        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    throw new BusinessException(
                            "El nombre de usuario ya está registrado"
                    );
                });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizar(String value) {
        return value == null ? null : value.trim();
    }
}