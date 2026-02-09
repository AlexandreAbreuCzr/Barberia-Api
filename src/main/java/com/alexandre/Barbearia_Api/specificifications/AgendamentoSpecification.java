package com.alexandre.Barbearia_Api.specificifications;

import com.alexandre.Barbearia_Api.model.Agendamento;
import com.alexandre.Barbearia_Api.model.AgendamentoStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendamentoSpecification {

    public static Specification<Agendamento> filtro(
            String clienteUsername,
            String barbeiroUsername,
            Long servicoId,
            LocalDate data,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime hora,
            AgendamentoStatus status,
            Boolean semBarbeiro
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (clienteUsername != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("cliente").get("username")),
                                clienteUsername.toLowerCase()
                        )
                );
            }

            if (barbeiroUsername != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("barbeiro").get("username")),
                                barbeiroUsername.toLowerCase()
                        )
                );
            }

            if (semBarbeiro != null) {
                if (semBarbeiro) {
                    predicates.add(cb.isNull(root.get("barbeiro")));
                } else {
                    predicates.add(cb.isNotNull(root.get("barbeiro")));
                }
            }

            if (servicoId != null) {
                predicates.add(
                        cb.equal(root.get("servico").get("id"), servicoId)
                );
            }

            if (data != null) {
                predicates.add(
                        cb.equal(root.get("data"), data)
                );
            }

            if (dataInicio != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("data"), dataInicio)
                );
            }

            if (dataFim != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("data"), dataFim)
                );
            }

            if (hora != null) {
                predicates.add(
                        cb.equal(root.get("hora"), hora)
                );
            }

            if (status != null) {
                predicates.add(
                        cb.equal(root.get("agendamentoStatus"), status)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
