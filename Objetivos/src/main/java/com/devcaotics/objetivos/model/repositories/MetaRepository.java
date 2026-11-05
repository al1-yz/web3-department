package com.devcaotics.objetivos.model.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devcaotics.objetivos.model.entities.Meta;

public interface MetaRepository extends JpaRepository<Meta, Integer> {

	public List<Meta> findByDepartamentoCodigo(int departamentoCodigo);
	public boolean existsByDepartamentoCodigo(int departamentoCodigo);

	// Meta "dona" (todo) de uma Métrica (parte)
	public Optional<Meta> findByMetricasCodigo(int metricaCodigo);

}
