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

import com.devcaotics.objetivos.model.entities.Departamento;
import com.devcaotics.objetivos.model.entities.Meta;
import com.devcaotics.objetivos.model.repositories.DepartamentoRepository;
import com.devcaotics.objetivos.model.repositories.MetaRepository;

@RestController
@RequestMapping("/departamento")
public class DepartamentoRestController {

	@Autowired
	private DepartamentoRepository dRepository;
	@Autowired
	private MetaRepository mRepository;

	@PostMapping
	public ResponseEntity<Departamento> create(@RequestBody Departamento d) {
		validar(d);
		if (this.dRepository.existsByNome(d.getNome())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um departamento com esse nome");
		}
		d.setCodigo(0);
		return ResponseEntity.status(HttpStatus.CREATED).body(this.dRepository.save(d));
	}

	@GetMapping
	public ResponseEntity<List<Departamento>> readAll() {
		return ResponseEntity.ok(this.dRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Departamento> read(@PathVariable int id) {
		return ResponseEntity.ok(buscar(id));
	}

	// navegação Departamento -> Meta (seta do diagrama)
	@GetMapping("/{id}/metas")
	public ResponseEntity<List<Meta>> readMetas(@PathVariable int id) {
		buscar(id);
		return ResponseEntity.ok(this.mRepository.findByDepartamentoCodigo(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Departamento> update(@PathVariable int id, @RequestBody Departamento d) {
		validar(d);
		Departamento atual = buscar(id);
		atual.setNome(d.getNome());
		atual.setSigla(d.getSigla());
		atual.setResponsavel(d.getResponsavel());
		return ResponseEntity.ok(this.dRepository.save(atual));
	}

	// associação: o departamento não apaga as metas, então só pode sair se não tiver nenhuma
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		buscar(id);
		if (this.mRepository.existsByDepartamentoCodigo(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O departamento possui metas. Remova ou transfira as metas primeiro");
		}
		this.dRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	private Departamento buscar(int id) {
		return this.dRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Departamento " + id + " não encontrado"));
	}

	private void validar(Departamento d) {
		if (d.getNome() == null || d.getNome().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do departamento é obrigatório");
		}
	}

}
