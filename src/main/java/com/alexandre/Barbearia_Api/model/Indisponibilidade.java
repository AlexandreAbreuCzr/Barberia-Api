package com.alexandre.Barbearia_Api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "indisponibilidade")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Indisponibilidade {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Usuario barbeiro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoIndisponibilidade tipo;


    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @CreationTimestamp
    private LocalDateTime dataDeCriacao;

    @UpdateTimestamp
    private LocalDateTime dataDeModificacao;

}
