CREATE TABLE tb_agendamento(
    id               BIGSERIAL PRIMARY KEY,
    cliente_id       BIGINT      NOT NULL,
    barbeiro_id      BIGINT      NOT NULL,
    servico_id       BIGINT      NOT NULL,
    data_hora_inicio TIMESTAMP   NOT NULL,
    data_hora_fim    TIMESTAMP   NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    observacao       VARCHAR(500),
    data_criacao     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_agendamento_cliente
        FOREIGN KEY (cliente_id)
            REFERENCES tb_cliente (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_agendamento_barbeiro
        FOREIGN KEY (barbeiro_id)
            REFERENCES barbeiros (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_agendamento_servico
        FOREIGN KEY (servico_id)
            REFERENCES servico (id)
            ON DELETE RESTRICT,

    CONSTRAINT ck_agendamento_status
        CHECK (
            status IN (
                       'AGENDADO',
                       'CONFIRMADO',
                       'CANCELADO',
                       'CONCLUIDO'
                )
            ),

    CONSTRAINT ck_agendamento_periodo
        CHECK (data_hora_fim > data_hora_inicio)
);

CREATE INDEX idx_agendamento_barbeiro_inicio
    ON tb_agendamento (barbeiro_id, data_hora_inicio);

CREATE INDEX idx_agendamento_cliente_inicio
    ON tb_agendamento (cliente_id, data_hora_inicio);

CREATE INDEX idx_agendamento_servico
    ON tb_agendamento (servico_id);