package com.superinka.ecosensor.backend.dto;

import lombok.Data;

@Data
public class SuscripcionRequest {

    private Long empresaId;
    private Long planId;
}