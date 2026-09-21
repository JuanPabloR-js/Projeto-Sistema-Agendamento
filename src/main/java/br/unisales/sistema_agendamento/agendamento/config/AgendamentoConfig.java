package br.unisales.sistema_agendamento.agendamento.config;

import br.unisales.sistema_agendamento.agendamento.service.ExpedienteAgendamento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * @apiNote Configura o relógio e o expediente utilizados no agendamento.
 * @author Juan Pablo Rocha Hempel
 * @since 20.09.2026
 */
@Configuration
public class AgendamentoConfig {

    @Bean("relogioAgendamento")
    public Clock relogioAgendamento() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ExpedienteAgendamento expedienteAgendamento(
            @Value("${agendamento.abertura:09:00}")
            String abertura,

            @Value("${agendamento.inicio-intervalo:12:00}")
            String inicioIntervalo,

            @Value("${agendamento.fim-intervalo:14:00}")
            String fimIntervalo,

            @Value("${agendamento.fechamento:18:00}")
            String fechamento,

            @Value("${agendamento.passo-minutos:10}")
            int passoMinutos,

            @Value("${agendamento.dias:MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY}")
            String dias
    ) {
        Set<DayOfWeek> diasAbertos = new HashSet<>();

        for (String dia : dias.split(",")) {
            diasAbertos.add(DayOfWeek.valueOf(dia.trim()));
        }

        return new ExpedienteAgendamento(
                LocalTime.parse(abertura),
                LocalTime.parse(inicioIntervalo),
                LocalTime.parse(fimIntervalo),
                LocalTime.parse(fechamento),
                passoMinutos,
                diasAbertos
        );
    }
}