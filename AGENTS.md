# AGENTS.md - regras para assistentes de IA neste repositório

## Arquitetura (cinco camadas)
SPA → HTTP/JSON → `controller` → `service` → `repository` → banco. Entidades e enums em `model`.
- **controller**: recebe e valida DTOs (`@Valid`), delega ao serviço, devolve DTO/status. **Nunca importa `repository`.**
- **service**: todas as regras de negócio e as transações (`@Transactional`). Devolve DTOs (não entidades), porque `open-in-view=false`.
- **repository**: só interfaces Spring Data, sem regra de negócio.
- **model**: entidades JPA e enums. Não depende de nenhuma outra camada.
- **frontend/**: só apresentação. Não duplica regras; mostra as mensagens de erro da API.
- Dependências sempre por **construtor** (sem `@Autowired` em campo).

## Convenções
- Nomes de domínio em português. Mensagens de erro ao usuário em português.
- Associações `@ManyToOne` são `LAZY`; coleções são `LAZY` com `cascade=ALL, orphanRemoval=true` (composição).
- Erros: exceções de `exception/` → `ApiExceptionHandler` → `ProblemDetail` (401, 403, 404, 409, 422, 400).
  - 400 validação de campos / JSON · 401 sem `X-Usuario-Id` · 403 não é o autor · 404 inexistente
  - 409 duplicidade ou sugestão já decidida · 422 regra de negócio (ordem dos passos, exportação bloqueada, rejeição sem motivo)
- Identidade simulada pelo cabeçalho `X-Usuario-Id` (não é autenticação).
- Toda regra nova precisa de teste em `src/test` e deve passar em `mvn test`.

## Comandos
```
cd backend  && mvn test            # testes de regras + ArchUnit
cd backend  && mvn spring-boot:run # API em http://localhost:8080
cd frontend && npm install && npm run dev   # SPA em http://localhost:5173
```

## Lições e erros a evitar (atualizar quando a IA repetir um erro)
- Código gerado por IA só vale depois de `mvn test` (testes de regras + ArchUnit) e de uma execução real: a IA
  não conseguiu compilar o backend no ambiente dela, e a confirmação veio da máquina do aluno.
- Entidades JPA não saem do serviço: como `open-in-view=false`, o serviço monta os DTOs dentro da transação
  (evita `LazyInitializationException`).
- Não colocar regra de negócio na SPA: ela mostra as mensagens da API (400/401/403/404/409/422).
- Controllers não importam `repository` (o ArchUnit falha o build se isso acontecer).
- Nenhum erro repetido pela IA até agora; registrar aqui quando houver.
