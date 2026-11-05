package com.devcaotics.objetivos.model.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Parte da Métrica (composição): uma medição registrada da métrica.
 * Ex.: valor 72 (%) medido em 2026-06-30.
 */
@Entity
public class Indicador {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int codigo;
	private double valor;
	@Column(nullable = false)
	private LocalDate dataMedicao;
	private String observacao;

	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public double getValor() {
		return valor;
	}
	public void setValor(double valor) {
		this.valor = valor;
	}
	public LocalDate getDataMedicao() {
		return dataMedicao;
	}
	public void setDataMedicao(LocalDate dataMedicao) {
		this.dataMedicao = dataMedicao;
	}
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

}
