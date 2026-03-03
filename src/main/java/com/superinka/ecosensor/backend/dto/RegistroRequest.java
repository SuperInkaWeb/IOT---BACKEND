package com.superinka.ecosensor.backend.dto;

import lombok.Data;

@Data
public class RegistroRequest {

    private String nombre;
    private String email;
    private String password;
    private Long empresaId;
}