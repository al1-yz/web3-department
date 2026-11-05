package com.devcaotics.objetivos.model.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devcaotics.objetivos.model.entities.Objetivo;

public interface ObjetivoRepository extends JpaRepository<Objetivo, Integer> {

	// Objetivo "dono" (todo) de uma Meta (parte)
	public Optional<Objetivo> findByMetasCodigo(int metaCodigo);

}
