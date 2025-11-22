package com.sms.SERVICENAME.security;  // TODO: Replace SERVICENAME with your service name

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * Purpose: Intercepts HTTP requests to extract and validate JWT tokens
 *
 * CUSTOMIZATION REQUIRED:
 * 1. Update package name: Replace 'SERVICENAME' with your service name
 * 2. Implement UserDetailsService if not using default
 *
 * NO OTHER CHANGES NEEDED - This template works as-is after package rename
 *
 * How it works:
 * 1. Extract JWT from Authorization header
 * 2. Validate token (delegates to JwtTokenProvider)
 * 3. Load user details from database
 * 4. Set Spring Security authentication context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract JWT from Authorization header
            String jwt = getJwtFromRequest(request);

            // Validate token and set authentication
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Extract user ID from token
                java.util.UUID userId = tokenProvider.getUserIdFromToken(jwt);

                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // Always continue filter chain (allows public endpoints to work)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT from Authorization header
     * Expected format: "Bearer {token}"
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " prefix
        }
        return null;
    }
}
