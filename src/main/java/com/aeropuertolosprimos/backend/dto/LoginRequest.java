package com.aeropuertolosprimos.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class LoginRequest {
    @JsonAlias("username")
    private String email;
    private String password;
}
