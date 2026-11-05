// ==================== acesso à API ====================

async function api(metodo, url, corpo) {
	const opcoes = { method: metodo, headers: {} };
	if (corpo !== undefined) {
		opcoes.headers["Content-Type"] = "application/json";
		opcoes.body = JSON.stringify(corpo);
	}
	const resp = await fetch(url, opcoes);
	const texto = await resp.text();
	const dados = texto ? JSON.parse(texto) : null;
	if (!resp.ok) {
		// mensagem do ResponseStatusException do backend
		throw new Error((dados && dados.message) || ("Erro " + resp.status));
	}
	return dados;
}

// executa uma ação, mostra a mensagem e recarrega a tela
async function executar(acao, sucesso) {
	try {
		await acao();
		if (sucesso) mensagem(sucesso, "success");
		await recarregar();
		return true;
	} catch (e) {
		mensagem(e.message, "error");
		return false;
	}
}

// ==================== utilitários de tela ====================

function esc(v) {
	if (v === null || v === undefined) return "";
	return String(v).replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function dataBR(iso) {
	if (!iso) return "";
	const [a, m, d] = iso.split("-");
	return d + "/" + m + "/" + a;
}

function num(v) {
	return Number(v).toLocaleString("pt-BR", { maximumFractionDigits: 2 });
}

function mensagem(texto, tipo) {
	const div = document.createElement("div");
	div.className = "alert alert-" + tipo;
	div.textContent = texto;
	document.getElementById("toasts").appendChild(div);
	setTimeout(() => div.remove(), 4000);
}

function confirmar(texto) {
	return window.confirm(texto);
}

// ==================== formulário genérico (modal) ====================

const modal = document.getElementById("modal-form");
const modalCampos = document.getElementById("modal-campos");
let aoSalvar = null;

/**
 * campos: [{ nome, rotulo, tipo: text|textarea|number|date|select, obrigatorio, opcoes: [{valor, texto}], secao }]
 * valores: objeto com os valores iniciais (para editar)
 * salvar: função async que recebe o objeto com os valores do formulário
 */
function abrirFormulario(titulo, campos, valores, salvar) {
	document.getElementById("modal-titulo").textContent = titulo;
	valores = valores || {};
	let html = "";
	for (const c of campos) {
		if (c.secao) {
			html += `<div class="divider my-1 text-sm">${esc(c.secao)}</div>`;
			continue;
		}
		const v = valores[c.nome] ?? "";
		const req = c.obrigatorio ? "required" : "";
		const rotulo = esc(c.rotulo) + (c.obrigatorio ? " *" : "");
		let campo;
		if (c.tipo === "textarea") {
			campo = `<textarea name="${c.nome}" class="textarea w-full" ${req}>${esc(v)}</textarea>`;
		} else if (c.tipo === "select") {
			campo = `<select name="${c.nome}" class="select w-full" ${req}>
				<option value="" disabled ${v === "" ? "selected" : ""}>Selecione...</option>
				${c.opcoes.map(o => `<option value="${esc(o.valor)}" ${String(o.valor) === String(v) ? "selected" : ""}>${esc(o.texto)}</option>`).join("")}
			</select>`;
		} else {
			const passo = c.tipo === "number" ? 'step="any"' : "";
			campo = `<input name="${c.nome}" type="${c.tipo || "text"}" ${passo} class="input w-full" value="${esc(v)}" ${req} />`;
		}
		html += `<label class="flex flex-col gap-1"><span class="text-sm font-medium">${rotulo}</span>${campo}</label>`;
	}
	modalCampos.innerHTML = html;
	aoSalvar = salvar;
	modal.showModal();
}

modalCampos.addEventListener("submit", async (ev) => {
	ev.preventDefault();
	const dados = {};
	for (const [k, v] of new FormData(modalCampos).entries()) {
		dados[k] = v === "" ? null : v;
	}
	const ok = await executar(() => aoSalvar(dados), "Salvo com sucesso");
	if (ok) modal.close();
});
document.getElementById("modal-cancelar").addEventListener("click", () => modal.close());

// ==================== campos de cada entidade ====================

let departamentos = [];

function opcoesDepartamento() {
	return departamentos.map(d => ({ valor: d.codigo, texto: d.sigla ? d.sigla + " - " + d.nome : d.nome }));
}

const camposDepartamento = () => [
	{ nome: "nome", rotulo: "Nome", obrigatorio: true },
	{ nome: "sigla", rotulo: "Sigla" },
	{ nome: "responsavel", rotulo: "Responsável" },
];
const camposObjetivo = () => [
	{ nome: "titulo", rotulo: "Título", obrigatorio: true },
	{ nome: "descricao", rotulo: "Descrição", tipo: "textarea" },
];
const camposMeta = () => [
	{ nome: "descricao", rotulo: "Descrição da meta", tipo: "textarea", obrigatorio: true },
	{ nome: "prazo", rotulo: "Prazo", tipo: "date" },
	{ nome: "departamento", rotulo: "Departamento", tipo: "select", obrigatorio: true, opcoes: opcoesDepartamento() },
];
const camposMetrica = () => [
	{ nome: "nome", rotulo: "Nome", obrigatorio: true },
	{ nome: "descricao", rotulo: "Descrição" },
	{ nome: "unidade", rotulo: "Unidade (%, R$, alunos...)" },
	{ nome: "valorAlvo", rotulo: "Valor alvo", tipo: "number", obrigatorio: true },
];
const camposIndicador = () => [
	{ nome: "valor", rotulo: "Valor medido", tipo: "number", obrigatorio: true },
	{ nome: "dataMedicao", rotulo: "Data da medição", tipo: "date", obrigatorio: true },
	{ nome: "observacao", rotulo: "Observação" },
];

// converte os valores do formulário de Meta para o JSON da API
function metaJson(f) {
	return { descricao: f.descricao, prazo: f.prazo, departamento: { codigo: Number(f.departamento) } };
}

function exigirDepartamento() {
	if (departamentos.length === 0) {
		mensagem("Cadastre um departamento antes: toda meta pertence a um departamento", "warning");
		return false;
	}
	return true;
}

// ==================== ações ====================

function novoDepartamento() {
	abrirFormulario("Novo departamento", camposDepartamento(), null, f => api("POST", "/departamento", f));
}
function editarDepartamento(d) {
	abrirFormulario("Editar departamento", camposDepartamento(), d, f => api("PUT", "/departamento/" + d.codigo, f));
}
function excluirDepartamento(d) {
	if (confirmar(`Remover o departamento "${d.nome}"?`)) {
		executar(() => api("DELETE", "/departamento/" + d.codigo), "Departamento removido");
	}
}
async function verMetasDepartamento(d) {
	try {
		const metas = await api("GET", "/departamento/" + d.codigo + "/metas");
		document.getElementById("modal-metas-titulo").textContent = "Metas de " + d.nome;
		document.getElementById("modal-metas-lista").innerHTML = metas.length
			? metas.map(m => `<li class="list-row"><div>${esc(m.descricao)}</div><div class="text-sm opacity-70">${m.prazo ? "até " + dataBR(m.prazo) : ""}</div></li>`).join("")
			: `<li class="list-row opacity-70">Nenhuma meta</li>`;
		document.getElementById("modal-metas").showModal();
	} catch (e) {
		mensagem(e.message, "error");
	}
}

// o objetivo já nasce com a primeira meta (todo objetivo precisa ter uma meta)
function novoObjetivo() {
	if (!exigirDepartamento()) return;
	// a descrição da meta é renomeada para não colidir com a descrição do objetivo
	const camposDaMeta = camposMeta().map(c => c.nome === "descricao" ? { ...c, nome: "metaDescricao" } : c);
	const campos = [...camposObjetivo(), { secao: "Primeira meta (obrigatória)" }, ...camposDaMeta];
	abrirFormulario("Novo objetivo", campos, null, f => api("POST", "/objetivo", {
		titulo: f.titulo,
		descricao: f.descricao,
		metas: [metaJson({ descricao: f.metaDescricao, prazo: f.prazo, departamento: f.departamento })],
	}));
}
function editarObjetivo(o) {
	abrirFormulario("Editar objetivo", camposObjetivo(), o, f => api("PUT", "/objetivo/" + o.codigo, f));
}
function excluirObjetivo(o) {
	if (confirmar(`Remover o objetivo "${o.titulo}"?\nTodas as metas, métricas e indicadores dele também serão apagados.`)) {
		executar(() => api("DELETE", "/objetivo/" + o.codigo), "Objetivo removido");
	}
}

function novaMeta(o) {
	if (!exigirDepartamento()) return;
	abrirFormulario("Nova meta em \"" + o.titulo + "\"", camposMeta(), null,
		f => api("POST", "/objetivo/" + o.codigo + "/meta", metaJson(f)));
}
function editarMeta(m) {
	abrirFormulario("Editar meta", camposMeta(), { ...m, departamento: m.departamento.codigo },
		f => api("PUT", "/meta/" + m.codigo, metaJson(f)));
}
function excluirMeta(m) {
	if (confirmar("Remover esta meta?\nAs métricas e indicadores dela também serão apagados.")) {
		executar(() => api("DELETE", "/meta/" + m.codigo), "Meta removida");
	}
}

function novaMetrica(m) {
	abrirFormulario("Nova métrica", camposMetrica(), null, f => api("POST", "/meta/" + m.codigo + "/metrica", f));
}
function editarMetrica(mt) {
	abrirFormulario("Editar métrica", camposMetrica(), mt, f => api("PUT", "/metrica/" + mt.codigo, f));
}
function excluirMetrica(mt) {
	if (confirmar(`Remover a métrica "${mt.nome}"?\nOs indicadores dela também serão apagados.`)) {
		executar(() => api("DELETE", "/metrica/" + mt.codigo), "Métrica removida");
	}
}

function novoIndicador(mt) {
	abrirFormulario("Nova medição de \"" + mt.nome + "\"", camposIndicador(),
		{ dataMedicao: new Date().toISOString().slice(0, 10) },
		f => api("POST", "/metrica/" + mt.codigo + "/indicador", f));
}
function editarIndicador(i) {
	abrirFormulario("Editar indicador", camposIndicador(), i, f => api("PUT", "/indicador/" + i.codigo, f));
}
function excluirIndicador(i) {
	if (confirmar("Remover esta medição?")) {
		executar(() => api("DELETE", "/indicador/" + i.codigo), "Indicador removido");
	}
}

// ==================== renderização ====================

// os botões guardam o nome da ação e o índice do objeto em "registro"
let registro = [];
function botao(texto, acao, obj, classe) {
	registro.push(obj);
	return `<button class="btn btn-xs ${classe || "btn-ghost"}" data-acao="${acao}" data-ref="${registro.length - 1}">${texto}</button>`;
}

const acoes = {
	editarDepartamento, excluirDepartamento, verMetasDepartamento,
	editarObjetivo, excluirObjetivo, novaMeta,
	editarMeta, excluirMeta, novaMetrica,
	editarMetrica, excluirMetrica, novoIndicador,
	editarIndicador, excluirIndicador,
};
document.addEventListener("click", (ev) => {
	const b = ev.target.closest("[data-acao]");
	if (b) acoes[b.dataset.acao](registro[Number(b.dataset.ref)]);
});

// guarda quais <details> estão abertos para não fechar tudo ao recarregar
const abertos = new Set();
document.addEventListener("toggle", (ev) => {
	const id = ev.target.dataset && ev.target.dataset.id;
	if (!id) return;
	if (ev.target.open) abertos.add(id); else abertos.delete(id);
}, true);
const aberto = id => abertos.has(id) ? "open" : "";

function renderIndicadores(mt) {
	if (mt.indicadores.length === 0) {
		return `<p class="text-sm opacity-60 px-2">Nenhuma medição registrada.</p>`;
	}
	return `<div class="overflow-x-auto"><table class="table table-xs">
		<thead><tr><th>Data</th><th>Valor</th><th>Observação</th><th></th></tr></thead>
		<tbody>${mt.indicadores.map(i => `<tr>
			<td>${dataBR(i.dataMedicao)}</td>
			<td>${num(i.valor)} ${esc(mt.unidade)}</td>
			<td>${esc(i.observacao)}</td>
			<td class="text-right whitespace-nowrap">${botao("Editar", "editarIndicador", i)}${botao("Excluir", "excluirIndicador", i, "btn-ghost text-error")}</td>
		</tr>`).join("")}</tbody></table></div>`;
}

function renderMetrica(mt) {
	// progresso = última medição / valor alvo
	const ultima = mt.indicadores.reduce((a, b) => (!a || b.dataMedicao >= a.dataMedicao) ? b : a, null);
	let progresso = `<span class="text-sm opacity-60">sem medição</span>`;
	if (ultima && mt.valorAlvo) {
		const pct = Math.round(ultima.valor / mt.valorAlvo * 100);
		const cor = pct >= 100 ? "progress-success" : pct >= 60 ? "progress-warning" : "progress-error";
		progresso = `<div class="flex items-center gap-2 w-56 max-w-full">
			<progress class="progress ${cor}" value="${Math.min(pct, 100)}" max="100"></progress>
			<span class="text-sm whitespace-nowrap">${pct}%</span></div>`;
	}
	return `<details class="collapse collapse-arrow bg-base-100 border border-base-300" data-id="mt${mt.codigo}" ${aberto("mt" + mt.codigo)}>
		<summary class="collapse-title">
			<div class="flex flex-wrap items-center justify-between gap-2 pr-4">
				<div>
					<span class="badge badge-ghost badge-sm mr-1">Métrica</span>
					<span class="font-medium">${esc(mt.nome)}</span>
					<span class="text-sm opacity-70">alvo: ${num(mt.valorAlvo)} ${esc(mt.unidade)}</span>
				</div>
				${progresso}
			</div>
		</summary>
		<div class="collapse-content">
			${mt.descricao ? `<p class="text-sm mb-2">${esc(mt.descricao)}</p>` : ""}
			<div class="flex flex-wrap gap-1 mb-2">
				${botao("+ Medição", "novoIndicador", mt, "btn-outline btn-primary")}
				${botao("Editar", "editarMetrica", mt)}
				${botao("Excluir", "excluirMetrica", mt, "btn-ghost text-error")}
			</div>
			${renderIndicadores(mt)}
		</div>
	</details>`;
}

function renderMeta(m, unica) {
	const dep = m.departamento;
	return `<details class="collapse collapse-arrow bg-base-200" data-id="m${m.codigo}" ${aberto("m" + m.codigo)}>
		<summary class="collapse-title">
			<span class="badge badge-secondary badge-sm mr-1">Meta</span>
			<span class="font-medium">${esc(m.descricao)}</span>
			<div class="flex flex-wrap gap-2 mt-1 text-sm">
				<span class="badge badge-outline badge-sm">${esc(dep.sigla || dep.nome)}</span>
				${m.prazo ? `<span class="opacity-70">até ${dataBR(m.prazo)}</span>` : ""}
				<span class="opacity-70">${m.metricas.length} métrica(s)</span>
			</div>
		</summary>
		<div class="collapse-content flex flex-col gap-2">
			<div class="flex flex-wrap gap-1">
				${botao("+ Métrica", "novaMetrica", m, "btn-outline btn-primary")}
				${botao("Editar", "editarMeta", m)}
				${unica ? `<span class="tooltip" data-tip="Todo objetivo precisa ter pelo menos uma meta"><button class="btn btn-xs btn-ghost" disabled>Excluir</button></span>`
						: botao("Excluir", "excluirMeta", m, "btn-ghost text-error")}
			</div>
			${m.metricas.length ? m.metricas.map(renderMetrica).join("") : `<p class="text-sm opacity-60">Nenhuma métrica.</p>`}
		</div>
	</details>`;
}

function renderObjetivo(o) {
	return `<div class="card bg-base-100 shadow">
		<div class="card-body p-5">
			<div class="flex flex-wrap items-start justify-between gap-2">
				<div>
					<span class="badge badge-primary badge-sm mb-1">Objetivo #${o.codigo}</span>
					<h3 class="card-title">${esc(o.titulo)}</h3>
					${o.descricao ? `<p class="text-sm opacity-80">${esc(o.descricao)}</p>` : ""}
				</div>
				<div class="flex gap-1">
					${botao("+ Meta", "novaMeta", o, "btn-outline btn-primary")}
					${botao("Editar", "editarObjetivo", o)}
					${botao("Excluir", "excluirObjetivo", o, "btn-ghost text-error")}
				</div>
			</div>
			<div class="flex flex-col gap-2 mt-2">
				${o.metas.map(m => renderMeta(m, o.metas.length === 1)).join("")}
			</div>
		</div>
	</div>`;
}

async function carregarDepartamentos() {
	departamentos = await api("GET", "/departamento");
	// contagem de metas por departamento (navegação Departamento -> Meta)
	const contagens = await Promise.all(departamentos.map(d => api("GET", "/departamento/" + d.codigo + "/metas")));
	document.getElementById("lista-departamentos").innerHTML = departamentos.length
		? departamentos.map((d, i) => `<tr>
			<td>${d.codigo}</td>
			<td class="font-medium">${esc(d.nome)}</td>
			<td>${esc(d.sigla)}</td>
			<td>${esc(d.responsavel)}</td>
			<td>${botao(contagens[i].length + " meta(s)", "verMetasDepartamento", d, "btn-link")}</td>
			<td class="text-right whitespace-nowrap">${botao("Editar", "editarDepartamento", d)}${botao("Excluir", "excluirDepartamento", d, "btn-ghost text-error")}</td>
		</tr>`).join("")
		: `<tr><td colspan="6" class="text-center opacity-60">Nenhum departamento cadastrado.</td></tr>`;
}

async function carregarObjetivos() {
	const objetivos = await api("GET", "/objetivo");
	document.getElementById("lista-objetivos").innerHTML = objetivos.length
		? objetivos.map(renderObjetivo).join("")
		: `<div class="alert">Nenhum objetivo cadastrado. ${departamentos.length ? "" : "Comece cadastrando um departamento na aba Departamentos."}</div>`;
}

async function recarregar() {
	registro = [];
	await carregarDepartamentos();
	await carregarObjetivos();
}

// ==================== abas e início ====================

document.querySelectorAll("[data-aba]").forEach(tab => tab.addEventListener("click", () => {
	document.querySelectorAll("[data-aba]").forEach(t => t.classList.toggle("tab-active", t === tab));
	document.getElementById("aba-objetivos").classList.toggle("hidden", tab.dataset.aba !== "objetivos");
	document.getElementById("aba-departamentos").classList.toggle("hidden", tab.dataset.aba !== "departamentos");
}));
document.getElementById("btn-novo-objetivo").addEventListener("click", novoObjetivo);
document.getElementById("btn-novo-departamento").addEventListener("click", novoDepartamento);

recarregar().catch(e => mensagem("Não foi possível falar com o backend: " + e.message, "error"));
