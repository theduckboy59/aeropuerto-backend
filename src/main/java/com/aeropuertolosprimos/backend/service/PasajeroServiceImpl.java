package com.aeropuertolosprimos.backend.service;
import lombok.RequiredArgsConstructor;

import com.aeropuertolosprimos.backend.dto.PasajeroRequest;
import com.aeropuertolosprimos.backend.dto.PasajeroResponse;
import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.exception.ResourceNotFoundException;
import com.aeropuertolosprimos.backend.model.Pasajero;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.PasajeroRepository;
import com.aeropuertolosprimos.backend.repository.RolRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PasajeroServiceImpl implements PasajeroService {

    private static final String ROL_PASAJERO = "PASAJERO";

    private final PasajeroRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CatalogoEstadoService catalogoEstadoService;
    private final RolRepository rolRepository;


    @Override
    public PasajeroResponse crear(PasajeroRequest request) {

        validar(request, true);

        if (repository.existsByPasaporte(request.getPasaporte().trim())) {
            throw new BusinessException("El número de pasaporte ingresado ya cuenta con usuario.");
        }

        if (userRepository.findByUsername(request.getUsername().trim()).isPresent()) {
            throw new BusinessException("El username ya existe.");
        }

        if (userRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new BusinessException("El email ya existe.");
        }

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        User user = new User();

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRolId(obtenerRolPasajeroId());
        user.setEstadoId(estadoActivoId);

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
        pasajero.setEstadoId(estadoActivoId);

        pasajero = repository.save(pasajero);

        return mapResponse(pasajero);
    }

    @Override
    public List<PasajeroResponse> listar() {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return repository.findByEstadoId(estadoActivoId)
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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pasajero no encontrado para el usuario autenticado"));

        return mapResponse(pasajero);
    }

    @Override
    public PasajeroResponse actualizar(Integer id, PasajeroRequest request) {
        Pasajero pasajero = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pasajero no encontrado"));

        if (request.getPasaporte() == null || request.getPasaporte().isBlank()) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getNombreCompleto() == null || request.getNombreCompleto().isBlank()) {
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        repository.findByPasaporte(request.getPasaporte().trim())
                .ifPresent(existente -> {
                    if (!Objects.equals(existente.getId(), id)) {
                        throw new BusinessException("El número de pasaporte ya existe.");
                    }
                });

        pasajero.setPasaporte(request.getPasaporte().trim());
        pasajero.setNombreCompleto(request.getNombreCompleto().trim());
        pasajero.setFechaNacimiento(request.getFechaNacimiento());
        pasajero.setNacionalidad(request.getNacionalidad());
        pasajero.setCodigoArea(request.getCodigoArea());
        pasajero.setTelefono(request.getTelefono());
        pasajero.setTelefonoEmergencia(request.getTelefonoEmergencia());
        pasajero.setDireccion(request.getDireccion());

        User user = pasajero.getUser();

        if (user != null) {
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                String username = request.getUsername().trim();

                userRepository.findByUsername(username)
                        .ifPresent(existente -> {
                            if (!Objects.equals(existente.getId(), user.getId())) {
                                throw new BusinessException("El username ya existe.");
                            }
                        });

                user.setUsername(username);
            }

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                String email = request.getEmail().trim();

                userRepository.findByEmail(email)
                        .ifPresent(existente -> {
                            if (!Objects.equals(existente.getId(), user.getId())) {
                                throw new BusinessException("El email ya existe.");
                            }
                        });

                user.setEmail(email);
            }

            userRepository.save(user);
        }

        return mapResponse(repository.save(pasajero));
    }

    @Override
    public List<PasajeroResponse> buscar(String nombre) {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return repository
                .findByNombreCompletoContainingIgnoreCaseAndEstadoId(
                        nombre.trim(),
                        estadoActivoId
                )
                .stream()
                .map(this::mapResponse)
                .toList();
    }

            @Override
            public List<PasajeroResponse> buscarConFiltros(
                String nombre,
                String pasaporte,
                Integer estadoId
            ) {

            String nombreFiltro = nombre == null ? "" : nombre.trim().toLowerCase();
            String pasaporteFiltro = pasaporte == null ? "" : pasaporte.trim().toLowerCase();

            return repository.findAll()
                .stream()
                .filter(p -> estadoId == null || Objects.equals(p.getEstadoId(), estadoId))
                .filter(p -> nombreFiltro.isBlank()
                    || valor(p.getNombreCompleto()).toLowerCase().contains(nombreFiltro))
                .filter(p -> pasaporteFiltro.isBlank()
                    || valor(p.getPasaporte()).toLowerCase().contains(pasaporteFiltro))
                .map(this::mapResponse)
                .toList();
            }

    @Override
    public void eliminar(Integer id) {
        Pasajero pasajero = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pasajero no encontrado"));

        Integer estadoInactivoId = catalogoEstadoService.obtenerInactivoId();

        pasajero.setEstadoId(estadoInactivoId);
        repository.save(pasajero);

        if (pasajero.getUser() != null) {
            User user = pasajero.getUser();
            user.setEstadoId(estadoInactivoId);
            userRepository.save(user);
        }
    }

    private void validar(PasajeroRequest request, boolean requirePassword) {

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
            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getPasaporte().length() > 15) {
            throw new BusinessException("El pasaporte no puede exceder 15 caracteres.");
        }

        if (requirePassword &&
                (request.getPassword() == null || request.getPassword().isBlank())) {

            throw new BusinessException("Debe ingresar los campos obligatorios");
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {

            String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$";

            if (!request.getPassword().matches(regex)) {
                throw new BusinessException(
                        "El formato de la contraseña debe incluir al menos una letra mayúscula, un carácter especial y un número"
                );
            }
        }
    }

    private PasajeroResponse mapResponse(Pasajero pasajero) {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();
        User user = pasajero.getUser();

        return PasajeroResponse.builder()
                .id(pasajero.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .pasaporte(pasajero.getPasaporte())
                .nombreCompleto(pasajero.getNombreCompleto())
                .fechaNacimiento(pasajero.getFechaNacimiento())
                .nacionalidad(pasajero.getNacionalidad())
                .codigoArea(pasajero.getCodigoArea())
                .telefono(pasajero.getTelefono())
                .telefonoEmergencia(pasajero.getTelefonoEmergencia())
                .direccion(pasajero.getDireccion())
                .estado(
                        Objects.equals(pasajero.getEstadoId(), estadoActivoId)
                                ? "Activo"
                                : "Inactivo"
                )
                .build();
    }

    private String valor(
            String value
    ) {
        return value == null ? "" : value;
    }

    private Integer obtenerRolPasajeroId() {

        return rolRepository
                .findByNombreIgnoreCase(ROL_PASAJERO)
                .orElseThrow(() ->
                        new BusinessException("Rol PASAJERO no encontrado")
                )
                .getId();
    }
}
