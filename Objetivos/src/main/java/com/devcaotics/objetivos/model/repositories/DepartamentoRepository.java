package com.devcaotics.objetivos.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devcaotics.objetivos.model.entities.Departamento;

public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {

	public boolean existsByNome(String nome);

}
