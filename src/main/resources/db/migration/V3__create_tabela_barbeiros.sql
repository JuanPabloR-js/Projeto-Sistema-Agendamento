CREATE TABLE barbeiros (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL,

    CONSTRAINT fk_barbeiros_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);