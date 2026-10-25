-- DECIMAL(4, 2) só aceitava até 99,99; o ServicoRequest permite até 1000,00.
ALTER TABLE servico
    ALTER COLUMN preco TYPE DECIMAL(6, 2);
