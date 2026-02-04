package com.alexandre.Barbearia_Api.infra.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

                        // ===== AUTH =====
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // ===== USUÁRIO =====
                        .requestMatchers(HttpMethod.GET, "/usuario/me").authenticated()

                        // ADMIN - usuários
                        .requestMatchers(HttpMethod.GET, "/usuario/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/usuario/admin/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/usuario/admin/*/role").hasRole("ADMIN")

                        // ===== SERVIÇOS =====
                        .requestMatchers(HttpMethod.GET, "/servicos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/servicos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/servicos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/servicos/**").hasRole("ADMIN")

                        // ===== AGENDAMENTO =====
                        .requestMatchers(HttpMethod.GET, "/agendamento/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/agendamento").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/agendamento/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/agendamento/**").authenticated()

                        // ===== INDISPONIBILIDADE =====
                        .requestMatchers(HttpMethod.POST, "/indisponibilidade").hasAnyRole("BARBEIRO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/indisponibilidade/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/indisponibilidade/**").hasAnyRole("BARBEIRO", "ADMIN")


                        // ===== FALLBACK =====
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }




    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
