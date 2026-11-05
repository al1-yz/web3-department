package com.devcaotics.objetivos.model.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devcaotics.objetivos.model.entities.Metrica;

public interface MetricaRepository extends JpaRepository<Metrica, Integer> {

	// Métrica "dona" (todo) de um Indicador (parte)
	public Optional<Metrica> findByIndicadoresCodigo(int indicadorCodigo);

}
