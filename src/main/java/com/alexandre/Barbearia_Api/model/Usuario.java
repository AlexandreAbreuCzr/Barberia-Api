package com.alexandre.Barbearia_Api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(columnDefinition = "TEXT")
    private String permissoes = "";

    @Column(nullable = false)
    private boolean status = true;

    @CreationTimestamp
    private LocalDateTime dataDeCriacao;

    @UpdateTimestamp
    private LocalDateTime dataDeModificacao;



    public Usuario(String username, String name, String email, String password, UserRole role) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(() -> "ROLE_" + role.name());

        for (AcessoPermissao permissao : getPermissoesEfetivas()) {
            authorities.add(permissao::authority);
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.status);
    }

    public Set<AcessoPermissao> getPermissoesEfetivas() {
        if (role == UserRole.ADMIN || role == UserRole.DONO) {
            return AcessoPermissao.defaultsForRole(role);
        }
        Set<AcessoPermissao> custom = AcessoPermissao.parse(permissoes);
        if (custom.isEmpty()) {
            return AcessoPermissao.defaultsForRole(role);
        }
        return custom;
    }

    public void setPermissoesEfetivas(Set<AcessoPermissao> permissaoSet) {
        this.permissoes = AcessoPermissao.encode(permissaoSet);
    }
}
