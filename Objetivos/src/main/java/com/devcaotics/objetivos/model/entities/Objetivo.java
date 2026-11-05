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
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/**
 * Raiz da hierarquia.
 * Composição Objetivo ◆── Meta (1..*): a Meta só existe dentro de um Objetivo
 * e é removida junto com ele. Todo Objetivo precisa ter pelo menos uma Meta.
 */
@Entity
public class Objetivo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int codigo;
	@Column(length = 120, nullable = false)
	private String titulo;
	@Lob
	private String descricao;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "objetivo_codigo", nullable = false)
	@OrderBy("codigo")
	private List<Meta> metas;

	public Objetivo() {
		this.metas = new ArrayList<Meta>();
	}

	public int getCodigo() {
		return codigo;
	}
	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public List<Meta> getMetas() {
		return metas;
	}
	public void setMetas(List<Meta> metas) {
		this.metas = metas;
	}

}
