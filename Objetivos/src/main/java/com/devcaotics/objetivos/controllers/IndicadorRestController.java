package com.devcaotics.objetivos.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.devcaotics.objetivos.model.entities.Indicador;
import com.devcaotics.objetivos.model.entities.Metrica;
import com.devcaotics.objetivos.model.repositories.IndicadorRepository;
import com.devcaotics.objetivos.model.repositories.MetricaRepository;

@RestController
@RequestMapping("/indicador")
public class IndicadorRestController {

	@Autowired
	private IndicadorRepository iRepository;
	@Autowired
	private MetricaRepository mtRepository;

	// não há POST /indicador: um Indicador é criado sempre dentro de uma Métrica (POST /metrica/{id}/indicador)

	@GetMapping
	public ResponseEntity<List<Indicador>> readAll() {
		return ResponseEntity.ok(this.iRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Indicador> read(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Indicador> update(@PathVariable int id, @RequestBody Indicador i) {
		validar(i);
		Indicador atual = buscar(id);
		atual.setValor(i.getValor());
		atual.setDataMedicao(i.getDataMedicao());
		atual.setObservacao(i.getObservacao());
		return ResponseEntity.ok(this.iRepository.save(atual));
	}

	// remove o Indicador da lista da Métrica (orphanRemoval apaga a linha)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		Indicador i = buscar(id);
		Metrica mt = this.mtRepository.findByIndicadoresCodigo(id).orElseThrow();
		mt.getIndicadores().remove(i);
		this.mtRepository.save(mt);
		return ResponseEntity.noContent().build();
	}

	private Indicador buscar(int id) {
		return this.iRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicador " + id + " não encontrado"));
	}

	static void validar(Indicador i) {
		if (i.getDataMedicao() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data da medição do indicador é obrigatória");
		}
	}

}
