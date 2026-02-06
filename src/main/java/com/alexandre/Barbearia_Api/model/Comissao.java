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
@Table(name = "comissao")
public class Comissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @ManyToOne
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Usuario barbeiro;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentual;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @CreationTimestamp
    private LocalDateTime dataDeCriacao;
}
