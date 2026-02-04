package com.alexandre.Barbearia_Api.service.agendamento;

import com.alexandre.Barbearia_Api.repository.AgendamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AgendamentoExpirationService {

    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public AgendamentoExpirationService(AgendamentoRepository agendamentoRepository, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    // a cada 15 minutos
    @Transactional
    @Scheduled(cron = "0 */15 * * * *", zone = "America/Sao_Paulo")
    public void expirarAgendamentos() {

        LocalDate hoje = LocalDate.now(clock);
        LocalTime agora = LocalTime.now(clock);

        agendamentoRepository.expirarRequisitados(hoje, agora);
    }
}
