# Estúdio BDD

Aplicação para documentar requisitos: histórias de usuário com cenários BDD (Dado/Quando/Então),
registro e decisão de sugestões de IA, exportação `.feature` e resumo do projeto.

_Projeto da disciplina Programação de Sistemas II (2026/2)_


## Integrantes do Grupo

* **José Augusto Serra Mota** - RA: 10750142
* **Pedro Henrique da Silva Araujo** - RA: 10442579
* **Pedro Henrique Germano Kihara** - RA: 10737627


## Estrutura
```
backend/   API REST (Spring Boot 3, Spring Data JPA, PostgreSQL, Bean Validation)
frontend/  SPA (React + Vite)
docs/      diagrama de classes, roteiro de verificação, registro de IA, exemplo.feature
AGENTS.md  regras de arquitetura e comandos para assistentes de IA
```
Camadas: SPA → controller → service → repository → model (ver `docs/diagrama-classes.md`).

## Pré-requisitos
Java 17+ (testado com 21), Maven 3.9+, Node 18+ e PostgreSQL 14+.

## Como executar
### 1. Banco (PostgreSQL)
No `psql` como administrador:
```sql
CREATE USER estudiobdd WITH PASSWORD 'estudiobdd';
CREATE DATABASE estudiobdd OWNER estudiobdd;
```
Valores padrão (`application.properties`) podem ser trocados por variáveis de ambiente:
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGIN`. As tabelas são criadas automaticamente na primeira
execução e **os dados persistem entre reinícios**. Os dados fictícios (2 usuários, 2 projetos, 3 histórias)
são carregados somente se o banco estiver vazio.

### 2. API
```
cd backend
mvn test              # testes de regras + ArchUnit
mvn spring-boot:run   # http://localhost:8080
```
### 3. SPA
```
cd frontend
npm install
npm run dev           # http://localhost:5173
```
A SPA usa `http://localhost:8080/api` (altere com `VITE_API_URL`). O CORS da API aceita **somente** a origem `http://localhost:5173`.

## Identidade simulada e limitações
A API identifica o usuário pelo cabeçalho `X-Usuario-Id` e os serviços validam a autoria. **Isso não é
autenticação**: não há senha nem token, e qualquer cliente pode enviar o id de outro usuário. Serve apenas
para demonstrar a regra de autoria. Leituras (GET) não exigem o cabeçalho; criações, edições, exclusões e
decisões de sugestões exigem (401 se ausente ou inválido).

Decisões de modelagem: registrar e decidir sugestões é permitido a qualquer usuário identificado (o enunciado
restringe só a história e os cenários ao autor); um projeto só pode ser excluído se não tiver histórias;
comparações de título/nome/e-mail ignoram maiúsculas/minúsculas.

## Endpoints
Erros seguem `ProblemDetail`: 400 (campos inválidos, com `erros` por campo), 401, 403, 404, 409, 422.
Criações retornam 201 + `Location`; exclusões, 204.

| Método e caminho | Descrição | Usuário |
|---|---|---|
| `GET /api/usuarios` · `POST /api/usuarios` | listar / cadastrar (e-mail único) | — |
| `GET /api/projetos` · `POST /api/projetos` | listar / criar (nome único) | POST |
| `GET/PUT/DELETE /api/projetos/{id}` | consultar / editar / excluir | PUT, DELETE |
| `GET /api/projetos/{id}/historias?prioridade=ALTA` | **consulta**: histórias com qtd. de cenários e de sugestões pendentes; filtro opcional | — |
| `POST /api/projetos/{id}/historias` | criar história (autor = usuário atual) | sim |
| `GET /api/projetos/{id}/resumo` | **resumo**: histórias, cenários por origem, sugestões por decisão, motivos de rejeição mais frequentes | — |
| `GET/PUT/DELETE /api/historias/{id}` | consultar (com cenários e passos) / editar / excluir | só autor (PUT, DELETE) |
| `GET /api/historias/{id}/cenarios` · `POST …/cenarios` | listar / criar cenário (corpo com `passos`) | POST só autor |
| `GET/PUT/DELETE /api/cenarios/{id}` | consultar / substituir (título, origem, posição, passos) / excluir | só autor |
| `POST /api/cenarios/{id}/passos` | adicionar passo (`posicao` opcional) | só autor |
| `PUT/DELETE /api/passos/{id}` | editar (tipo, texto, `posicao`) / excluir passo | só autor |
| `GET /api/historias/{id}/sugestoes?decisao=PENDENTE` | listar sugestões (filtro opcional) | — |
| `POST /api/historias/{id}/sugestoes` | registrar `{ferramenta, prompt, conteudo}` | sim |
| `POST /api/sugestoes/{id}/aceitar` | aceitar `{justificativa}` (não altera cenários) | sim |
| `POST /api/sugestoes/{id}/rejeitar` | rejeitar `{motivo, justificativa}` (motivo obrigatório) | sim |
| `GET /api/historias/{id}/feature` | exporta `.feature` (`text/plain`) se houver cenários e nenhuma sugestão pendente | — |

Exemplo de cenário:
```json
POST /api/historias/1/cenarios      X-Usuario-Id: 1
{ "titulo": "Pagamento recusado", "origem": "SUGESTAO_IA",
  "passos": [ {"tipo":"DADO","texto":"que tenho itens no carrinho"},
              {"tipo":"QUANDO","texto":"o pagamento é recusado"},
              {"tipo":"ENTAO","texto":"o pedido não é criado"} ] }
```

## Entrega
Roteiro e evidências: `docs/verificacao.md`. Registro de IA: `docs/registro-ia.md`.
Arquivo exportado: `docs/exemplo.feature`. Bônus (fluxo configurável): **não implementado**.
