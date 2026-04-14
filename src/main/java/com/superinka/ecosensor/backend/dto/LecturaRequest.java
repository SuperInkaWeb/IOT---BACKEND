package com.superinka.ecosensor.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LecturaRequest {

	@NotBlank(message = "El deviceId es obligatorio")
    private String deviceId;

    @NotBlank(message = "El tipoMetrica es obligatorio")
    private String tipoMetrica;

    @NotNull(message = "El valor es obligatorio")
    private Double valor;
    
    private Double temperatura;

    private Double humedad;
    
}