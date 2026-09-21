CREATE TABLE tb_cliente
(
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT      NOT NULL,
    telefone   VARCHAR(20) NOT NULL,

    CONSTRAINT uk_cliente_usuario UNIQUE (usuario_id),

    CONSTRAINT fk_cliente_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES tb_usuario (id)
);