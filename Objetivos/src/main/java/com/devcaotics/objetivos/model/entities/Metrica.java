package com.devcaotics.objetivos.model.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/**
 * Parte da Meta (composição): o que será medido e qual o alvo.
 * Ex.: nome "Taxa de aprovação", unidade "%", valorAlvo 80.
 * Composição Metrica ◆──> Indicador (*): os Indicadores são removidos junto com a Métrica.
 */
@Entity
public class Metrica {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int codigo;
	@Column(length = 80, nullable = false)
	private String nome;
	private String descricao;
	@Column(length = 20)
	private String unidade;
	private double valorAlvo;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "metrica_codigo", nullable = false)
	@OrderBy("dataMedicao")
	private List<Indicador> indicadores;

	public Metrica() {
		this.indicadores = new ArrayList<Indicador>();
	}

	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public String getUnidade() {
		return unidade;
	}
	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}
	public double getValorAlvo() {
		return valorAlvo;
	}
	public void setValorAlvo(double valorAlvo) {
		this.valorAlvo = valorAlvo;
	}
	public List<Indicador> getIndicadores() {
		return indicadores;
	}
	public void setIndicadores(List<Indicador> indicadores) {
		this.indicadores = indicadores;
	}

}
