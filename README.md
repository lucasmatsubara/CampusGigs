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
- [ ] CP3 — Emissão e validação de token JWT nos endpoints protegidos
- [ ] CP4 — Regras de autorização por papel (ADMIN / USER)
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

**Login (confirma as credenciais, sem token ainda)**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"lucas@fiap.com.br","password":"senha123"}'
```

> As entidades de Gig/Contratação, a emissão de JWT, as regras de
> autorização por papel e a integração com CEP chegam nos próximos
> checkpoints.
