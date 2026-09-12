CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    telefone        VARCHAR(20),
    senha           VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL,

    CONSTRAINT uk_usuarios_email UNIQUE (email),

    CONSTRAINT ck_usuarios_role
        CHECK (role IN ('CLIENTE', 'BARBEIRO', 'ADMIN'))
);