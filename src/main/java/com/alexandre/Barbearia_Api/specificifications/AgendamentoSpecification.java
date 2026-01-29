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
            String clienteName,
            String barbeiroName,
            Long servicoId,
            LocalDate data,
            LocalTime hora,
            AgendamentoStatus status
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (clienteName != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("cliente").get("name")),
                                clienteName.toLowerCase()
                        )
                );
            }

            if (barbeiroName != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("barbeiro").get("name")),
                                barbeiroName.toLowerCase()
                        )
                );
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
