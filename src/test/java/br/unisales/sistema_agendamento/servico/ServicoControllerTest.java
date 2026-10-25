package br.unisales.sistema_agendamento.servico;

import br.unisales.sistema_agendamento.servico.controller.ServicoController;
import br.unisales.sistema_agendamento.servico.dto.ServicoRequest;
import br.unisales.sistema_agendamento.servico.dto.ServicoResponse;
import br.unisales.sistema_agendamento.servico.service.ServicoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @apiNote Testa as respostas HTTP do ServicoController, inclusive os erros
 * convertidos pelo GlobalExceptionHandler.
 * Os filtros de segurança são desligados: aqui o foco é o domínio,
 * não as permissões de acesso.
 * @author Mateus Alves Costa
 * @since 25.10.2026
 */
@WebMvcTest(ServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ServicoControllerTest {

    private static final String JSON_VALIDO = """
            {
              "nome": "Corte masculino",
              "descricao": "Corte com máquina e tesoura",
              "duracaoMinutos": 30,
              "preco": 45.00,
              "ativo": true
            }
            """;

    private static final String JSON_SEM_NOME = """
            {
              "nome": "",
              "descricao": "Corte com máquina e tesoura",
              "duracaoMinutos": 30,
              "preco": 45.00,
              "ativo": true
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicoService service;

    private final ServicoResponse resposta = new ServicoResponse(
            1L,
            "Corte masculino",
            "Corte com máquina e tesoura",
            30,
            new BigDecimal("45.00"),
            true
    );

    // ---------------------------------------------------------- POST /servicos

    @Test
    void deveCriarServicoERetornarLocalizacao() throws Exception {
        when(service.criar(any(ServicoRequest.class))).thenReturn(resposta);

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/servicos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Corte masculino"));
    }

    // As regras do ServicoRequest só valem se o controller usar @Valid.
    @Test
    void deveRetornar400SemChamarServiceQuandoCriarComDadosInvalidos() throws Exception {
        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SEM_NOME))
                .andExpect(status().isBadRequest());

        verify(service, never()).criar(any());
    }

    @Test
    void deveRetornar400QuandoJsonForMalformado() throws Exception {
        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nome\": "))
                .andExpect(status().isBadRequest());

        verify(service, never()).criar(any());
    }

    @Test
    void deveRetornar400QuandoDuracaoNaoForNumero() throws Exception {
        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO.replace("30", "\"meia hora\"")))
                .andExpect(status().isBadRequest());

        verify(service, never()).criar(any());
    }

    @Test
    void deveRetornar409QuandoNomeJaExistir() throws Exception {
        when(service.criar(any(ServicoRequest.class)))
                .thenThrow(new DataIntegrityViolationException("Serviço com o mesmo nome já existe."));

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de dados"));
    }

    // ---------------------------------------------------- GET /servicos/{id}

    @Test
    void deveBuscarServicoPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(resposta);

        mockMvc.perform(get("/servicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Corte masculino"))
                .andExpect(jsonPath("$.preco").value(45.00));
    }

    @Test
    void deveRetornar404QuandoServicoNaoExistir() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException("Serviço não encontrado com o ID: 99"));

        mockMvc.perform(get("/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Serviço não encontrado com o ID: 99"));
    }

    @Test
    void deveRetornar400QuandoIdNaoForNumerico() throws Exception {
        mockMvc.perform(get("/servicos/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // ------------------------------------------------- GET /servicos e /buscar

    @Test
    void deveListarComPaginacaoPadraoOrdenadaPorNome() throws Exception {
        when(service.listar(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(captor.capture());

        assertEquals(10, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, captor.getValue().getSort().getOrderFor("nome").getDirection());
    }

    @Test
    void deveRepassarFiltrosDaBusca() throws Exception {
        when(service.buscar(eq("corte"), eq(false), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/servicos/buscar")
                        .param("nome", "corte")
                        .param("ativo", "false"))
                .andExpect(status().isOk());

        verify(service).buscar(eq("corte"), eq(false), any(Pageable.class));
    }

    @Test
    void deveRetornar400QuandoFiltroAtivoForInvalido() throws Exception {
        mockMvc.perform(get("/servicos/buscar").param("ativo", "talvez"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // ---------------------------------------------------- PUT /servicos/{id}

    @Test
    void deveAtualizarServico() throws Exception {
        when(service.atualizar(eq(1L), any(ServicoRequest.class))).thenReturn(resposta);

        mockMvc.perform(put("/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRetornar400SemChamarServiceQuandoAtualizarComDadosInvalidos() throws Exception {
        mockMvc.perform(put("/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_SEM_NOME))
                .andExpect(status().isBadRequest());

        verify(service, never()).atualizar(anyLong(), any());
    }

    @Test
    void deveRetornar404AoAtualizarServicoInexistente() throws Exception {
        when(service.atualizar(eq(99L), any(ServicoRequest.class)))
                .thenThrow(new EntityNotFoundException("Serviço não encontrado com o ID: 99"));

        mockMvc.perform(put("/servicos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isNotFound());
    }

    // ----------------------------------------------- PATCH /servicos/{id}/{valor}

    @Test
    void deveDesativarServicoERetornar204() throws Exception {
        mockMvc.perform(patch("/servicos/1/false"))
                .andExpect(status().isNoContent());

        verify(service).ativarDesativar(1L, false);
    }

    @Test
    void deveRetornar404AoAlterarStatusDeServicoInexistente() throws Exception {
        doThrow(new EntityNotFoundException("Serviço não encontrado com o ID: 99"))
                .when(service).ativarDesativar(99L, true);

        mockMvc.perform(patch("/servicos/99/true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoValorDoStatusNaoForBooleano() throws Exception {
        mockMvc.perform(patch("/servicos/1/talvez"))
                .andExpect(status().isBadRequest());

        verify(service, never()).ativarDesativar(anyLong(), anyBoolean());
    }
}
