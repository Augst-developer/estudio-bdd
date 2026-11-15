# Roteiro de verificação (seção 13)

Pré-requisito: API no ar com os dados fictícios. Ids: usuário 1 = Ana, 2 = Bruno; projeto 1 = Loja Virtual;
história 1 (pronta para exportar, autora Ana), 2 (sugestão pendente, Ana), 3 (autor Bruno, sem cenários).



| # | Caso | Entrada | Esperado | Observado | Captura |
|---|---|---|---|---|---|
| 1 | História válida | `curl -i -X POST $B/projetos/1/historias -H "$H" -H "X-Usuario-Id: 1" -d '{"titulo":"Nova","papel":"cliente","objetivo":"x","beneficio":"y","prioridade":"ALTA"}'` | 201 | 201 Created. História criada com id 4, autor Ana, prioridade ALTA. | [print](evidencias/1-historia-valida.png) |
| 2 | Título repetido no projeto | mesma chamada de novo | 409 | 409. "Já existe uma história com o título … neste projeto." | [print](evidencias/2-titulo-repetido-no-projeto.png) |
| 3 | Narrativa incompleta | mesmo POST sem `beneficio` | 400, `erros.beneficio` | 400. erros.beneficio: "O benefício (para...) é obrigatório." | [print](evidencias/3-narrativa-incompleta.png) |
| 4 | Cenário válido | POST `/historias/3/cenarios` com X-Usuario-Id 2 e passos Dado/Quando/Então | 201, passos com posição 1..3 | 201. Cenário com 3 passos, posições 1, 2 e 3. | [print](evidencias/4-cenario-valido.png) |
| 5 | Sem Quando | passos Dado, Então | 422 "…ao menos um passo Quando" | 422. "O cenário deve ter ao menos um passo Quando." | [print](evidencias/5-sem-quando.png) |
| 6 | Sem Então | passos Dado, Quando | 422 "…ao menos um passo Então" | 422. "O cenário deve ter ao menos um passo Então." | [print](evidencias/6-sem-entao.png) |
| 7 | Fora de ordem | passos Quando, Dado, Então | 422 "fora de ordem" | 422. "Passos fora de ordem na posição 2: a sequência deve ser Dado → Quando → Então." | [print](evidencias/7-fora-de-ordem.png) |
| 8 | Iniciado por E | passos E, Quando, Então | 422 "não pode começar" | 422. "O cenário não pode começar com um passo E: use Dado ou Quando." | [print](evidencias/8-iniciado-por-E.png) |
| 9 | Ordem após edição | `PUT /cenarios/{id}` invertendo/alterando passos; `GET /cenarios/{id}` | passos na ordem enviada, posições 1..n | 9a: 200. 9b: 200, 5 passos na ordem enviada (Dado, E, Quando, Então, E), posições 1 a 5. | [print](evidencias/9-ordem-apos-edicao.png) |
| 10 | Edição por não autor | `PUT /historias/1` com X-Usuario-Id 2 | 403 | 403. "Somente o autor da história pode editá-la ou excluí-la, ou alterar seus cenários." A história 1 não foi alterada. | [print](evidencias/10-edicao-por-nao-autor.png) |
| 11 | Exclusão por não autor | `DELETE /historias/1` e `DELETE /cenarios/{id}` com X-Usuario-Id 2 | 403 | 11a e 11b: 403, mesma mensagem do caso 10. História 1 e cenário 1 continuam existindo. | [print](evidencias/11-exclusao-por-nao-autor.png) |
| 12 | Sem identidade | `PUT /historias/1` sem cabeçalho | 401 | 401. "Informe o usuário atual no cabeçalho X-Usuario-Id." | [print](evidencias/12-sem-identidade.png) |
| 13 | Registrar sugestão | `POST /historias/3/sugestoes` `{ferramenta,prompt,conteudo}` | 201, decisao PENDENTE | 201. Sugestão criada com id X, decisao PENDENTE, registradaPor Bruno. | [print](evidencias/13-registrar-sugestao.png) |
| 14 | Aceitar | `POST /sugestoes/{id}/aceitar` | 200, ACEITA; cenários inalterados | 200. decisao ACEITA, decididaPor Bruno, justificativa registrada. Os cenários da história 3 não mudaram. | [print](evidencias/14-aceitar.png) |
| 15 | Rejeitar com motivo | `POST /sugestoes/{id}/rejeitar` `{"motivo":"GENERICA"}` | 200, REJEITADA | 15a: 201 (PENDENTE). 15b: 200, decisao REJEITADA, motivoRejeicao GENERICA. | [print](evidencias/15-rejeitar-com-motivo.png) |
| 16 | Rejeitar sem motivo | `{"justificativa":"x"}` | 400 | 400. "Um ou mais campos são inválidos." erros.motivo: "A rejeição exige um motivo (…)". A sugestão continua PENDENTE. | [print](evidencias/16-rejeitar-sem-motivo.png) |
| 17 | Alterar sugestão decidida | aceitar/rejeitar de novo a mesma | 409 | 409. "A sugestão 4 já foi decidida (ACEITA) e não pode ser alterada." | [print](evidencias/17-alterar-sugestao-decidida.png) |
| 18 | Exportação sem cenários | `GET /historias/3/feature` (sem cenários) | 422 | 422. "A história não pode ser exportada: não possui nenhum cenário." | [print](evidencias/18-exportacao-sem-cenarios.png) |
| 19 | Exportação com pendente | `GET /historias/2/feature` | 422 "pendente(s)" | 422. "A história não pode ser exportada: possui 1 sugestão(ões) pendente(s)." | [print](evidencias/19-exportacao-com-pendente.png) |
| 20 | Exportação permitida | `GET /historias/1/feature` | 200, conteúdo igual a `docs/exemplo.feature` | 200, texto começando com "# language: pt". Conteúdo igual ao docs/exemplo.feature (2 cenários). | [print](evidencias/20-exportacao-permitida.png) |
| 21 | Resumo | `GET /projetos/1/resumo` e conferir com a consulta | totais coerentes com os dados | 200. totalHistorias 4, totalCenarios 4; cenariosPorOrigem HUMANO 3 e SUGESTAO_IA 1; sugestoesPorDecisao ACEITA 2, REJEITADA 2, PENDENTE 2; motivo mais frequente GENERICA com 2. | [print](evidencias/21-resumo.png) |
| 22 | Consulta com filtro | `GET /projetos/1/historias?prioridade=ALTA` | só histórias ALTA, com contagens | 22a: 200, 4 histórias com a quantidade de cenários e de pendentes (história 1: 2 e 0; história 2: 1 e 1; história 3: 1 e 1; a nova: 0 e 0). 22b: 200, só as de prioridade ALTA (história 1 e a nova). | [print](evidencias/22-consulta-com-filtro.png) |
| 23 | Persistência | reiniciar a API e repetir `GET /projetos/1/historias` | dados mantidos, sem recarregar a carga inicial | Após parar e subir a API de novo, a consulta retornou as mesmas 4 histórias, e os dados iniciais não foram duplicados. | [print](evidencias/23-persistencia.png) |
| 24 | `mvn test` | `cd backend && mvn test` | todos os testes passam | BUILD SUCCESS. Tests run: 20, Failures: 0, Errors: 0. | [print](evidencias/24-mvn-test.png) |
