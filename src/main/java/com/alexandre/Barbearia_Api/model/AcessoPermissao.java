package com.alexandre.Barbearia_Api.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum AcessoPermissao {
    DASHBOARD_VISUALIZAR,
    USUARIOS_VISUALIZAR,
    USUARIOS_GERIR,
    USUARIOS_ALTERAR_ROLE,
    USUARIOS_ALTERAR_PERMISSOES,
    AGENDA_GERIR,
    SERVICOS_GERIR,
    INDISPONIBILIDADE_GERIR,
    COMISSOES_GERIR,
    CAIXA_GERIR;

    public String authority() {
        return "PERM_" + name();
    }

    public static Set<AcessoPermissao> parse(String raw) {
        if (raw == null || raw.isBlank()) return Set.of();

        Set<AcessoPermissao> parsed = new LinkedHashSet<>();
        for (String item : raw.split(",")) {
            String normalized = item == null ? "" : item.trim().toUpperCase();
            if (normalized.isBlank()) continue;
            try {
                parsed.add(AcessoPermissao.valueOf(normalized));
            } catch (IllegalArgumentException ignored) {
                // Ignore unknown permissions to preserve backward compatibility.
            }
        }
        return parsed;
    }

    public static String encode(Set<AcessoPermissao> permissions) {
        if (permissions == null || permissions.isEmpty()) return "";
        return permissions.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));
    }

    public static Set<AcessoPermissao> defaultsForRole(UserRole role) {
        if (role == null) return Set.of();

        return switch (role) {
            case ADMIN -> all();
            case GERENTE -> Set.of(
                    DASHBOARD_VISUALIZAR,
                    USUARIOS_VISUALIZAR,
                    USUARIOS_GERIR,
                    USUARIOS_ALTERAR_PERMISSOES,
                    AGENDA_GERIR,
                    SERVICOS_GERIR,
                    INDISPONIBILIDADE_GERIR,
                    COMISSOES_GERIR,
                    CAIXA_GERIR
            );
            case RECEPCIONISTA -> Set.of(
                    USUARIOS_VISUALIZAR,
                    AGENDA_GERIR
            );
            case BARBEIRO -> Set.of(AGENDA_GERIR);
            case USER -> Set.of();
        };
    }

    public static Set<AcessoPermissao> all() {
        return Arrays.stream(values())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
