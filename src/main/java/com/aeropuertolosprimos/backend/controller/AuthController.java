package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.dto.ApiResponse;
import com.aeropuertolosprimos.backend.dto.LoginRequest;
import com.aeropuertolosprimos.backend.dto.LoginResponse;
import com.aeropuertolosprimos.backend.dto.RegisterRequest;
import com.aeropuertolosprimos.backend.security.JwtService;
import com.aeropuertolosprimos.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        service.register(request);

        ApiResponse response = new ApiResponse(
                HttpStatus.CREATED.value(),
                "Se ha creado con éxito el usuario.",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails user = (UserDetails) auth.getPrincipal();

        String role = user.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENTE");

        String token = jwtService.generateToken(
                user.getUsername(),
                role
        );

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }
}