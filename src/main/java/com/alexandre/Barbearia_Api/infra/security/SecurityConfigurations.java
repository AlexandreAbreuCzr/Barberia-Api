package com.alexandre.Barbearia_Api.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {
    private final SecurityFilter securityFilter;

    public SecurityConfigurations(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/password/forgot").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/password/reset").permitAll()

                        .requestMatchers(HttpMethod.GET, "/usuario/barbeiros").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/usuario/me").authenticated()

                        .requestMatchers(HttpMethod.GET, "/usuario/admin/**")
                            .hasAnyRole("ADMIN", "GERENTE", "RECEPCIONISTA")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/usuario/admin/*/status",
                                "/usuario/admin/*/telefone",
                                "/usuario/admin/*/name"
                        ).hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/usuario/admin/*/role").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/servico/**", "/servicos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/servico", "/servicos").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/servico/**", "/servicos/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/servico/**", "/servicos/**").hasAnyRole("ADMIN", "GERENTE")

                        .requestMatchers(HttpMethod.GET, "/agendamento/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/agendamento").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/agendamento/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/agendamento/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/indisponibilidade").hasAnyRole("BARBEIRO", "ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/indisponibilidade/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/indisponibilidade/**").hasAnyRole("BARBEIRO", "ADMIN", "GERENTE")

                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/avaliacao/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/avaliacao").permitAll()

                        .requestMatchers(HttpMethod.GET, "/comissao/taxa").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/comissao/taxa").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/comissao/**").hasAnyRole("BARBEIRO", "ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/comissao/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/caixa/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/caixa/**").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/dashboard/**").hasAnyRole("ADMIN", "GERENTE")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }




    @Bean
    public AuthenticationManager authenticationManager(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
