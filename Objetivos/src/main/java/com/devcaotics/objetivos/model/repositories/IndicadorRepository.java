package com.devcaotics.objetivos.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devcaotics.objetivos.model.entities.Indicador;

public interface IndicadorRepository extends JpaRepository<Indicador, Integer> {

}
