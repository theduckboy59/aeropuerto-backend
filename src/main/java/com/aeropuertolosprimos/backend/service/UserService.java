package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.model.User;

import java.util.List;

public interface UserService { // Define el contrato del servicio (no implementación)

    User save(User user);
    // Guarda un usuario (insert)
    // Entrada: objeto User
    // Salida: User persistido (con ID generado)

    List<User> findAll();
    // Obtiene todos los usuarios
    // Salida: lista de usuarios

    User findById(Integer id);
    // Busca un usuario por su ID
    // Entrada: ID
    // Salida: User (puede ser null si no existe → cuidado)

    void deleteById(Integer id);
    // Elimina un usuario por ID
    // Entrada: ID
    // Sin retorno

    User update(User user);
    // Actualiza un usuario existente
    // Entrada: objeto User con ID
    // Salida: User actualizado
}