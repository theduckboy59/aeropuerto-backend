package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service // Indica que es un componente de servicio (Spring lo gestiona)
public class UserServiceImpl implements UserService { // Implementa la interfaz UserService

    private final UserRepository userRepository; // Dependencia del repositorio (inyección)

    public UserServiceImpl(UserRepository userRepository) {
        // Constructor para inyección de dependencias (Spring lo hace automáticamente)
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
        // Guarda el usuario en BD (insert o update si ya tiene ID)
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
        // Retorna todos los usuarios
    }

    @Override
    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
        // Busca por ID, si no existe devuelve null (riesgo de null handling)
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
        // Elimina el usuario por ID
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
        // Actualiza usuario (realmente save hace insert/update)
    }
}