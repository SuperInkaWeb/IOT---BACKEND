package com.superinka.ecosensor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class SensorResumenDTO {
    private Long id;
    private String deviceId;
    private String ubicacion;
    private String tipo;      
    private Boolean activo; 
}