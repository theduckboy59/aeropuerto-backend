package com.aeropuertolosprimos.backend.security;

import com.aeropuertolosprimos.backend.model.Rol;
import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.RolRepository;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            RolRepository rolRepository
    ) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String input)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(input)
                .or(() -> userRepository.findByUsername(input))
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        if (user.getRolId() == null) {
            throw new UsernameNotFoundException("El usuario no tiene rol asignado");
        }

        Rol rol = rolRepository.findById(user.getRolId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Rol no encontrado"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
        );
    }
}