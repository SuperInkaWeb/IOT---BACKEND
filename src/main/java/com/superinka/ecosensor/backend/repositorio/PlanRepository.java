package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.superinka.ecosensor.backend.modelo.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}