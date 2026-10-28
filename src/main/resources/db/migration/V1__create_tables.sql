CREATE TABLE support_sectors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    acronym VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    support_sector_id BIGINT NOT NULL REFERENCES support_sectors(id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    registration VARCHAR(30) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('SOLICITANTE', 'TECNICO')),
    support_sector_id BIGINT REFERENCES support_sectors(id),
    CONSTRAINT technician_sector_required CHECK (type <> 'TECNICO' OR support_sector_id IS NOT NULL)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(200) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('BAIXA', 'MEDIA', 'ALTA')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('ABERTO', 'EM_ATENDIMENTO', 'RESOLVIDO', 'CANCELADO')),
    opened_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    solution VARCHAR(2000),
    requester_id BIGINT NOT NULL REFERENCES users(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    assigned_technician_id BIGINT REFERENCES users(id)
);

CREATE TABLE interactions (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    author_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_categories_sector ON categories(support_sector_id);
CREATE INDEX idx_users_sector ON users(support_sector_id);
CREATE INDEX idx_tickets_filters ON tickets(status, priority, opened_at);
CREATE INDEX idx_tickets_category ON tickets(category_id);
CREATE INDEX idx_tickets_requester ON tickets(requester_id);
CREATE INDEX idx_tickets_technician ON tickets(assigned_technician_id);
CREATE INDEX idx_interactions_ticket_time ON interactions(ticket_id, created_at);
