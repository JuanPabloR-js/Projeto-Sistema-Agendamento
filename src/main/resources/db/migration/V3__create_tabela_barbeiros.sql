CREATE TABLE barbeiros (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL,
    especialidade   VARCHAR(255) NOT NULL,
    ativo            BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_barbeiros_usuario
        UNIQUE (usuario_id),

    CONSTRAINT fk_barbeiros_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);