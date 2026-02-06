package com.alexandre.Barbearia_Api.service.caixa;

import com.alexandre.Barbearia_Api.dto.caixa.CaixaCreateDTO;
import com.alexandre.Barbearia_Api.dto.caixa.CaixaResponseDTO;
import com.alexandre.Barbearia_Api.infra.exceptions.usuario.UsuarioNotFoundException;
import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.Caixa;
import com.alexandre.Barbearia_Api.model.CaixaTipo;
import com.alexandre.Barbearia_Api.model.Usuario;
import com.alexandre.Barbearia_Api.model.Comissao;
import com.alexandre.Barbearia_Api.repository.CaixaRepository;
import com.alexandre.Barbearia_Api.repository.ComissaoRepository;
import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComissaoRepository comissaoRepository;

    public CaixaService(
            CaixaRepository caixaRepository,
            UsuarioRepository usuarioRepository,
            ComissaoRepository comissaoRepository
    ) {
        this.caixaRepository = caixaRepository;
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
}
