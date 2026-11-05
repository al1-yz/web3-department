# Objetivos

Backend REST de planejamento estratégico. Um **Objetivo** é composto por **Metas**. Cada Meta pertence a um **Departamento** e é composta por **Métricas**. Cada Métrica é composta por **Indicadores** (as medições).

## Tecnologias

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC)
- Spring Data JPA / Hibernate 7
- SQLite (`sqlite-jdbc` + `hibernate-community-dialects`)
- Maven (via Maven Wrapper)

## Modelo (diagrama da aula)

```
 OBJETIVO ◆──────── 1..* META ◆──────── * MÉTRICA ◆──────> * INDICADOR
                          ▲
                          │ associação (sem cascade)
                     DEPARTAMENTO
```

| Relação | Tipo | Efeito no código |
|---|---|---|
| Objetivo ◆ Meta | Composição, 1..* | `@OneToMany(cascade = ALL, orphanRemoval = true)`. Não existe Objetivo sem Meta, e apagar o Objetivo apaga tudo abaixo dele |
| Meta ◆ Métrica | Composição, 0..* | Apagar a Meta apaga as Métricas e os Indicadores |
| Métrica ◆ Indicador | Composição, 0..* | Apagar a Métrica apaga os Indicadores |
| Departamento — Meta | Associação | `@ManyToOne` na Meta. O Departamento tem CRUD próprio e só pode ser apagado se não tiver metas |

Na composição, a parte é sempre criada a partir do todo (`POST /objetivo/{id}/meta`, `POST /meta/{id}/metrica`, `POST /metrica/{id}/indicador`). Não existe `POST /meta` solto.

### Atributos

| Entidade | Campos |
|---|---|
| Departamento | `codigo`, `nome` (obrigatório, único), `sigla`, `responsavel` |
| Objetivo | `codigo`, `titulo` (obrigatório), `descricao`, `metas` |
| Meta | `codigo`, `descricao` (obrigatório), `prazo` (data), `departamento` (obrigatório), `metricas` |
| Métrica | `codigo`, `nome` (obrigatório), `descricao`, `unidade` (ex.: `%`, `R$`), `valorAlvo`, `indicadores` |
| Indicador | `codigo`, `valor`, `dataMedicao` (obrigatório), `observacao` |

## Como rodar

```bash
./mvnw spring-boot:run
```
No Windows: `mvnw.cmd spring-boot:run`. Também dá para rodar a classe `ObjetivosApplication` pelo Eclipse/STS.

O banco `objetivos.db` é criado automaticamente na pasta do projeto, e as tabelas são geradas pelo Hibernate (`ddl-auto=update`). Para começar do zero, pare a aplicação e apague o arquivo `objetivos.db`.

A API sobe em `http://localhost:8080`.

## Frontend

Com a aplicação rodando, abra **http://localhost:8080/** no navegador. A página fica em `src/main/resources/static` (HTML, DaisyUI e Tailwind via CDN, JavaScript puro com `fetch`) e é servida pelo próprio Spring, sem precisar de CORS nem de outro servidor.

- **Departamentos:** cadastrar, editar e excluir. A coluna *Metas* abre as metas do departamento. Excluir um departamento com metas mostra o erro 409.
- **Objetivos:** a árvore Objetivo → Meta → Métrica → Indicador, com expansão por nível. O formulário de novo objetivo já pede a primeira meta, porque todo objetivo precisa de uma. O botão de excluir fica desativado na única meta de um objetivo.
- **Métricas:** a barra de progresso mostra a última medição em relação ao valor alvo.
- As mensagens de sucesso e de erro do backend aparecem no canto da tela.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/departamento` | Cria departamento |
| GET | `/departamento` | Lista departamentos |
| GET | `/departamento/{id}` | Busca departamento |
| GET | `/departamento/{id}/metas` | Metas do departamento |
| PUT | `/departamento/{id}` | Altera departamento |
| DELETE | `/departamento/{id}` | Remove (409 se tiver metas) |
| POST | `/objetivo` | Cria objetivo **com pelo menos uma meta** |
| GET | `/objetivo` | Lista objetivos (com toda a árvore) |
| GET | `/objetivo/{id}` | Busca objetivo |
| PUT | `/objetivo/{id}` | Altera título/descrição |
| DELETE | `/objetivo/{id}` | Remove objetivo e tudo abaixo |
| GET | `/objetivo/{id}/meta` | Metas do objetivo |
| POST | `/objetivo/{id}/meta` | Adiciona meta ao objetivo |
| GET | `/meta`, `/meta/{id}` | Lista / busca metas |
| PUT | `/meta/{id}` | Altera descrição, prazo e departamento |
| DELETE | `/meta/{id}` | Remove meta (409 se for a última do objetivo) |
| GET / POST | `/meta/{id}/metrica` | Lista / adiciona métricas da meta |
| GET | `/metrica`, `/metrica/{id}` | Lista / busca métricas |
| PUT / DELETE | `/metrica/{id}` | Altera / remove métrica |
| GET / POST | `/metrica/{id}/indicador` | Lista / adiciona indicadores da métrica |
| GET | `/indicador`, `/indicador/{id}` | Lista / busca indicadores |
| PUT / DELETE | `/indicador/{id}` | Altera / remove indicador |

Respostas de erro: `400` para dados inválidos, `404` quando o item não existe e `409` quando uma regra do diagrama impede a operação.

## Exemplos

Criar um departamento:
```json
POST /departamento
{ "nome": "Coordenação de Ensino", "sigla": "CE", "responsavel": "Maria" }
```

Criar um objetivo já com meta, métrica e indicador. O departamento é informado só pelo `codigo`:
```json
POST /objetivo
{
  "titulo": "Melhorar a aprovação",
  "descricao": "Aumentar a aprovação dos alunos",
  "metas": [
    {
      "descricao": "Reduzir reprovação em Web 3",
      "prazo": "2026-12-15",
      "departamento": { "codigo": 1 },
      "metricas": [
        {
          "nome": "Taxa de aprovação",
          "unidade": "%",
          "valorAlvo": 80,
          "indicadores": [ { "valor": 65, "dataMedicao": "2026-06-30" } ]
        }
      ]
    }
  ]
}
```

Adicionar um indicador (uma nova medição) a uma métrica:
```json
POST /metrica/1/indicador
{ "valor": 72, "dataMedicao": "2026-09-30", "observacao": "2º bimestre" }
```

## Estrutura

```
src/main/java/com/devcaotics/objetivos
├── ObjetivosApplication.java
├── ServletInitializer.java
├── controllers/          um RestController por entidade
├── model/entities/       Departamento, Objetivo, Meta, Metrica, Indicador
└── model/repositories/   interfaces JpaRepository
```
