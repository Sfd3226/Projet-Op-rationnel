package com.transfert.transfertargent.config;

import com.transfert.transfertargent.models.User;
import com.transfert.transfertargent.services.JwtService;
import com.transfert.transfertargent.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Récupérer le header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String telephone;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le JWT
        jwt = authHeader.substring(7); // Retirer "Bearer "
        // Extraire le téléphone (username) depuis le token
        telephone = jwtService.extractTelephone(jwt);

        // Vérifier que l'utilisateur n'est pas encore authentifié
        if (telephone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Charger l'utilisateur réel depuis la base
            User user = userDetailsService.loadUserByUsername(telephone);

            // Vérifier la validité du token
            if (jwtService.isTokenValid(jwt, user)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Placer l'utilisateur dans le contexte Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Passer la requête au filtre suivant
        filterChain.doFilter(request, response);
    }
}
