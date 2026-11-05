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
import com.devcaotics.objetivos.model.repositories.ObjetivoRepository;

@RestController
@RequestMapping("/objetivo")
public class ObjetivoRestController {

	@Autowired
	private ObjetivoRepository oRepository;
	@Autowired
	private DepartamentoRepository dRepository;

	/**
	 * Cria o Objetivo junto com as suas Metas (composição, 1..*).
	 * As Metas podem vir com Métricas e Indicadores aninhados: tudo é salvo em cascata.
	 */
	@PostMapping
	public ResponseEntity<Objetivo> create(@RequestBody Objetivo o) {
		validar(o);
		if (o.getMetas() == null || o.getMetas().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todo objetivo precisa ter pelo menos uma meta");
		}
		o.setCodigo(0);
		for (Meta m : o.getMetas()) {
			prepararNovaMeta(m);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(this.oRepository.save(o));
	}

	@GetMapping
	public ResponseEntity<List<Objetivo>> readAll() {
		return ResponseEntity.ok(this.oRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Objetivo> read(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id));
	}

	// altera só os dados do Objetivo; as metas têm rotas próprias
	@PutMapping("/{id}")
	public ResponseEntity<Objetivo> update(@PathVariable int id, @RequestBody Objetivo o) {
		validar(o);
		Objetivo atual = buscar(id);
		atual.setTitulo(o.getTitulo());
		atual.setDescricao(o.getDescricao());
		return ResponseEntity.ok(this.oRepository.save(atual));
	}

	// composição: apagar o Objetivo apaga Metas, Métricas e Indicadores
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		this.oRepository.delete(buscar(id));
		return ResponseEntity.noContent().build();
	}

	// ---------- Metas do Objetivo (a parte é criada sempre a partir do todo) ----------

	@GetMapping("/{id}/meta")
	public ResponseEntity<List<Meta>> readMetas(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id).getMetas());
	}

	@PostMapping("/{id}/meta")
	public ResponseEntity<Meta> addMeta(@PathVariable int id, @RequestBody Meta m) {
		Objetivo o = buscar(id);
		prepararNovaMeta(m);
		o.getMetas().add(m);
		Objetivo salvo = this.oRepository.save(o);
		Meta criada = salvo.getMetas().get(salvo.getMetas().size() - 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(criada);
	}

	// ---------- auxiliares ----------

	private Objetivo buscar(int id) {
		return this.oRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objetivo " + id + " não encontrado"));
	}

	private void validar(Objetivo o) {
		if (o.getTitulo() == null || o.getTitulo().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O título do objetivo é obrigatório");
		}
	}

	/**
	 * Valida a Meta, troca o departamento enviado ({"codigo": X}) pelo departamento do banco
	 * e zera os códigos da Meta e das partes aninhadas para que sejam inseridas como novas.
	 */
	private void prepararNovaMeta(Meta m) {
		MetaRestController.validar(m);
		if (m.getDepartamento() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A meta precisa de um departamento");
		}
		int depId = m.getDepartamento().getCodigo();
		m.setDepartamento(this.dRepository.findById(depId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento " + depId + " não existe")));
		m.setCodigo(0);
		for (Metrica mt : m.getMetricas()) {
			MetricaRestController.validar(mt);
			mt.setCodigo(0);
			for (Indicador i : mt.getIndicadores()) {
				IndicadorRestController.validar(i);
				i.setCodigo(0);
			}
		}
	}

}
