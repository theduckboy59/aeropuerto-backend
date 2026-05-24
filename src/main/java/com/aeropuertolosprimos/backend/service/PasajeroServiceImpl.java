package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.PasajeroRequest;
import com.aeropuertolosprimos.backend.dto.PasajeroResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.exception.ResourceNotFoundException;
import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.PasajeroRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PasajeroServiceImpl implements PasajeroService {

    private final PasajeroRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasajeroServiceImpl(
            PasajeroRepository repository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasajeroResponse crear(PasajeroRequest request) {

        validar(request);

        if (repository.existsByPasaporte(request.getPasaporte().trim())) {

            throw new BusinessException(
                    "El número de pasaporte ingresado ya cuenta con usuario."
            );
        }

        if (userRepository.findByUsername(
                request.getUsername().trim()
        ).isPresent()) {

            throw new BusinessException(
                    "El username ya existe."
            );
        }

        if (userRepository.findByEmail(
                request.getEmail().trim()
        ).isPresent()) {

            throw new BusinessException(
                    "El email ya existe."
            );
        }

        User user = new User();

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRolId(1);
        user.setEstadoId(1);

        user = userRepository.save(user);

        Pasajero pasajero = new Pasajero();

        pasajero.setUser(user);
        pasajero.setPasaporte(request.getPasaporte().trim());
        pasajero.setNombreCompleto(request.getNombreCompleto());
        pasajero.setFechaNacimiento(request.getFechaNacimiento());
        pasajero.setNacionalidad(request.getNacionalidad());
        pasajero.setCodigoArea(request.getCodigoArea());
        pasajero.setTelefono(request.getTelefono());
        pasajero.setTelefonoEmergencia(request.getTelefonoEmergencia());
        pasajero.setDireccion(request.getDireccion());
        pasajero.setEstadoId(1);

        pasajero = repository.save(pasajero);

        return mapResponse(pasajero);
    }

    @Override
    public List<PasajeroResponse> listar() {

        return repository.findByEstadoId(1)
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    public PasajeroResponse obtenerPorId(Integer id) {

        Pasajero pasajero = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pasajero no encontrado"));

        return mapResponse(pasajero);
    }

    @Override
    public PasajeroResponse obtenerActualPorEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new BusinessException("No se pudo identificar al usuario autenticado");
        }

        Pasajero pasajero = repository.findByUser_Email(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Pasajero no encontrado para el usuario autenticado"));

        return mapResponse(pasajero);
    }

    @Override
    public PasajeroResponse actualizar(Integer id, PasajeroRequest request) {

        validar(request);

        Pasajero pasajero = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pasajero no encontrado"));

        User user = pasajero.getUser();

        pasajero.setPasaporte(request.getPasaporte().trim());
        pasajero.setNombreCompleto(request.getNombreCompleto());
        pasajero.setFechaNacimiento(request.getFechaNacimiento());
        pasajero.setNacionalidad(request.getNacionalidad());
        pasajero.setCodigoArea(request.getCodigoArea());
        pasajero.setTelefono(request.getTelefono());
        pasajero.setTelefonoEmergencia(request.getTelefonoEmergencia());
        pasajero.setDireccion(request.getDireccion());

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        userRepository.save(user);

        pasajero = repository.save(pasajero);

        return mapResponse(pasajero);
    }

    @Override
    public List<PasajeroResponse> buscar(String nombre) {

        return repository
                .findByNombreCompletoContainingIgnoreCaseAndEstadoId(
                        nombre.trim(),
                        1
                )
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    @Override
    public void eliminar(Integer id) {

        Pasajero pasajero = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pasajero no encontrado"));

        pasajero.setEstadoId(2);

        repository.save(pasajero);
    }

    private void validar(PasajeroRequest request) {

        if (
                request.getUsername() == null ||
                        request.getUsername().isBlank() ||

                        request.getEmail() == null ||
                        request.getEmail().isBlank() ||

                        request.getPasaporte() == null ||
                        request.getPasaporte().isBlank() ||

                        request.getNombreCompleto() == null ||
                        request.getNombreCompleto().isBlank() ||

                        request.getFechaNacimiento() == null ||

                        request.getNacionalidad() == null ||
                        request.getNacionalidad().isBlank() ||

                        request.getTelefono() == null ||
                        !request.getTelefono().matches("\\d{8}") ||

                        request.getTelefonoEmergencia() == null ||
                        !request.getTelefonoEmergencia().matches("\\d{8}") ||

                        request.getDireccion() == null ||
                        request.getDireccion().isBlank()
        ) {

            throw new BusinessException(
                    "Debe ingresar los campos obligatorios"
            );
        }

        if (request.getPasaporte().length() > 15) {

            throw new BusinessException(
                    "El pasaporte no puede exceder 15 caracteres."
            );
        }

        String regex =
                "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$";

        if (!request.getPassword().matches(regex)) {

            throw new BusinessException(
                    "El formato de la contraseña debe incluir al menos una letra mayúscula, un carácter especial y un número"
            );
        }
    }

    private PasajeroResponse mapResponse(Pasajero pasajero) {

        return PasajeroResponse.builder()
                .id(pasajero.getId())
                .userId(pasajero.getUser().getId())
                .username(pasajero.getUser().getUsername())
                .email(pasajero.getUser().getEmail())
                .pasaporte(pasajero.getPasaporte())
                .nombreCompleto(pasajero.getNombreCompleto())
                .fechaNacimiento(pasajero.getFechaNacimiento())
                .nacionalidad(pasajero.getNacionalidad())
                .codigoArea(pasajero.getCodigoArea())
                .telefono(pasajero.getTelefono())
                .telefonoEmergencia(pasajero.getTelefonoEmergencia())
                .direccion(pasajero.getDireccion())
                .estado(
                        pasajero.getEstadoId() == 1
                                ? "Activo"
                                : "Inactivo"
                )
                .build();
    }
}
