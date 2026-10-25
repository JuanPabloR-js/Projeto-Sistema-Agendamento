package br.unisales.sistema_agendamento.servico;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.unisales.sistema_agendamento.servico.dto.ServicoRequest;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @apiNote Testa as regras de validação de cada campo do ServicoRequest.
 * Os limites de tamanho e preço seguem a tabela "servico" (migration V1)
 * e a regra do agendamento, que exige duração positiva.
 * @author Mateus Alves Costa
 * @since 25.10.2026
 */
class ServicoRequestValidationTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void criarValidador() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        fabrica.close();
    }

    private ServicoRequest valido() {
        return new ServicoRequest(
                "Corte masculino",
                "Corte com máquina e tesoura",
                30,
                new BigDecimal("45.00"),
                true
        );
    }

    private ServicoRequest com(String nome, String descricao, Integer duracao,
                               BigDecimal preco, Boolean ativo) {
        return new ServicoRequest(nome, descricao, duracao, preco, ativo);
    }

    // Garante que houve erro exatamente no campo esperado.
    private void assertErroNoCampo(ServicoRequest request, String campo) {
        Set<ConstraintViolation<ServicoRequest>> erros = validador.validate(request);

        assertEquals(1, erros.size(), () -> "Erros encontrados: " + erros);
        assertEquals(campo, erros.iterator().next().getPropertyPath().toString());
    }

    @Test
    void deveAceitarRequisicaoValida() {
        assertTrue(validador.validate(valido()).isEmpty());
    }

    // ----------------------------------------------------------------- nome

    @Test
    void deveRejeitarNomeNulo() {
        assertErroNoCampo(com(null, "Descrição", 30, BigDecimal.TEN, true), "nome");
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        assertErroNoCampo(com("   ", "Descrição", 30, BigDecimal.TEN, true), "nome");
    }

    // A coluna nome é VARCHAR(100).
    @Test
    void deveRejeitarNomeComMaisDe100Caracteres() {
        assertErroNoCampo(
                com("a".repeat(101), "Descrição", 30, BigDecimal.TEN, true),
                "nome"
        );
    }

    // ------------------------------------------------------------ descricao

    @Test
    void deveRejeitarDescricaoEmBranco() {
        assertErroNoCampo(com("Corte", "", 30, BigDecimal.TEN, true), "descricao");
    }

    // A coluna descricao é VARCHAR(255).
    @Test
    void deveRejeitarDescricaoComMaisDe255Caracteres() {
        assertErroNoCampo(
                com("Corte", "a".repeat(256), 30, BigDecimal.TEN, true),
                "descricao"
        );
    }

    // -------------------------------------------------------------- duracao

    @Test
    void deveRejeitarDuracaoNula() {
        assertErroNoCampo(com("Corte", "Descrição", null, BigDecimal.TEN, true), "duracaoMinutos");
    }

    @Test
    void deveRejeitarDuracaoNegativa() {
        assertErroNoCampo(com("Corte", "Descrição", -10, BigDecimal.TEN, true), "duracaoMinutos");
    }

    // O AgendamentoService recusa serviços com duração <= 0.
    @Test
    void deveRejeitarDuracaoZero() {
        assertErroNoCampo(com("Corte", "Descrição", 0, BigDecimal.TEN, true), "duracaoMinutos");
    }

    // ---------------------------------------------------------------- preco

    // A coluna preco é NOT NULL.
    @Test
    void deveRejeitarPrecoNulo() {
        assertErroNoCampo(com("Corte", "Descrição", 30, null, true), "preco");
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        assertErroNoCampo(
                com("Corte", "Descrição", 30, new BigDecimal("-1.00"), true),
                "preco"
        );
    }

    @Test
    void deveRejeitarPrecoAcimaDoLimite() {
        assertErroNoCampo(
                com("Corte", "Descrição", 30, new BigDecimal("1000.01"), true),
                "preco"
        );
    }

    @Test
    void deveAceitarPrecoNoLimite() {
        assertTrue(validador.validate(
                com("Corte", "Descrição", 30, new BigDecimal("1000.00"), true)
        ).isEmpty());
    }

    // ---------------------------------------------------------------- ativo

    @Test
    void deveRejeitarAtivoNulo() {
        assertErroNoCampo(com("Corte", "Descrição", 30, BigDecimal.TEN, null), "ativo");
    }
}
