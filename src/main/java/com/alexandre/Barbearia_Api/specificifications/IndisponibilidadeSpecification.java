package com.alexandre.Barbearia_Api.specificifications;

import com.alexandre.Barbearia_Api.model.Indisponibilidade;
import com.alexandre.Barbearia_Api.model.TipoIndisponibilidade;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class IndisponibilidadeSpecification {

    public static Specification<Indisponibilidade> barbeiroUsername(String username) {
        return (root, query, cb) ->
                cb.equal(root.get("barbeiro").get("username"), username);
    }

    public static Specification<Indisponibilidade> tipo(TipoIndisponibilidade tipo) {
        return (root, query, cb) ->
                cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Indisponibilidade> overlap(LocalDateTime inicio, LocalDateTime fim) {
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("inicio"), fim),
                cb.greaterThanOrEqualTo(root.get("fim"), inicio)
        );
    }
}
