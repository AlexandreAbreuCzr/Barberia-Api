package com.alexandre.Barbearia_Api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servico")
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer duracaoMediaEmMinutos;

    @Column(name = "percentual_comissao", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualComissao = new BigDecimal("50.00");

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean status = true;

    @CreationTimestamp
    private LocalDateTime dataDeCriacao;

    @UpdateTimestamp
    private LocalDateTime dataDeModificacao;


}
