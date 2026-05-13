package com.aeropuertolosprimos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
}