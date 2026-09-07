    CREATE TABLE servico (
        id                  INTEGER PRIMARY KEY AUTOINCREMENT,
        nome                VARCHAR(100) NOT NULL,
        descricao           VARCHAR(255) NOT NULL,
        duracao_minutos     INTEGER NOT NULL,
        preco               DECIMAL(4, 2) NOT NULL,
        ativo               BOOLEAN NOT NULL DEFAULT TRUE,

        CONSTRAINT uk_servico_nome UNIQUE (nome)
    );

