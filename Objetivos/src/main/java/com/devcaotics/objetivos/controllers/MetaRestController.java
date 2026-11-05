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
import com.devcaotics.objetivos.model.entities.Objetivo;
import com.devcaotics.objetivos.model.repositories.DepartamentoRepository;
import com.devcaotics.objetivos.model.repositories.MetaRepository;
import com.devcaotics.objetivos.model.repositories.ObjetivoRepository;

@RestController
@RequestMapping("/meta")
public class MetaRestController {

	@Autowired
	private MetaRepository mRepository;
	@Autowired
	private ObjetivoRepository oRepository;
	@Autowired
	private DepartamentoRepository dRepository;

	// não há POST /meta: uma Meta é criada sempre dentro de um Objetivo (POST /objetivo/{id}/meta)

	@GetMapping
	public ResponseEntity<List<Meta>> readAll() {
		return ResponseEntity.ok(this.mRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Meta> read(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id));
	}

	// altera descrição, prazo e departamento; as métricas têm rotas próprias
	@PutMapping("/{id}")
	public ResponseEntity<Meta> update(@PathVariable int id, @RequestBody Meta m) {
		validar(m);
		Meta atual = buscar(id);
		atual.setDescricao(m.getDescricao());
		atual.setPrazo(m.getPrazo());
		if (m.getDepartamento() != null) {
			int depId = m.getDepartamento().getCodigo();
			atual.setDepartamento(this.dRepository.findById(depId).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento " + depId + " não existe")));
		}
		return ResponseEntity.ok(this.mRepository.save(atual));
	}

	// remove a Meta da lista do Objetivo (orphanRemoval apaga Meta, Métricas e Indicadores)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		Meta m = buscar(id);
		Objetivo o = this.oRepository.findByMetasCodigo(id).orElseThrow();
		if (o.getMetas().size() == 1) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Todo objetivo precisa ter pelo menos uma meta. Apague o objetivo ou cadastre outra meta antes");
		}
		o.getMetas().remove(m);
		this.oRepository.save(o);
		return ResponseEntity.noContent().build();
	}

	// ---------- Métricas da Meta ----------

	@GetMapping("/{id}/metrica")
	public ResponseEntity<List<Metrica>> readMetricas(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id).getMetricas());
	}

	@PostMapping("/{id}/metrica")
	public ResponseEntity<Metrica> addMetrica(@PathVariable int id, @RequestBody Metrica mt) {
		Meta m = buscar(id);
		MetricaRestController.validar(mt);
		mt.setCodigo(0);
		for (Indicador i : mt.getIndicadores()) {
			IndicadorRestController.validar(i);
			i.setCodigo(0);
		}
		m.getMetricas().add(mt);
		Meta salva = this.mRepository.save(m);
		return ResponseEntity.status(HttpStatus.CREATED).body(salva.getMetricas().get(salva.getMetricas().size() - 1));
	}

	// ---------- auxiliares ----------

	private Meta buscar(int id) {
		return this.mRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta " + id + " não encontrada"));
	}

	static void validar(Meta m) {
		if (m.getDescricao() == null || m.getDescricao().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A descrição da meta é obrigatória");
		}
	}

}
