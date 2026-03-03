package com.superinka.ecosensor.backend.dto;

import lombok.Data;

@Data
public class LecturaRequest {

    private Long sensorId;
    private String tipoMetrica;
    private Double valor;
}