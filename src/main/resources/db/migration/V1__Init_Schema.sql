-- Flyway V1 initial schema
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    full_name       VARCHAR(150),
    bio             TEXT,
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tasks (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    description  TEXT,
    budget       NUMERIC(12,2) NOT NULL,
    status       VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_by   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bids (
    id             BIGSERIAL PRIMARY KEY,
    task_id        BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    freelancer_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount         NUMERIC(12,2) NOT NULL,
    status         VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMPTZ DEFAULT now(),
    UNIQUE(task_id, freelancer_id)
);

CREATE TABLE IF NOT EXISTS milestones (
    id         BIGSERIAL PRIMARY KEY,
    task_id    BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    title      VARCHAR(200) NOT NULL,
    status     VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS messages (
    id         BIGSERIAL PRIMARY KEY,
    task_id    BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    sender_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payments (
    id               BIGSERIAL PRIMARY KEY,
    task_id          BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    amount           NUMERIC(12,2) NOT NULL,
    stripe_payment_id VARCHAR(100),
    status           VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at       TIMESTAMPTZ DEFAULT now()
);
