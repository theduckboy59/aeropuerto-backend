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

        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new RuntimeException("Email ya registrado");
                });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        user = userRepository.save(user);

        Pasajero pasajero = new Pasajero();
        pasajero.setUserId(user.getId());
        pasajero.setTipoDocumentoId(request.getTipoDocumentoId());
        pasajero.setNumeroDocumento(request.getNumeroDocumento());
        pasajero.setNombreCompleto(request.getNombreCompleto());
        pasajero.setFechaNacimiento(request.getFechaNacimiento());
        pasajero.setNacionalidadId(request.getNacionalidadId());
        pasajero.setCodigoAreaId(request.getCodigoAreaId());
        pasajero.setTelefono(request.getTelefono());
        pasajero.setTelefonoEmergencia(request.getTelefonoEmergencia());
        pasajero.setDireccion(request.getDireccion());

        pasajeroRepository.save(pasajero);
    }
}