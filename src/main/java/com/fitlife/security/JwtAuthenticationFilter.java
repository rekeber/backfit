package com.fitlife.security;

import com.fitlife.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info("JWT Filter processing: {} {}", method, requestURI);

        String token = getTokenFromRequest(request);
        log.info("Token extracted: {}", token != null ? "Present (length: " + token.length() + ")" : "Not present");

        if (token != null) {
            boolean isValid = jwtTokenProvider.validateToken(token);
            log.info("Token validation result: {}", isValid);
            
            if (isValid) {
                try {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    log.info("Email extracted from token: {}", email);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    log.info("User details loaded for: {}", email);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authentication set in SecurityContext for user: {}", email);
                } catch (Exception e) {
                    log.error("Error processing JWT token: ", e);
                }
            }
        } else {
            log.info("No token found in request headers for: {} {}", method, requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken != null ? "Bearer ***" : "null");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}