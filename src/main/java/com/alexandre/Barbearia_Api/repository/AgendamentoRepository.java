package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.AgendamentoStatus;
import com.alexandre.Barbearia_Api.model.Servico;
import com.alexandre.Barbearia_Api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento>{
    List<Agendamento> findByCliente_Username(String clienteUsername);
    List<Agendamento> findByBarbeiro_Username(String barbeiroUsername);
    List<Agendamento> findByServicoId(Long servicoId);
    List<Agendamento> findByDataAndHora(LocalDate data, LocalTime hora);
    List<Agendamento> findByBarbeiroAndData(Usuario barbeiro, LocalDate data);
    List<Agendamento> findByData(LocalDate data);
    List<Agendamento> findByAgendamentoStatus(AgendamentoStatus agendamentoStatus);
}
