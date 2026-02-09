package com.alexandre.Barbearia_Api.repository;

import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.AgendamentoStatus;
import com.alexandre.Barbearia_Api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento>{
    List<Agendamento> findByCliente_Username(String clienteUsername);
    List<Agendamento> findByBarbeiro_Username(String barbeiroUsername);
    List<Agendamento> findByServicoId(Long servicoId);
    List<Agendamento> findByDataAndHora(LocalDate data, LocalTime hora);
    List<Agendamento> findByBarbeiroAndData(Usuario barbeiro, LocalDate data);
    List<Agendamento> findByBarbeiro_UsernameAndDataBetweenOrderByDataAscHoraAsc(
            String barbeiroUsername,
            LocalDate inicio,
            LocalDate fim
    );
    List<Agendamento> findByData(LocalDate data);
    List<Agendamento> findByAgendamentoStatus(AgendamentoStatus agendamentoStatus);
    @Modifying
    @Query("""
        update Agendamento a
        set a.agendamentoStatus = com.alexandre.Barbearia_Api.model.AgendamentoStatus.EXPIRADO
        where a.agendamentoStatus = com.alexandre.Barbearia_Api.model.AgendamentoStatus.REQUISITADO
          and (a.data < :hoje or (a.data = :hoje and a.hora < :agora))
    """)
    int expirarRequisitados(
            @Param("hoje") LocalDate hoje,
            @Param("agora") LocalTime agora
    );
}
