## Executar no GitHub Codespaces (alternativa ao PC local)

O projeto também roda em um Codespace, abrindo apenas o navegador. Nele a API e a SPA ficam em endereços
públicos do tipo `https://<nome-do-codespace>-<porta>.app.github.dev`, e não em `localhost`; por isso há
alguns ajustes de CORS, de URL da API e de visibilidade das portas.

### Passo a passo
1. **Criar:** no repositório, **Code > Codespaces > Create codespace on main**. A cota gratuita é mensal
   e limitada: pare o Codespace ao terminar.
2. **Java 21:** confira com `java -version`. Se não for 17–21, troque (ver "Versão do Java" abaixo).
3. **Instalar e iniciar o PostgreSQL** (o Codespace padrão não o traz):
  > Execute uma linha de cada vez
   ```bash
   sudo apt-get update && sudo apt-get install -y postgresql
   sudo service postgresql start
   sudo runuser -u postgres -- psql -c "CREATE USER estudiobdd WITH PASSWORD 'estudiobdd';"
   sudo runuser -u postgres -- psql -c "CREATE DATABASE estudiobdd OWNER estudiobdd;"
   ```
4. **API** (terminal 1). Libere o CORS para o endereço da SPA e rode:
   ```bash
   cd backend
   export CORS_ORIGIN=https://$CODESPACE_NAME-5173.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN
   mvn test
   mvn spring-boot:run
   ```
5. **SPA** (terminal 2), apontando para a API do Codespace:
   ```bash
   cd frontend
   npm install
   VITE_API_URL=https://$CODESPACE_NAME-8080.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN/api npm run dev
   ```
6. **Liberar a porta da API:** na aba **Ports**, clique com o botão direito na porta **8080** >
   **Port Visibility > Public**.
7. **Abrir a SPA:** na aba **Ports**, clique no ícone de globo da porta **5173**, escolha um usuário e navegue.
   Para testar a API direto, abra `https://<codespace>-8080.app.github.dev/api/projetos` ou use o `docs/testes.http`
   trocando a variável `@base` pelo endereço da porta 8080.

Para ver os endereços do seu Codespace: `echo https://$CODESPACE_NAME-8080.$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN`
(troque `8080` por `5173` para o da SPA).

### Problemas comuns e soluções

**Versão do Java.** Se o `mvn test` falhar com `MockitoException: Could not modify all classes`, o Codespace
está com um JDK mais novo do que o suportado pelas bibliotecas de teste. Use o Java 21 com o SDKMAN:

```bash
sdk list java | grep "21\."          # liste as versões do Java 21
sdk install java <identificador>     # ex.: 21.0.x-tem (use o identificador exato da lista)
sdk default java <identificador>     # padrão para novos terminais
sdk use java <identificador>         # ou só para o terminal atual
java -version                        # confirme a versão ativa
```
> Execute uma linha de cada vez

**Banco de dados**
- `[sudo] password for codespace` ao criar o usuário: não use `sudo -u postgres`; use
  `sudo runuser -u postgres -- psql ...` como no passo 3. Não existe senha para digitar.
- `role "estudiobdd" already exists` / `database ... already exists`: o comando já foi executado; pule-o.
- `Connection refused` ao subir a API, ou após parar e reabrir o Codespace: o PostgreSQL não está
  rodando. Execute `sudo service postgresql start`.
- Os dados persistem enquanto o Codespace existir. Um Codespace **novo** começa com banco vazio e recebe
  os dados fictícios na primeira execução da API.

**Porta da API e CORS**
- A SPA mostra "Não foi possível conectar à API": a porta **8080** não está **Public** (passo 6), a API não
  está rodando, ou a SPA foi iniciada sem `VITE_API_URL`.
- Erro de CORS no console do navegador: o `CORS_ORIGIN` precisa ser exportado **no mesmo terminal e antes**
  de `mvn spring-boot:run`, e ser exatamente o endereço da porta 5173. Se o nome do Codespace mudou,
  exporte de novo e reinicie a API.
- A porta 8080 pública fica acessível pela internet enquanto o Codespace roda. Use só para demonstração
  e pare o Codespace depois.

**SPA**
- `Blocked request. This host is not allowed`: o `frontend/vite.config.js` precisa de
  `host: true` e `allowedHosts: ['.app.github.dev']` (já incluídos neste repositório).
- A SPA continua chamando `localhost:8080`: reinicie o `npm run dev` com `VITE_API_URL=...` na mesma linha
  do comando (passo 5).
- `Port 5173 is already in use`: já existe uma instância rodando; feche-a (Ctrl+C no terminal dela).

**Geral**
- Para economizar a cota: **Codespaces > Stop codespace** ao terminar. Ao voltar, rode de novo os passos
  `sudo service postgresql start`, 4 e 5 (sem `npm install`).
