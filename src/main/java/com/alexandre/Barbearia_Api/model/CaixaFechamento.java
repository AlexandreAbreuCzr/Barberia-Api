package com.alexandre.Barbearia_Api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "caixa_fechamento")
public class CaixaFechamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaixaFechamentoPeriodo periodo;

    @Column(nullable = false)
    private LocalDateTime dataInicio;

    @Column(nullable = false)
    private LocalDateTime dataFim;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEntradas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSaidas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoApurado;

    @Column(precision = 12, scale = 2)
    private BigDecimal saldoInformado;

    @Column(precision = 12, scale = 2)
    private BigDecimal diferenca;

    @Column(nullable = false)
    private Long totalLancamentos;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private Boolean solicitarNfce = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaixaFechamentoNfceStatus nfceStatus = CaixaFechamentoNfceStatus.NAO_SOLICITADA;

    @Column(length = 64)
    private String nfceChave;

    @ManyToOne
    @JoinColumn(name = "fechado_por_id")
    private Usuario fechadoPor;

    @CreationTimestamp
    private LocalDateTime dataDeCriacao;
}
