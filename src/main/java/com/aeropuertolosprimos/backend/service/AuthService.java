package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);
}