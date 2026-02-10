package com.alexandre.Barbearia_Api.service.comissao;

import com.alexandre.Barbearia_Api.dto.comissao.ComissaoResponseDTO;
import com.alexandre.Barbearia_Api.dto.comissao.ComissaoUpdateDTO;
import com.alexandre.Barbearia_Api.dto.comissao.ComissaoConfigDTO;
import com.alexandre.Barbearia_Api.dto.comissao.ComissaoTaxaUpdateDTO;
import com.alexandre.Barbearia_Api.dto.usuario.UsuarioResponseDTO;
import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.Comissao;
import com.alexandre.Barbearia_Api.model.ComissaoConfig;
import com.alexandre.Barbearia_Api.repository.ComissaoRepository;
import com.alexandre.Barbearia_Api.repository.ComissaoConfigRepository;
import com.alexandre.Barbearia_Api.service.usuario.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComissaoService {

    private static final BigDecimal PERCENTUAL_PADRAO = new BigDecimal("50");

    private final ComissaoRepository comissaoRepository;
    private final ComissaoConfigRepository comissaoConfigRepository;
    private final UsuarioService usuarioService;

    public ComissaoService(
            ComissaoRepository comissaoRepository,
            ComissaoConfigRepository comissaoConfigRepository,
            UsuarioService usuarioService
    ) {
        this.comissaoRepository = comissaoRepository;
        this.comissaoConfigRepository = comissaoConfigRepository;
        this.usuarioService = usuarioService;
    }

    public Comissao createForAgendamento(Agendamento agendamento) {
        return comissaoRepository.findByAgendamento_Id(agendamento.getId())
                .orElseGet(() -> {
                    BigDecimal percentual = getPercentualPadrao();
                    BigDecimal valor = agendamento.getServico().getPrice()
                            .multiply(percentual)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    Comissao comissao = new Comissao();
                    comissao.setAgendamento(agendamento);
                    comissao.setBarbeiro(agendamento.getBarbeiro());
                    comissao.setServico(agendamento.getServico());
                    comissao.setPercentual(percentual);
                    comissao.setValor(valor);

                    return comissaoRepository.save(comissao);
                });
    }

    public List<ComissaoResponseDTO> find(String barbeiroUsername, LocalDate inicio, LocalDate fim) {
        UsuarioResponseDTO usuario = usuarioService.getUsuarioAutenticado();
        String role = usuario.role();
        String filtroBarbeiro = barbeiroUsername;

        if (role != null && role.equals("FUNCIONARIO")) {
            filtroBarbeiro = usuario.username();
        }

        LocalDateTime dataInicio = inicio != null ? inicio.atStartOfDay() : null;
        LocalDateTime dataFim = fim != null ? fim.atTime(23, 59, 59) : null;

        List<Comissao> lista;
        if (filtroBarbeiro != null && dataInicio != null && dataFim != null) {
            lista = comissaoRepository.findByBarbeiro_UsernameAndDataDeCriacaoBetween(filtroBarbeiro, dataInicio, dataFim);
        } else if (filtroBarbeiro != null) {
            lista = comissaoRepository.findByBarbeiro_Username(filtroBarbeiro);
        } else if (dataInicio != null && dataFim != null) {
            lista = comissaoRepository.findByDataDeCriacaoBetween(dataInicio, dataFim);
        } else {
            lista = comissaoRepository.findAll();
        }

        return lista.stream().map(this::toResponse).toList();
    }

    public ComissaoResponseDTO updatePercentual(Long id, ComissaoUpdateDTO dto) {
        Comissao comissao = comissaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comissao não encontrada"));

        BigDecimal percentual = dto.percentual();
        if (percentual == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentual inválido");
        }
        if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentual deve estar entre 0 e 100");
        }

        BigDecimal valor = comissao.getAgendamento().getServico().getPrice()
                .multiply(percentual)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        comissao.setPercentual(percentual);
        comissao.setValor(valor);

        return toResponse(comissaoRepository.save(comissao));
    }

    public ComissaoConfigDTO getTaxaGlobal() {
        return new ComissaoConfigDTO(getPercentualPadrao());
    }

    public ComissaoConfigDTO updateTaxaGlobal(ComissaoTaxaUpdateDTO dto) {
        BigDecimal percentual = dto.percentual();
        if (percentual == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentual inválido");
        }
        if (percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentual deve estar entre 0 e 100");
        }

        ComissaoConfig config = getOrCreateConfig();
        config.setPercentual(percentual);
        comissaoConfigRepository.save(config);

        if (Boolean.TRUE.equals(dto.aplicarEmTodas())) {
            List<Comissao> lista = comissaoRepository.findAll();
            for (Comissao comissao : lista) {
                BigDecimal novoValor = comissao.getAgendamento().getServico().getPrice()
                        .multiply(percentual)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                comissao.setPercentual(percentual);
                comissao.setValor(novoValor);
            }
            comissaoRepository.saveAll(lista);
        }

        return new ComissaoConfigDTO(percentual);
    }

    private ComissaoResponseDTO toResponse(Comissao comissao) {
        return new ComissaoResponseDTO(
                comissao.getId(),
                comissao.getAgendamento().getId(),
                comissao.getBarbeiro().getUsername(),
                comissao.getBarbeiro().getName(),
                comissao.getServico().getName(),
                comissao.getPercentual(),
                comissao.getValor(),
                comissao.getDataDeCriacao()
        );
    }

    private ComissaoConfig getOrCreateConfig() {
        return comissaoConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    ComissaoConfig config = new ComissaoConfig();
                    config.setPercentual(PERCENTUAL_PADRAO);
                    return comissaoConfigRepository.save(config);
                });
    }

    private BigDecimal getPercentualPadrao() {
        return getOrCreateConfig().getPercentual();
    }
}
