# Registro de uso de IA

A ferramenta utilizada durante as interações foi o **Claude** (Anthropic), por meio do aplicativo. A IA foi utilizada como apoio ao desenvolvimento do projeto auxiliando na interpretação do enunciado e na elaboração de propostas de implementação. 

As sugestões geradas não foram adotadas automaticamente: cada proposta foi analisada pelo grupo, considerando sua adequação aos requisitos, às regras de negócio e à arquitetura definida para o sistema.

Após essa análise, as soluções consideradas adequadas foram implementadas, testadas e verificadas. Quando necessário, foram realizados ajustes para atender às necessidades do projeto.


---

## API

### Interação 1 - Modelo de domínio e mapeamento JPA (22/10)
- **Problema:** mapear Usuario, Projeto, HistoriaUsuario, Cenario, Passo e Sugestao conforme a seção 3, com listas ordenadas e restrições de unicidade.
- **Prompt:** Com base no enunciado do projeto anexado, implemente o modelo de domínio em Java utilizando Spring Data JPA, contemplando as entidades Usuario, Projeto, HistoriaUsuario, Cenario, Passo e Sugestao. Respeite os atributos, relacionamentos, cardinalidades e restrições de unicidade definidos na seção 3. Considere o uso de associações @ManyToOne com carregamento LAZY, coleções @OneToMany com cascade e orphanRemoval quando adequado, ordenação dos cenários e passos por posição e métodos para manter essa ordenação. 
- **Proposta da IA:** entidades com associações `@ManyToOne(LAZY)`, coleções `@OneToMany` com `cascade=ALL` e `orphanRemoval` para cenários, passos e sugestões, `@OrderBy("posicao")`, `uniqueConstraints` (e-mail, nome do projeto, título por projeto, título de cenário por história) e métodos de domínio para renumerar posições.
- **Decisão:** Aceita sem alterações, mantendo o modelo sugerido no enunciado. A estratégia LAZY foi mantida porque listas não precisam dos passos nem das sugestões.
- **Mudanças:** pacote `model` (6 entidades e 5 enums) e `repository`.
- **Verificação:** a aplicação subiu no meu PostgreSQL e criou as tabelas; os dados fictícios foram carregados.

### Interação 2 - Regra 2: ordem dos passos e autoria (03/11)
- **Problema:** validar Dado → Quando → Então, com E/Mas herdando o tipo anterior, e impedir edição por quem não é o autor.
- **Prompt:** Com base nas regras de negócio do enunciado, implemente as validações necessárias para garantir que os cenários sigam a sequência Dado, Quando e Então, considerando que os passos E e Mas devem herdar o tipo do passo anterior e não podem iniciar um cenário. Garanta também que somente o autor de uma história possa editá-la ou excluir seus dados. Organize as validações na camada de serviço, de forma que sejam aplicadas tanto na criação quanto na alteração de cenários e passos. Considere o uso de transações para evitar que alterações inválidas sejam persistidas e explique como as regras foram implementadas.
- **Proposta da IA:** classe `ValidadorCenario`, sem dependências do Spring e reaplicada a cada alteração de cenário ou passo individual (se falhar, a transação desfaz tudo); verificação de autoria em `HistoriaService.exigirAutor`.
- **Decisão:** Aceita, entendemos que a validação fica no serviço (e não na SPA) porque a regra precisa valer para qualquer cliente da API.
- **Mudanças:** `ValidadorCenario`, `CenarioService`, `HistoriaService`.
- **Verificação:** testes `ValidadorCenarioTest` e `RegrasDeNegocioTest` passando em `mvn test` (BUILD SUCCESS). Os casos pelo cliente HTTP estão no roteiro (`docs/verificacao.md`).

### Interação 3 - Erros padronizados, sugestões, exportação e arquitetura (03/11)
- **Problema:** respostas de erro coerentes, regras 4 a 7 (sugestões, exportação, consulta e resumo) e a regra "controller não acessa repositório".
- **Prompt:** Com base no enunciado do projeto, implemente o tratamento padronizado de erros da API e as funcionalidades relacionadas às sugestões de IA, exportação de histórias e consultas do projeto. Utilize ProblemDetail para estruturar as respostas de erro, considerando os códigos HTTP adequados para cada situação. Implemente as regras de negócio das sugestões, garantindo que uma sugestão já decidida não possa ser alterada, desenvolva a exportação das histórias no formato Gherkin com a identificação do idioma português e implemente as consultas necessárias para apresentar as histórias, suas quantidades de cenários, sugestões pendentes e o resumo geral do projeto. Por fim, crie um teste com ArchUnit para verificar se os controllers respeitam a arquitetura definida, sem acessar repositórios diretamente.
- **Proposta da IA:** `ApiExceptionHandler` com `ProblemDetail` (400, 401, 403, 404, 409, 422); `SugestaoService` (decisão final não pode ser alterada); `ExportacaoService` (Gherkin com `# language: pt`); `ConsultaProjetoService` com consultas agregadas; teste ArchUnit.
- **Decisão:** Aceita, a divisão: 409 para duplicidade ou sugestão já decidida, 422 para regra de negócio, 400 para validação de campos.
- **Mudanças:** `ApiExceptionHandler`, serviços, `ArquiteturaTest`, `WebConfig` (CORS só para `http://localhost:5173`).
- **Verificação:** `mvn test` com BUILD SUCCESS, incluindo as regras do ArchUnit; API respondendo em `http://localhost:8080/api/projetos`.

---

## SPA

### Interação 4 - Estrutura da SPA e consumo da API (09/11)
- **Problema:** criar a SPA com seleção de usuário, lista de projetos e histórias com filtro por prioridade.
- **Prompt:** Com base na parte 2 do enunciado do projeto, desenvolva a estrutura inicial da SPA utilizando React com Vite para consumir a API REST já implementada. Implemente a seleção do usuário de demonstração, a listagem de projetos e a visualização das histórias de cada projeto, incluindo o filtro por prioridade. Organize as telas em componentes separados e centralize as requisições HTTP em um arquivo api.js, garantindo que as chamadas enviem o cabeçalho X-Usuario-Id conforme esperado pela API. Mantenha as regras de negócio e suas validações no backend, deixando a SPA responsável pela interface, interação com o usuário e exibição das mensagens de erro retornadas pela API. Priorize uma estrutura simples, organizada e compatível com os requisitos do projeto.
- **Proposta da IA:** React com Vite; `api.js` concentra as chamadas e envia `X-Usuario-Id`; telas em arquivos separados; nenhuma regra de negócio na SPA, apenas a exibição das mensagens de erro da API.
- **Decisão:** Aceita, React por ser uma das opções do enunciado.
- **Mudanças:** pasta `frontend/` (`App`, `Projetos`, `ProjetoView`, `api.js`).
- **Verificação:** `npm install` e `npm run dev` funcionaram; entrei em `http://localhost:5173` e naveguei pelos projetos e histórias com dados reais da API.

### Interação 5 - Editor de história e cenários com passos ordenados (09/11)
- **Problema:** editar a história e os cenários, mantendo os passos na ordem.
- **Prompt:** Com base nos requisitos do enunciado do projeto, implemente a funcionalidade de edição de histórias e cenários na SPA. Desenvolva um componente CenarioEditor que permita visualizar e editar os passos de cada cenário, oferecendo opções para adicionar, remover e alterar a ordem dos passos por meio de controles para subir e descer. Ao salvar as alterações, envie a lista completa de passos para o endpoint PUT /cenarios/{id} da API. Mantenha as validações das regras de negócio no backend, utilizando as respostas da API para informar ao usuário sobre possíveis erros. Organize os componentes de forma que a edição dos cenários esteja integrada à visualização e edição da história, mantendo a interface simples e consistente com a estrutura existente.
- **Proposta da IA:** `CenarioEditor` com passos reordenáveis (subir/descer/remover) que envia a lista inteira ao `PUT /cenarios/{id}`; a validação da ordem fica no backend e o erro é mostrado na tela.
- **Decisão:** Aceita, pois a SPA não repete a regra para não divergir do backend.
- **Mudanças:** `CenarioEditor.jsx`, `HistoriaView.jsx`.
- **Verificação:** build de produção do frontend (`npm run build`) sem erros; uso pela tela. A verificação detalhada das regras (cenário inválido, ordem dos passos) será feita pelo roteiro.

### Interação 6 - Sugestões ao lado da história, exportação e resumo (10/11)
- **Problema:** registrar e decidir sugestões exibindo-as ao lado da história, exportar o `.feature` e mostrar o resumo do projeto.
- **Prompt:** Com base nos requisitos do enunciado do projeto, implemente na SPA as funcionalidades de gerenciamento das sugestões de IA, exportação de histórias e consulta do resumo do projeto. Crie um componente Sugestoes que permita registrar novas sugestões, visualizar seu conteúdo e realizar as decisões de aceitar ou rejeitar, exigindo o preenchimento do motivo nos casos de rejeição. Disponibilize essas funcionalidades junto à visualização da história. Implemente também um botão para exportar a história no formato .feature, permitindo visualizar o conteúdo gerado e realizar o download do arquivo. Por fim, adicione uma aba 'Resumo' em ProjetoView, apresentando os dados consolidados do projeto retornados pela API. Mantenha as regras de negócio e os bloqueios de operações no backend, exibindo na interface as mensagens de erro retornadas pela API. Integre as funcionalidades à estrutura existente, preservando a organização e a consistência visual da aplicação.
- **Proposta da IA:** componente `Sugestoes` (registrar, aceitar, rejeitar com motivo), botão de exportar que mostra e baixa o arquivo, e aba "Resumo" em `ProjetoView`.
- **Decisão:** Aceita, a exportação bloqueada aparece como mensagem da API.
- **Mudanças:** `Sugestoes.jsx`, `HistoriaView.jsx`, `ProjetoView.jsx`.
- **Verificação:** funcionamento básico pela tela; os bloqueios (sugestão pendente, rejeição sem motivo) estão no roteiro de verificação.

---