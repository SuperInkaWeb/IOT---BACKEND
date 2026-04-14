package com.superinka.ecosensor.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorResumenDTO {
    private Long id;
    private String deviceId;
    private String ubicacion;
}