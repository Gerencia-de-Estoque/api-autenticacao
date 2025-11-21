# API de Autenticação

Serviço responsável por autenticar filiais/usuários e emitir tokens JWT. Ele reaproveita o mesmo MySQL usado pela API principal da loja.

## Pré‑requisitos

- Docker e Docker Compose v2
- API da loja (`../API-LOJA-DE-CONSTRU-O`) rodando para fornecer o MySQL exposto em `localhost:3307`

## Variáveis principais

| Variável | Descrição | Padrão |
| --- | --- | --- |
| `DB_HOST` | Host do MySQL compartilhado | `host.docker.internal` |
| `DB_PORT` | Porta externa exposta pelo MySQL | `3307` |
| `PORT` | Porta publicada do serviço | `8089` |

> Se estiver em Linux nativo e o host `host.docker.internal` não existir, sobrescreva `DB_HOST` com o IP da máquina.

## Como executar

```bash
cd api-autentic
DB_HOST=host.docker.internal \
DB_PORT=3307 \
docker compose up -d --build
```

- API: http://localhost:8089
- Adminer (apontado para o mesmo banco): http://localhost:8081

Para interromper:

```bash
docker compose down
```

## Testes e Lint

O projeto inclui checkstyle/PMD/SpotBugs **e** os testes unitários. Rode tudo junto (com saída detalhada de cada teste):

```bash
cd api-autentic/springboot/demo
./mvnw -Dstyle.color=always \
       -Dsurefire.reportFormat=plain \
       -Dsurefire.printSummary=true \
       -Dsurefire.useFile=false \
       verify
```

Esse comando executa os plugins, compila e roda os testes. Falha se houver violações ou testes quebrados.

## Endpoints

| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/auth/login` | Autentica uma filial (login/senha) e retorna `token`, `tipo`, `expiraEm`. |

Envie o JSON `{"login":"...","senha":"..."}` usando um usuário criado via API da loja.
