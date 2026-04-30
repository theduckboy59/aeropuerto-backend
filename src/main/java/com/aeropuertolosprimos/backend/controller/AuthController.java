package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.RegisterRequest;
import com.aeropuertolosprimos.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        service.register(request);
        return "Usuario creado correctamente";
    }
}