package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.model.User;
import com.aeropuertolosprimos.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que es un controlador REST (retorna JSON automáticamente)
@RequestMapping("/users") // URL base → http://localhost:8080/users
public class UserController {

    private final UserService userService; // Dependencia del servicio

    public UserController(UserService userService) {
        this.userService = userService; // Inyección por constructor
    }

    // Crear usuario
    @PostMapping // POST /users
    public User create(@RequestBody User user) {
        return userService.save(user);
        // Recibe JSON → lo convierte a objeto → lo guarda
    }

    // Obtener todos
    @GetMapping // GET /users
    public List<User> findAll() {
        return userService.findAll();
        // Retorna lista completa
    }

    // Obtener por ID
    @GetMapping("/{id}") // GET /users/{id}
    public User findById(@PathVariable Integer id) {
        return userService.findById(id);
        // Retorna un usuario o null si no existe
    }

    // Eliminar
    @DeleteMapping("/{id}") // DELETE /users/{id}
    public void delete(@PathVariable Integer id) {
        userService.deleteById(id);
        // Elimina por ID
    }

    // Actualizar
    @PutMapping // PUT /users
    public User update(@RequestBody User user) {
        return userService.update(user);
        // Actualiza (requiere ID en el JSON)
    }
}