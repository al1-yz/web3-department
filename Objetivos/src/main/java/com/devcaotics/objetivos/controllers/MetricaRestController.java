package com.devcaotics.objetivos.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.devcaotics.objetivos.model.entities.Indicador;
import com.devcaotics.objetivos.model.entities.Meta;
import com.devcaotics.objetivos.model.entities.Metrica;
import com.devcaotics.objetivos.model.repositories.MetaRepository;
import com.devcaotics.objetivos.model.repositories.MetricaRepository;

@RestController
@RequestMapping("/metrica")
public class MetricaRestController {

	@Autowired
	private MetricaRepository mtRepository;
	@Autowired
	private MetaRepository mRepository;

	// não há POST /metrica: uma Métrica é criada sempre dentro de uma Meta (POST /meta/{id}/metrica)

	@GetMapping
	public ResponseEntity<List<Metrica>> readAll() {
		return ResponseEntity.ok(this.mtRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Metrica> read(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Metrica> update(@PathVariable int id, @RequestBody Metrica mt) {
		validar(mt);
		Metrica atual = buscar(id);
		atual.setNome(mt.getNome());
		atual.setDescricao(mt.getDescricao());
		atual.setUnidade(mt.getUnidade());
		atual.setValorAlvo(mt.getValorAlvo());
		return ResponseEntity.ok(this.mtRepository.save(atual));
	}

	// remove a Métrica da lista da Meta (orphanRemoval apaga a Métrica e os Indicadores)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		Metrica mt = buscar(id);
		Meta m = this.mRepository.findByMetricasCodigo(id).orElseThrow();
		m.getMetricas().remove(mt);
		this.mRepository.save(m);
		return ResponseEntity.noContent().build();
	}

	// ---------- Indicadores da Métrica ----------

	@GetMapping("/{id}/indicador")
	public ResponseEntity<List<Indicador>> readIndicadores(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id).getIndicadores());
	}

	@PostMapping("/{id}/indicador")
	public ResponseEntity<Indicador> addIndicador(@PathVariable int id, @RequestBody Indicador i) {
		Metrica mt = buscar(id);
		IndicadorRestController.validar(i);
		i.setCodigo(0);
		mt.getIndicadores().add(i);
		this.mtRepository.saveAndFlush(mt);
		// a lista é ordenada por data só na leitura; o novo item é o de maior código
		Indicador criado = mt.getIndicadores().stream()
				.max((a, b) -> Integer.compare(a.getCodigo(), b.getCodigo())).orElseThrow();
		return ResponseEntity.status(HttpStatus.CREATED).body(criado);
	}

	// ---------- auxiliares ----------

	private Metrica buscar(int id) {
		return this.mtRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Métrica " + id + " não encontrada"));
	}

	static void validar(Metrica mt) {
		if (mt.getNome() == null || mt.getNome().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome da métrica é obrigatório");
		}
	}

}
