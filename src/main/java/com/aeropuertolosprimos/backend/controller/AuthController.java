package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.config.JwtService;
import com.aeropuertolosprimos.backend.dto.LoginRequest;
import com.aeropuertolosprimos.backend.dto.LoginResponse;
import com.aeropuertolosprimos.backend.dto.RegisterRequest;
import com.aeropuertolosprimos.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        service.register(request);
        return "Usuario creado correctamente";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails user = (UserDetails) auth.getPrincipal();

        String token = jwtService.generateToken(user.getUsername());

        return new LoginResponse(token);
    }
}