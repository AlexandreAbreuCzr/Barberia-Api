package com.alexandre.Barbearia_Api.service.caixa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final CaixaFechamentoRepository caixaFechamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComissaoRepository comissaoRepository;
    private final String nfceProvider;
    private final String nfceNuvemFiscalBaseUrl;
    private final String nfceNuvemFiscalToken;
    private final String nfceNuvemFiscalAmbiente;
    private final String nfceNuvemFiscalPrestadorCpfCnpj;
    private final String nfceNuvemFiscalCodigoServico;
    private final String nfceNuvemFiscalItemListaServico;
    private final String nfceNuvemFiscalMunicipioCodigo;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CaixaService(
            CaixaRepository caixaRepository,
            CaixaFechamentoRepository caixaFechamentoRepository,
            UsuarioRepository usuarioRepository,
            ComissaoRepository comissaoRepository,
            ObjectMapper objectMapper,
            @Value("${app.nfce.provider:mock}") String nfceProvider,
            @Value("${app.nfce.nuvemfiscal.base-url:https://api.nuvemfiscal.com.br}") String nfceNuvemFiscalBaseUrl,
            @Value("${app.nfce.nuvemfiscal.token:}") String nfceNuvemFiscalToken,
            @Value("${app.nfce.nuvemfiscal.ambiente:homologacao}") String nfceNuvemFiscalAmbiente,
            @Value("${app.nfce.nuvemfiscal.prestador-cpf-cnpj:}") String nfceNuvemFiscalPrestadorCpfCnpj,
            @Value("${app.nfce.nuvemfiscal.codigo-servico:}") String nfceNuvemFiscalCodigoServico,
            @Value("${app.nfce.nuvemfiscal.item-lista-servico:}") String nfceNuvemFiscalItemListaServico,
            @Value("${app.nfce.nuvemfiscal.municipio-codigo:}") String nfceNuvemFiscalMunicipioCodigo
    ) {
        this.caixaRepository = caixaRepository;
        this.caixaFechamentoRepository = caixaFechamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.comissaoRepository = comissaoRepository;
        this.objectMapper = objectMapper;
        this.nfceProvider = nfceProvider;
        this.nfceNuvemFiscalBaseUrl = nfceNuvemFiscalBaseUrl;
        this.nfceNuvemFiscalToken = nfceNuvemFiscalToken;
        this.nfceNuvemFiscalAmbiente = nfceNuvemFiscalAmbiente;
        this.nfceNuvemFiscalPrestadorCpfCnpj = nfceNuvemFiscalPrestadorCpfCnpj;
        this.nfceNuvemFiscalCodigoServico = nfceNuvemFiscalCodigoServico;
        this.nfceNuvemFiscalItemListaServico = nfceNuvemFiscalItemListaServico;
        this.nfceNuvemFiscalMunicipioCodigo = nfceNuvemFiscalMunicipioCodigo;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
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
                buildNfceInfo()
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
        if (solicitarNfce) {
            saved = emitirNfceInterno(saved);
        }
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

    public CaixaFechamentoResponseDTO emitirNfce(Long fechamentoId) {
        CaixaFechamento fechamento = caixaFechamentoRepository.findById(fechamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Fechamento nao encontrado."));

        CaixaFechamento atualizado = emitirNfceInterno(fechamento);
        return toFechamentoResponse(atualizado);
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

    private CaixaFechamento emitirNfceInterno(CaixaFechamento fechamento) {
        if (!Boolean.TRUE.equals(fechamento.getSolicitarNfce())) {
            throw new IllegalArgumentException("Esse fechamento nao foi marcado para emissao de NFC-e.");
        }

        if (fechamento.getNfceStatus() == CaixaFechamentoNfceStatus.EMITIDA
                && fechamento.getNfceChave() != null
                && !fechamento.getNfceChave().isBlank()) {
            return fechamento;
        }

        String provider = providerName();

        try {
            if ("mock".equals(provider)) {
                fechamento.setNfceStatus(CaixaFechamentoNfceStatus.EMITIDA);
                fechamento.setNfceChave(generateMockNfceKey(fechamento.getId()));
            } else if ("nuvemfiscal".equals(provider)) {
                emitirNfceNuvemFiscal(fechamento);
            } else {
                fechamento.setNfceStatus(CaixaFechamentoNfceStatus.PENDENTE_INTEGRACAO);
                if (fechamento.getNfceChave() != null && fechamento.getNfceChave().startsWith("MOCK-")) {
                    fechamento.setNfceChave(null);
                }
            }
        } catch (Exception exception) {
            fechamento.setNfceStatus(CaixaFechamentoNfceStatus.FALHA);
            caixaFechamentoRepository.save(fechamento);
            throw new IllegalStateException("Falha ao emitir NFC-e.");
        }

        return caixaFechamentoRepository.save(fechamento);
    }

    private String buildNfceInfo() {
        return switch (providerName()) {
            case "mock" -> "Nota fiscal em modo MOCK: o sistema gera chave simulada para testes.";
            case "nuvemfiscal" -> "Nota fiscal via Nuvem Fiscal: configure token e dados fiscais do prestador.";
            case "none" ->
                    "Nota fiscal pendente de integracao real com SEFAZ/certificado. O fechamento fica em PENDENTE_INTEGRACAO.";
            default ->
                    "Nota fiscal com provedor customizado configurado; valide emissao e credenciais no ambiente.";
        };
    }

    private void emitirNfceNuvemFiscal(CaixaFechamento fechamento) throws IOException, InterruptedException {
        String token = sanitize(nfceNuvemFiscalToken);
        String prestadorCpfCnpj = onlyDigits(nfceNuvemFiscalPrestadorCpfCnpj);

        if (token == null) {
            throw new IllegalStateException("Token da Nuvem Fiscal nao configurado.");
        }
        if (prestadorCpfCnpj == null) {
            throw new IllegalStateException("CPF/CNPJ do prestador nao configurado para emissao da nota.");
        }

        String url = sanitizeUrl(nfceNuvemFiscalBaseUrl) + "/nfse";
        String referencia = "fechamento-" + fechamento.getId();

        Map<String, Object> payload = buildNuvemFiscalPayload(fechamento, referencia, prestadorCpfCnpj);
        String payloadJson = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Nuvem Fiscal retornou " + response.statusCode() + ": " + limitText(response.body(), 220)
            );
        }

        JsonNode root = parseJson(response.body());
        String status = lower(extractText(root,
                "/status",
                "/situacao",
                "/data/status",
                "/nfse/status",
                "/nfse/situacao"
        ));
        String chave = extractText(root,
                "/chave",
                "/chave_nfse",
                "/nfse/chave",
                "/numero",
                "/nfse/numero",
                "/id",
                "/data/id",
                "/nfse/id"
        );

        if (status.contains("autoriz") || status.contains("emitid") || status.contains("conclu")) {
            fechamento.setNfceStatus(CaixaFechamentoNfceStatus.EMITIDA);
            fechamento.setNfceChave(normalizeNfceKey(chave, referencia));
            return;
        }

        fechamento.setNfceStatus(CaixaFechamentoNfceStatus.PENDENTE_INTEGRACAO);
        fechamento.setNfceChave(normalizeNfceKey(chave, referencia));
    }

    private Map<String, Object> buildNuvemFiscalPayload(
            CaixaFechamento fechamento,
            String referencia,
            String prestadorCpfCnpj
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("-03:00"));

        BigDecimal valorServicos = safePositiveAmount(fechamento.getTotalEntradas());
        BigDecimal valorDeducoes = safePositiveAmount(fechamento.getTotalSaidas());

        Map<String, Object> prestador = new LinkedHashMap<>();
        prestador.put("cpf_cnpj", prestadorCpfCnpj);

        Map<String, Object> servico = new LinkedHashMap<>();
        servico.put("discriminacao", buildDescricaoNfse(fechamento));
        servico.put("valor_servicos", valorServicos);
        servico.put("valor_deducoes", valorDeducoes);
        servico.put("iss_retido", false);

        String codigoServico = sanitize(nfceNuvemFiscalCodigoServico);
        if (codigoServico != null) {
            servico.put("codigo_servico_municipio", codigoServico);
        }

        String itemListaServico = sanitize(nfceNuvemFiscalItemListaServico);
        if (itemListaServico != null) {
            servico.put("item_lista_servico", itemListaServico);
        }

        Map<String, Object> tomador = new LinkedHashMap<>();
        tomador.put("nome_razao_social", "Consumidor Final");
        tomador.put("cpf_cnpj", "00000000000");

        Map<String, Object> rps = new LinkedHashMap<>();
        rps.put("referencia", referencia);
        rps.put("data_emissao", now.toString());
        rps.put("competencia", now.toLocalDate().toString());
        rps.put("prestador", prestador);
        rps.put("tomador", tomador);
        rps.put("servico", servico);

        String municipioCodigo = sanitize(nfceNuvemFiscalMunicipioCodigo);
        if (municipioCodigo != null) {
            rps.put("codigo_municipio", municipioCodigo);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ambiente", sanitize(nfceNuvemFiscalAmbiente) != null ? sanitize(nfceNuvemFiscalAmbiente) : "homologacao");
        payload.put("referencia", referencia);
        payload.put("rps", rps);
        return payload;
    }

    private BigDecimal safePositiveAmount(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (value.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildDescricaoNfse(CaixaFechamento fechamento) {
        String periodo = fechamento.getPeriodo() != null ? fechamento.getPeriodo().name() : "PERIODO";
        String base = "Servicos de barbearia - fechamento " + periodo + " #" + fechamento.getId();
        String observacao = sanitize(fechamento.getObservacao());
        if (observacao == null) return base;
        return limitText(base + " - " + observacao, 350);
    }

    private JsonNode parseJson(String text) throws JsonProcessingException {
        if (text == null || text.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(text);
    }

    private String extractText(JsonNode root, String... pointers) {
        if (root == null || pointers == null) return null;

        for (String pointer : pointers) {
            if (pointer == null || pointer.isBlank()) continue;
            JsonNode node = root.at(pointer);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                String value = node.asText(null);
                if (value != null && !value.isBlank()) return value.trim();
            }
        }
        return null;
    }

    private String normalizeNfceKey(String value, String referenciaFallback) {
        String key = sanitize(value);
        if (key == null) key = referenciaFallback;
        return limitText(key, 64);
    }

    private String sanitizeUrl(String value) {
        String normalized = sanitize(value);
        if (normalized == null) return "https://api.nuvemfiscal.com.br";
        if (normalized.endsWith("/")) return normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private String sanitize(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String onlyDigits(String value) {
        String normalized = sanitize(value);
        if (normalized == null) return null;
        String digits = normalized.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    private String lower(String value) {
        String normalized = sanitize(value);
        if (normalized == null) return "";
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String limitText(String value, int maxLen) {
        if (value == null) return null;
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen);
    }

    private String generateMockNfceKey(Long fechamentoId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long id = fechamentoId != null ? fechamentoId : 0L;
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000));
        String hash = Integer.toHexString((timestamp + "-" + id + "-" + System.nanoTime()).hashCode())
                .toUpperCase(Locale.ROOT);
        return "MOCK-" + timestamp + "-" + suffix + "-" + hash;
    }

    private String providerName() {
        if (nfceProvider == null || nfceProvider.isBlank()) return "none";
        return nfceProvider.trim().toLowerCase(Locale.ROOT);
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
