package com.superinka.ecosensor.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuscripcionRequest {

	private Long empresaId;

	@NotNull
	private Long planId;
}