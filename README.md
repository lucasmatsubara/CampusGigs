# CampusGigs

API REST de freelas entre alunos de uma universidade. Um aluno se cadastra,
publica um serviço (freela) e outro aluno, autenticado, contrata esse serviço.

Projeto do desafio **Java Advanced — Projeto Diamante 1**.

## Stack

- Java 17 + Spring Boot
- Spring Web (Spring MVC)
- Spring Data JPA + PostgreSQL
- Flyway (versionamento de schema)
- Docker (Postgres sobe automaticamente via `spring-boot-docker-compose`)
- Gradle

## Status do projeto (checkpoints)

- [x] **CP1** — Ambiente sobe via Docker; primeira migration com o schema inicial
- [x] **CP2** — Cadastro e autenticação (senha protegida)
- [x] **CP3** — Emissão e validação de token JWT nos endpoints protegidos
- [x] **CP4** — Regras de autorização por papel (ADMIN / USER)
- [ ] CP5 — Integração com serviço externo (CEP) e revisão final

## Como rodar (CP1)

Pré-requisitos: JDK 17+ e Docker rodando.

```bash
./gradlew bootRun
```

Ao subir, o Spring Boot detecta o `compose.yaml` na raiz do projeto e
sobe automaticamente um container Postgres (você não precisa instalar
nem configurar o banco manualmente). Na inicialização, o Flyway aplica a
migration `V1__create_schema.sql`, criando as tabelas `users`, `gigs` e
`hirings`.

Para conferir que o schema subiu:

```bash
docker exec -it postgres_campus_gigs psql -U campusgigs -d campusgigs -c "\dt"
```

## Modelo de domínio (schema inicial)

- **users** — aluno cadastrado (nome, e-mail único, senha, papel
  `USER`/`ADMIN`, endereço obtido via CEP)
- **gigs** — o freela publicado por um usuário (`provider_id`): título,
  descrição, categoria, preço e situação (`ATIVO`, `PAUSADO`, `ENCERRADO`)
- **hirings** — a contratação de um gig por outro usuário
  (`requester_id`): situação (`SOLICITADA`, `ACEITA`, `CONCLUIDA`,
  `CANCELADA`)

## Cadastro e login (CP2)

A senha nunca é salva em texto puro: no cadastro ela é protegida com
BCrypt (`PasswordEncoder`) antes de ir para o banco. O login usa o
`AuthenticationManager` do Spring Security, que busca o usuário pelo
e-mail e compara a senha informada com o hash salvo — se não bater,
retorna erro automaticamente. Ainda não há emissão de token; isso é
o CP3.

**Cadastrar usuário**

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Lucas","email":"lucas@fiap.com.br","password":"senha123"}'
```

**Login (agora retorna um JWT)**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"lucas@fiap.com.br","password":"senha123"}'
```

Resposta:

```json
{
  "id": 1,
  "name": "Lucas",
  "email": "lucas@fiap.com.br",
  "role": "USER",
  "token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

## Autenticação JWT (CP3)

O token é assinado com um par de chaves RSA (RS256), gerado em memória a
cada subida da aplicação — não depende de arquivo de chave versionado no
repositório. O login (acima) emite o token; qualquer outro endpoint além
de `POST /users` e `POST /auth/login` agora exige esse token no header
`Authorization`.

**Chamando um endpoint protegido**

```bash
curl http://localhost:8080/users/me \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..."
```

Sem o header (ou com um token inválido/expirado), a API responde
`401 Unauthorized`.

## Gigs e contratações (CP4)

Regras de autorização:

- **Publicar um gig** (`POST /gigs`) — qualquer usuário autenticado
- **Listar gigs** (`GET /gigs`, `GET /gigs/{id}`) — público, não exige token
- **Contratar um gig** (`POST /hirings`) — qualquer usuário autenticado,
  desde que o gig esteja `ATIVO` e **não seja o próprio usuário o dono do
  gig** (senão, `409 Conflict`)
- **Encerrar um gig** (`PATCH /gigs/{id}/close`) — só o **dono** do gig ou
  um usuário **ADMIN** (senão, `403 Forbidden`)

Todo cadastro novo nasce com papel `USER`. Para testar a regra do ADMIN,
promova um usuário direto no banco:

```bash
docker exec -it postgres_campus_gigs psql -U campusgigs -d campusgigs \
  -c "UPDATE users SET role = 'ADMIN' WHERE email = 'lucas@fiap.com.br';"
```

**Publicar um gig**

```bash
curl -X POST http://localhost:8080/gigs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"Aula de Java","description":"Reforço para prova","category":"Aulas","price":50.00}'
```

**Listar gigs (sem token)**

```bash
curl http://localhost:8080/gigs
```

**Contratar um gig**

```bash
curl -X POST http://localhost:8080/hirings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-de-outro-usuario>" \
  -d '{"gigId":1}'
```

**Encerrar um gig que não é seu (evidência de acesso negado por papel)**

```bash
curl -X PATCH http://localhost:8080/gigs/1/close \
  -H "Authorization: Bearer <token-de-quem-nao-e-dono-nem-admin>"
```

Resposta esperada — `403 Forbidden`:

```json
{
  "timestamp": "2026-09-21T02:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Voce nao tem permissao para realizar esta acao",
  "path": "/gigs/1/close"
}
```

Todo erro da API (validação, autorização, regra de negócio, recurso não
encontrado) passa pelo `GlobalExceptionHandler` e volta nesse formato —
nunca com stack trace.

> A integração com o serviço de CEP e a revisão final chegam no próximo
> checkpoint.
