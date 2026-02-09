package com.alexandre.Barbearia_Api.service.caixa;

import com.alexandre.Barbearia_Api.dto.caixa.CaixaCreateDTO;
import com.alexandre.Barbearia_Api.dto.caixa.CaixaResponseDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoCreateDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoPreviewDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoResponseDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoResumoBarbeiroDTO;
import com.alexandre.Barbearia_Api.dto.caixa.fechamento.CaixaFechamentoResumoDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.Caixa;
import com.alexandre.Barbearia_Api.model.CaixaFechamento;
import com.alexandre.Barbearia_Api.model.CaixaFechamentoNfceStatus;
import com.alexandre.Barbearia_Api.model.CaixaFechamentoPeriodo;
import com.alexandre.Barbearia_Api.model.CaixaTipo;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.model.Comissao;
import com.alexandre.Barbearia_Api.repository.CaixaFechamentoRepository;
import com.alexandre.Barbearia_Api.repository.CaixaRepository;
import com.alexandre.Barbearia_Api.repository.ComissaoRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final CaixaFechamentoRepository caixaFechamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComissaoRepository comissaoRepository;

    public CaixaService(
            CaixaRepository caixaRepository,
            CaixaFechamentoRepository caixaFechamentoRepository,
            UsuarioRepository usuarioRepository,
            ComissaoRepository comissaoRepository
    ) {
        this.caixaRepository = caixaRepository;
        this.caixaFechamentoRepository = caixaFechamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.comissaoRepository = comissaoRepository;
    }

    public Caixa createEntradaAgendamento(Agendamento agendamento) {
        return caixaRepository.findByAgendamento_Id(agendamento.getId())
                .orElseGet(() -> {
                    Caixa caixa = new Caixa();
                    caixa.setTipo(CaixaTipo.ENTRADA);
                    caixa.setDescricao("Agendamento " + agendamento.getId() + " - " + agendamento.getServico().getName());
                    caixa.setValor(agendamento.getServico().getPrice());
                    caixa.setAgendamento(agendamento);
                    caixa.setBarbeiro(agendamento.getBarbeiro());
                    return caixaRepository.save(caixa);
                });
    }

    public CaixaResponseDTO createManual(CaixaCreateDTO dto) {
        Caixa caixa = new Caixa();
        caixa.setTipo(dto.tipo());
        caixa.setDescricao(dto.descricao());
        caixa.setValor(dto.valor());

        if (dto.barbeiroUsername() != null && !dto.barbeiroUsername().isBlank()) {
            Usuario barbeiro = usuarioRepository.findByUsername(dto.barbeiroUsername())
                    .orElseThrow(UsuarioNotFoundException::new);
            caixa.setBarbeiro(barbeiro);
        }

        Caixa saved = caixaRepository.save(caixa);
        return toResponse(saved);
    }

    public List<CaixaResponseDTO> find(CaixaTipo tipo, LocalDate inicio, LocalDate fim) {
        LocalDateTime dataInicio = inicio != null ? inicio.atStartOfDay() : null;
        LocalDateTime dataFim = fim != null ? fim.atTime(23, 59, 59) : null;

        List<Caixa> lista;
        if (tipo != null && dataInicio != null && dataFim != null) {
            lista = caixaRepository.findByTipoAndDataDeCriacaoBetween(tipo, dataInicio, dataFim);
        } else if (tipo != null) {
            lista = caixaRepository.findByTipo(tipo);
        } else if (dataInicio != null && dataFim != null) {
            lista = caixaRepository.findByDataDeCriacaoBetween(dataInicio, dataFim);
        } else {
            lista = caixaRepository.findAll();
        }

        return lista.stream().map(this::toResponse).toList();
    }

    public CaixaFechamentoPreviewDTO previewFechamento(
            CaixaFechamentoPeriodo periodo,
            LocalDate referencia,
            LocalDate inicio,
            LocalDate fim
    ) {
        PeriodRange range = resolvePeriodRange(periodo, referencia, inicio, fim);
        CaixaFechamentoResumoDTO resumo = buildResumo(range.dataInicio(), range.dataFim());

        return new CaixaFechamentoPreviewDTO(
                periodo,
                range.referencia(),
                resumo,
                null
        );
    }

    public CaixaFechamentoResponseDTO fechar(CaixaFechamentoCreateDTO dto) {
        PeriodRange range = resolvePeriodRange(dto.periodo(), dto.referencia(), dto.inicio(), dto.fim());
        validateFechamentoDuplicado(dto.periodo(), range.dataInicio(), range.dataFim());

        CaixaFechamentoResumoDTO resumo = buildResumo(range.dataInicio(), range.dataFim());
        BigDecimal saldoInformado = dto.saldoInformado();
        BigDecimal diferenca =
                saldoInformado != null ? saldoInformado.subtract(resumo.saldoApurado()) : null;

        CaixaFechamento fechamento = new CaixaFechamento();
        fechamento.setPeriodo(dto.periodo());
        fechamento.setDataInicio(range.dataInicio());
        fechamento.setDataFim(range.dataFim());
        fechamento.setTotalEntradas(resumo.totalEntradas());
        fechamento.setTotalSaidas(resumo.totalSaidas());
        fechamento.setSaldoApurado(resumo.saldoApurado());
        fechamento.setSaldoInformado(saldoInformado);
        fechamento.setDiferenca(diferenca);
        fechamento.setTotalLancamentos(resumo.totalLancamentos());
        fechamento.setObservacao(normalizeObservacao(dto.observacao()));

        boolean solicitarNfce = Boolean.TRUE.equals(dto.solicitarNfce());
        fechamento.setSolicitarNfce(solicitarNfce);
        fechamento.setNfceStatus(
                solicitarNfce
                        ? CaixaFechamentoNfceStatus.PENDENTE_INTEGRACAO
                        : CaixaFechamentoNfceStatus.NAO_SOLICITADA
        );
        fechamento.setFechadoPor(getUsuarioAutenticado());

        CaixaFechamento saved = caixaFechamentoRepository.save(fechamento);
        return toFechamentoResponse(saved);
    }

    public List<CaixaFechamentoResponseDTO> findFechamentos(
            CaixaFechamentoPeriodo periodo,
            LocalDate inicio,
            LocalDate fim
    ) {
        return caixaFechamentoRepository.findAllByOrderByDataDeCriacaoDesc().stream()
                .filter(item -> periodo == null || item.getPeriodo() == periodo)
                .filter(item -> inicio == null || !item.getDataFim().toLocalDate().isBefore(inicio))
                .filter(item -> fim == null || !item.getDataInicio().toLocalDate().isAfter(fim))
                .map(this::toFechamentoResponse)
                .toList();
    }

    private CaixaResponseDTO toResponse(Caixa caixa) {
        BigDecimal valorBarbeiro = null;
        BigDecimal valorBarbearia = null;
        BigDecimal percentualComissao = null;

        if (caixa.getAgendamento() != null) {
            Comissao comissao = comissaoRepository.findByAgendamento_Id(caixa.getAgendamento().getId())
                    .orElse(null);
            if (comissao != null) {
                percentualComissao = comissao.getPercentual();
                valorBarbeiro = comissao.getValor();
                if (caixa.getValor() != null) {
                    valorBarbearia = caixa.getValor().subtract(valorBarbeiro);
                }
            }
        }

        return new CaixaResponseDTO(
                caixa.getId(),
                caixa.getTipo(),
                caixa.getDescricao(),
                caixa.getValor(),
                valorBarbeiro,
                valorBarbearia,
                percentualComissao,
                caixa.getAgendamento() != null ? caixa.getAgendamento().getId() : null,
                caixa.getBarbeiro() != null ? caixa.getBarbeiro().getUsername() : null,
                caixa.getAgendamento() != null ? caixa.getAgendamento().getServico().getName() : null,
                caixa.getDataDeCriacao()
        );
    }

    private CaixaFechamentoResumoDTO buildResumo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<Caixa> lancamentos = caixaRepository.findByDataDeCriacaoBetween(dataInicio, dataFim);

        BigDecimal totalEntradas = lancamentos.stream()
                .filter(item -> item.getTipo() == CaixaTipo.ENTRADA)
                .map(Caixa::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = lancamentos.stream()
                .filter(item -> item.getTipo() == CaixaTipo.SAIDA)
                .map(Caixa::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoApurado = totalEntradas.subtract(totalSaidas);

        Map<String, BarberResumoAccumulator> porBarbeiro = new TreeMap<>();
        for (Caixa lancamento : lancamentos) {
            String username = lancamento.getBarbeiro() != null
                    ? lancamento.getBarbeiro().getUsername()
                    : "sem_barbeiro";
            porBarbeiro.computeIfAbsent(username, BarberResumoAccumulator::new)
                    .add(lancamento.getTipo(), lancamento.getValor());
        }

        List<CaixaFechamentoResumoBarbeiroDTO> resumoPorBarbeiro = new ArrayList<>();
        for (BarberResumoAccumulator acc : porBarbeiro.values()) {
            resumoPorBarbeiro.add(acc.toDTO());
        }

        return new CaixaFechamentoResumoDTO(
                dataInicio,
                dataFim,
                totalEntradas,
                totalSaidas,
                saldoApurado,
                (long) lancamentos.size(),
                resumoPorBarbeiro
        );
    }

    private PeriodRange resolvePeriodRange(
            CaixaFechamentoPeriodo periodo,
            LocalDate referencia,
            LocalDate inicio,
            LocalDate fim
    ) {
        if (periodo == null) {
            throw new IllegalArgumentException("Periodo do fechamento nao informado.");
        }

        LocalDate referenceDate = referencia != null ? referencia : LocalDate.now();

        return switch (periodo) {
            case DIARIO -> new PeriodRange(
                    referenceDate.atStartOfDay(),
                    referenceDate.atTime(23, 59, 59),
                    referenceDate
            );
            case SEMANAL -> {
                LocalDate inicioSemana = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate fimSemana = inicioSemana.plusDays(6);
                yield new PeriodRange(
                        inicioSemana.atStartOfDay(),
                        fimSemana.atTime(23, 59, 59),
                        referenceDate
                );
            }
            case MENSAL -> {
                LocalDate inicioMes = referenceDate.withDayOfMonth(1);
                LocalDate fimMes = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
                yield new PeriodRange(
                        inicioMes.atStartOfDay(),
                        fimMes.atTime(23, 59, 59),
                        referenceDate
                );
            }
            case PERSONALIZADO -> {
                if (inicio == null || fim == null) {
                    throw new IllegalArgumentException("Informe inicio e fim para fechamento personalizado.");
                }
                if (fim.isBefore(inicio)) {
                    throw new IllegalArgumentException("O fim do fechamento nao pode ser anterior ao inicio.");
                }
                yield new PeriodRange(
                        inicio.atStartOfDay(),
                        fim.atTime(23, 59, 59),
                        null
                );
            }
        };
    }

    private void validateFechamentoDuplicado(
            CaixaFechamentoPeriodo periodo,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        boolean exists = caixaFechamentoRepository.existsByPeriodoAndDataInicioAndDataFim(
                periodo,
                dataInicio,
                dataFim
        );

        if (exists) {
            throw new IllegalArgumentException("Ja existe fechamento para esse periodo.");
        }
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Nao foi possivel identificar o usuario autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {
            return usuario;
        }

        String username = null;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String value) {
            username = value;
        }

        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Nao foi possivel identificar o usuario autenticado.");
        }

        String normalized = username.trim().toLowerCase();
        return usuarioRepository.findByUsername(normalized)
                .orElseThrow(UsuarioNotFoundException::new);
    }

    private String normalizeObservacao(String observacao) {
        if (observacao == null) return null;
        String normalized = observacao.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private CaixaFechamentoResponseDTO toFechamentoResponse(CaixaFechamento fechamento) {
        return new CaixaFechamentoResponseDTO(
                fechamento.getId(),
                fechamento.getPeriodo(),
                fechamento.getDataInicio(),
                fechamento.getDataFim(),
                fechamento.getTotalEntradas(),
                fechamento.getTotalSaidas(),
                fechamento.getSaldoApurado(),
                fechamento.getSaldoInformado(),
                fechamento.getDiferenca(),
                fechamento.getTotalLancamentos(),
                fechamento.getObservacao(),
                fechamento.getSolicitarNfce(),
                fechamento.getNfceStatus(),
                fechamento.getNfceChave(),
                fechamento.getFechadoPor() != null ? fechamento.getFechadoPor().getUsername() : null,
                fechamento.getDataDeCriacao()
        );
    }

    private record PeriodRange(LocalDateTime dataInicio, LocalDateTime dataFim, LocalDate referencia) {}

    private static final class BarberResumoAccumulator {
        private final String username;
        private BigDecimal entradas = BigDecimal.ZERO;
        private BigDecimal saidas = BigDecimal.ZERO;
        private long totalLancamentos = 0L;

        private BarberResumoAccumulator(String username) {
            this.username = username;
        }

        private void add(CaixaTipo tipo, BigDecimal valor) {
            if (valor == null || tipo == null) return;

            if (tipo == CaixaTipo.ENTRADA) {
                entradas = entradas.add(valor);
            } else if (tipo == CaixaTipo.SAIDA) {
                saidas = saidas.add(valor);
            }
            totalLancamentos += 1L;
        }

        private CaixaFechamentoResumoBarbeiroDTO toDTO() {
            return new CaixaFechamentoResumoBarbeiroDTO(
                    username,
                    entradas,
                    saidas,
                    entradas.subtract(saidas),
                    totalLancamentos
            );
        }
    }
}
