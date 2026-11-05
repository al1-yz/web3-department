package com.devcaotics.objetivos.model.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/**
 * Parte do Objetivo (composição).
 * Composição Meta ◆── Metrica (*): as Métricas são removidas junto com a Meta.
 * Associação com Departamento: a Meta pertence a um Departamento (sem cascade).
 */
@Entity
public class Meta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int codigo;
	@Lob
	@Column(nullable = false)
	private String descricao;
	private LocalDate prazo;

	@ManyToOne(optional = false)
	@JoinColumn(name = "departamento_codigo", nullable = false)
	private Departamento departamento;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_codigo", nullable = false)
	@OrderBy("codigo")
	private List<Metrica> metricas;

	public Meta() {
		this.metricas = new ArrayList<Metrica>();
	}

	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public LocalDate getPrazo() {
		return prazo;
	}
	public void setPrazo(LocalDate prazo) {
		this.prazo = prazo;
	}
	public Departamento getDepartamento() {
		return departamento;
	}
	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}
	public List<Metrica> getMetricas() {
		return metricas;
	}
	public void setMetricas(List<Metrica> metricas) {
		this.metricas = metricas;
	}

}
