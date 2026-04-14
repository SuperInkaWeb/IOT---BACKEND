package com.superinka.ecosensor.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SuscripcionResponse {

    private Long id;
    private Long empresaId;
    private Long planId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
}