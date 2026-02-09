package com.alexandre.Barbearia_Api.service.dashboard;

import com.alexandre.Barbearia_Api.dto.dashboard.DashboardBarberRankingDTO;
import com.alexandre.Barbearia_Api.dto.dashboard.DashboardDailyPointDTO;
import com.alexandre.Barbearia_Api.dto.dashboard.DashboardOverviewDTO;
import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.AgendamentoStatus;
import com.alexandre.Barbearia_Api.model.Caixa;
import com.alexandre.Barbearia_Api.model.CaixaTipo;
import com.alexandre.Barbearia_Api.model.Comissao;
import com.alexandre.Barbearia_Api.repository.AgendamentoRepository;
import com.alexandre.Barbearia_Api.repository.CaixaRepository;
import com.alexandre.Barbearia_Api.repository.ComissaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final AgendamentoRepository agendamentoRepository;
    private final CaixaRepository caixaRepository;
    private final ComissaoRepository comissaoRepository;

    public DashboardService(
            AgendamentoRepository agendamentoRepository,
            CaixaRepository caixaRepository,
            ComissaoRepository comissaoRepository
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.caixaRepository = caixaRepository;
        this.comissaoRepository = comissaoRepository;
    }

    public DashboardOverviewDTO getOverview(LocalDate inicio, LocalDate fim) {
        LocalDate dataFim = fim != null ? fim : LocalDate.now();
        LocalDate dataInicio = inicio != null ? inicio : dataFim.minusDays(29);

        validarPeriodo(dataInicio, dataFim);

        List<Agendamento> agendamentos = agendamentoRepository.findByDataBetween(dataInicio, dataFim);
        LocalDateTime dataHoraInicio = dataInicio.atStartOfDay();
        LocalDateTime dataHoraFim = dataFim.atTime(23, 59, 59);
        List<Caixa> lancamentos = caixaRepository.findByDataDeCriacaoBetween(dataHoraInicio, dataHoraFim);
        List<Comissao> comissoes = comissaoRepository.findByDataDeCriacaoBetween(dataHoraInicio, dataHoraFim);

        DashboardAccumulator accumulator = new DashboardAccumulator(dataInicio, dataFim);
        accumulator.addAgendamentos(agendamentos);
        accumulator.addLancamentos(lancamentos);
        accumulator.addComissoes(comissoes);

        return accumulator.toDTO();
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Informe inicio e fim para consultar o dashboard.");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data fim deve ser igual ou posterior a data inicio.");
        }
        long dias = ChronoUnit.DAYS.between(inicio, fim) + 1;
        if (dias > 366) {
            throw new IllegalArgumentException("O dashboard permite intervalo de ate 366 dias.");
        }
    }

    private static final class DashboardAccumulator {
        private final LocalDate inicio;
        private final LocalDate fim;
        private final long diasAnalisados;
        private final Map<LocalDate, DailyAccumulator> serie = new LinkedHashMap<>();
        private final Map<String, BarberAccumulator> ranking = new LinkedHashMap<>();

        private long totalAgendamentos = 0L;
        private long totalRequisitados = 0L;
        private long totalAgendados = 0L;
        private long totalConcluidos = 0L;
        private long totalCancelados = 0L;
        private long totalExpirados = 0L;
        private BigDecimal totalEntradas = BigDecimal.ZERO;
        private BigDecimal totalSaidas = BigDecimal.ZERO;
        private BigDecimal totalComissoes = BigDecimal.ZERO;

        private DashboardAccumulator(LocalDate inicio, LocalDate fim) {
            this.inicio = inicio;
            this.fim = fim;
            this.diasAnalisados = ChronoUnit.DAYS.between(inicio, fim) + 1;

            for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
                serie.put(data, new DailyAccumulator(data));
            }
        }

        private void addAgendamentos(List<Agendamento> agendamentos) {
            for (Agendamento agendamento : agendamentos) {
                if (agendamento == null || agendamento.getData() == null) continue;

                totalAgendamentos += 1L;
                DailyAccumulator dia = serie.get(agendamento.getData());
                if (dia != null) {
                    dia.agendamentos += 1L;
                }

                AgendamentoStatus status = agendamento.getAgendamentoStatus();
                if (status == AgendamentoStatus.REQUISITADO) totalRequisitados += 1L;
                if (status == AgendamentoStatus.AGENDADO) totalAgendados += 1L;
                if (status == AgendamentoStatus.CONCLUIDO) totalConcluidos += 1L;
                if (status == AgendamentoStatus.CANCELADO) totalCancelados += 1L;
                if (status == AgendamentoStatus.EXPIRADO) totalExpirados += 1L;

                if (status == AgendamentoStatus.CONCLUIDO && dia != null) {
                    dia.concluidos += 1L;
                }

                if (agendamento.getBarbeiro() != null && agendamento.getBarbeiro().getUsername() != null) {
                    BarberAccumulator barbeiro = ranking.computeIfAbsent(
                            agendamento.getBarbeiro().getUsername(),
                            key -> new BarberAccumulator(
                                    agendamento.getBarbeiro().getUsername(),
                                    agendamento.getBarbeiro().getName()
                            )
                    );
                    barbeiro.agendamentos += 1L;
                    if (status == AgendamentoStatus.CONCLUIDO) {
                        barbeiro.concluidos += 1L;
                    }
                }
            }
        }

        private void addLancamentos(List<Caixa> lancamentos) {
            for (Caixa lancamento : lancamentos) {
                if (lancamento == null || lancamento.getDataDeCriacao() == null) continue;
                LocalDate data = lancamento.getDataDeCriacao().toLocalDate();
                DailyAccumulator dia = serie.get(data);
                BigDecimal valor = safeValue(lancamento.getValor());

                if (lancamento.getTipo() == CaixaTipo.ENTRADA) {
                    totalEntradas = totalEntradas.add(valor);
                    if (dia != null) dia.entradas = dia.entradas.add(valor);
                    if (lancamento.getBarbeiro() != null && lancamento.getBarbeiro().getUsername() != null) {
                        BarberAccumulator barbeiro = ranking.computeIfAbsent(
                                lancamento.getBarbeiro().getUsername(),
                                key -> new BarberAccumulator(
                                        lancamento.getBarbeiro().getUsername(),
                                        lancamento.getBarbeiro().getName()
                                )
                        );
                        barbeiro.faturamento = barbeiro.faturamento.add(valor);
                    }
                } else if (lancamento.getTipo() == CaixaTipo.SAIDA) {
                    totalSaidas = totalSaidas.add(valor);
                    if (dia != null) dia.saidas = dia.saidas.add(valor);
                }
            }
        }

        private void addComissoes(List<Comissao> comissoes) {
            for (Comissao comissao : comissoes) {
                if (comissao == null || comissao.getBarbeiro() == null) continue;
                BigDecimal valor = safeValue(comissao.getValor());
                totalComissoes = totalComissoes.add(valor);

                String username = comissao.getBarbeiro().getUsername();
                if (username == null || username.isBlank()) continue;

                BarberAccumulator barbeiro = ranking.computeIfAbsent(
                        username,
                        key -> new BarberAccumulator(username, comissao.getBarbeiro().getName())
                );
                barbeiro.comissao = barbeiro.comissao.add(valor);
            }
        }

        private DashboardOverviewDTO toDTO() {
            BigDecimal saldo = totalEntradas.subtract(totalSaidas);
            BigDecimal ticketMedio = totalConcluidos > 0
                    ? totalEntradas.divide(BigDecimal.valueOf(totalConcluidos), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            List<DashboardDailyPointDTO> serieDiaria = new ArrayList<>();
            for (DailyAccumulator dia : serie.values()) {
                serieDiaria.add(new DashboardDailyPointDTO(
                        dia.data,
                        dia.agendamentos,
                        dia.concluidos,
                        dia.entradas,
                        dia.saidas,
                        dia.entradas.subtract(dia.saidas)
                ));
            }

            List<DashboardBarberRankingDTO> rankingBarbeiros = ranking.values().stream()
                    .sorted((left, right) -> {
                        int byRevenue = right.faturamento.compareTo(left.faturamento);
                        if (byRevenue != 0) return byRevenue;
                        int byConcluidos = Long.compare(right.concluidos, left.concluidos);
                        if (byConcluidos != 0) return byConcluidos;
                        return left.username.compareToIgnoreCase(right.username);
                    })
                    .map(item -> new DashboardBarberRankingDTO(
                            item.username,
                            item.nome,
                            item.agendamentos,
                            item.concluidos,
                            item.faturamento,
                            item.comissao
                    ))
                    .limit(10)
                    .toList();

            return new DashboardOverviewDTO(
                    inicio,
                    fim,
                    diasAnalisados,
                    totalAgendamentos,
                    totalRequisitados,
                    totalAgendados,
                    totalConcluidos,
                    totalCancelados,
                    totalExpirados,
                    totalEntradas,
                    totalSaidas,
                    saldo,
                    totalComissoes,
                    ticketMedio,
                    serieDiaria,
                    rankingBarbeiros
            );
        }

        private BigDecimal safeValue(BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
        }
    }

    private static final class DailyAccumulator {
        private final LocalDate data;
        private long agendamentos = 0L;
        private long concluidos = 0L;
        private BigDecimal entradas = BigDecimal.ZERO;
        private BigDecimal saidas = BigDecimal.ZERO;

        private DailyAccumulator(LocalDate data) {
            this.data = data;
        }
    }

    private static final class BarberAccumulator {
        private final String username;
        private final String nome;
        private long agendamentos = 0L;
        private long concluidos = 0L;
        private BigDecimal faturamento = BigDecimal.ZERO;
        private BigDecimal comissao = BigDecimal.ZERO;

        private BarberAccumulator(String username, String nome) {
            this.username = username;
            this.nome = nome;
        }
    }
}
