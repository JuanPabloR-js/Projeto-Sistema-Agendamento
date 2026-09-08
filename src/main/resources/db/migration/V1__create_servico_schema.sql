    CREATE TABLE servico (
        id                  BIGSERIAL PRIMARY KEY,
        nome                VARCHAR(100) NOT NULL,
        descricao           VARCHAR(255) NOT NULL,
        duracao_minutos     INT NOT NULL,
        preco               DECIMAL(4, 2) NOT NULL,
        ativo               BOOLEAN NOT NULL DEFAULT TRUE,

        CONSTRAINT uk_servico_nome UNIQUE (nome)
    );

