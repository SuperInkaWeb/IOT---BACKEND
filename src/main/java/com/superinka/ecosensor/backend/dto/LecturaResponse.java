package com.superinka.ecosensor.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LecturaResponse {

    private Long id;
    private String deviceId;
    private String tipoMetrica;
    private Double valor;
    private Double temperatura;
    private Double humedad;
    private LocalDateTime fecha;
}