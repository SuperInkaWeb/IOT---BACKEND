package com.superinka.ecosensor.backend.servicio;

import com.superinka.ecosensor.backend.dto.AuthResponse;
import com.superinka.ecosensor.backend.dto.LoginRequest;
import com.superinka.ecosensor.backend.dto.RegistroRequest;

public interface AuthService {

    AuthResponse registrar(RegistroRequest request);

    AuthResponse login(LoginRequest request);
}