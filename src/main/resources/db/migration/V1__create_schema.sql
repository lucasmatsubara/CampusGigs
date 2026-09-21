-- V1: schema inicial da CampusGigs
-- Usuarios (alunos), gigs (servicos/freelas publicados) e contratacoes.

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    email      VARCHAR(160) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    cep        VARCHAR(9),
    street     VARCHAR(160),
    city       VARCHAR(120),
    state      VARCHAR(2),
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE gigs (
    id          BIGSERIAL PRIMARY KEY,
    provider_id BIGINT        NOT NULL REFERENCES users (id),
    title       VARCHAR(120)  NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category    VARCHAR(60)   NOT NULL,
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    status      VARCHAR(20)   NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'PAUSADO', 'ENCERRADO')),
    created_at  TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_gigs_provider ON gigs (provider_id);

CREATE TABLE hirings (
    id           BIGSERIAL PRIMARY KEY,
    gig_id       BIGINT      NOT NULL REFERENCES gigs (id),
    requester_id BIGINT      NOT NULL REFERENCES users (id),
    status       VARCHAR(20) NOT NULL DEFAULT 'SOLICITADA'
        CHECK (status IN ('SOLICITADA', 'ACEITA', 'CONCLUIDA', 'CANCELADA')),
    created_at   TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_hirings_gig ON hirings (gig_id);
CREATE INDEX idx_hirings_requester ON hirings (requester_id);
