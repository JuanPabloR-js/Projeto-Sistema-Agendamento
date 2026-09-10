package br.unisales.sistema_agendamento.agendamento.controller;

import br.unisales.sistema_agendamento.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = OpenApiConfig.JWT_SECURITY_SCHEME)
public class AgendamentoController {
}
