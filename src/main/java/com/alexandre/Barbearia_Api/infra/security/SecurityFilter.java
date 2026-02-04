package com.alexandre.Barbearia_Api.infra.security;

import com.alexandre.Barbearia_Api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = recoverToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String subject = tokenService.validateToken(token); // subject do JWT

                if (subject != null && !subject.isBlank()) {
                    usuarioRepository.findByUsername(subject)
                            .or(() -> usuarioRepository.findByEmail(subject))
                            .ifPresent(usuario -> {


                                if (!usuario.isEnabled()) return;

                                var authentication = new UsernamePasswordAuthenticationToken(
                                        usuario,
                                        null,
                                        usuario.getAuthorities()
                                );

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            });
                }
            } catch (Exception ignored) {

            }
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // auth
        if (path.equals("/auth/login") || path.equals("/auth/register")) return true;

        // serviços públicos (se no seu security tá permitAll no GET)
        if (path.startsWith("/servicos") && HttpMethod.GET.matches(method)) return true;

        // preflight já tratado, mas aqui também pode
        if (HttpMethod.OPTIONS.matches(method)) return true;

        return false;
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7);
    }
}
