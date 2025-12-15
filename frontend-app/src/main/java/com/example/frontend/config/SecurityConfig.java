package com.example.frontend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;

import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Security configuration for the frontend application using OAuth2/OIDC with Keycloak.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Role required to access secured endpoints.
     */
    @Value("${app.security.required-role:my-role}")
    private String requiredRole;

    /**
     * Flag to enforce two-factor authentication (2FA).
     */
    @Value("${app.security.require-2fa:false}")
    private boolean require2fa;

    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
     * Configures the JWT decoder using issuer URI from Keycloak client registration.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        var clientRegistration = clientRegistrationRepository.findByRegistrationId("keycloak");
        String issuerUri = clientRegistration.getProviderDetails().getIssuerUri();
        logger.info("Initializing JwtDecoder with issuer URI: {}", issuerUri);
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    /**
     * Security filter chain that defines:
     * - Public endpoints for login and static resources.
     * - Secured endpoints requiring specific role.
     * - OAuth2 login with a custom OIDC user service to map roles.
     * - Logout handler that initiates Keycloak logout and redirects back.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        logger.info("Configuring security filter chain");

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login**", "/css/**").permitAll()
                .requestMatchers("/", "/home", "/calendar").hasRole(requiredRole)
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> 
                    userInfo.oidcUserService(customOidcUserService(jwtDecoder))
                )
            )
            .logout(logout -> {
                logout.invalidateHttpSession(true)
                      .clearAuthentication(true)
                      .deleteCookies("JSESSIONID");
                var logoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
                logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
                logout.logoutSuccessHandler(logoutSuccessHandler);
            });

        return http.build();
    }

    /**
     * Custom OIDC user service to map Keycloak realm roles to Spring Security authorities.
     * Combines roles from 'realm_access' claim with existing user authorities.
     */
    private OidcUserService customOidcUserService(JwtDecoder jwtDecoder) {
        OidcUserService delegate = new OidcUserService();

        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                logger.debug("Loading user with custom OIDC user service");

                OidcUser oidcUser = delegate.loadUser(userRequest);

                Jwt jwt = jwtDecoder.decode(userRequest.getAccessToken().getTokenValue());
                Map<String, Object> claims = jwt.getClaims();

                Object realmAccess = claims.get("realm_access");
                if (realmAccess instanceof Map<?, ?> realmAccessMap) {
                    Object rolesObj = realmAccessMap.get("roles");
                    if (rolesObj instanceof List<?>) {
                        List<String> roles = ((List<?>) rolesObj).stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());

                        logger.debug("Realm roles extracted from token: {}", roles);

                        Set<GrantedAuthority> mappedAuthorities = roles.stream()
                                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                                .collect(Collectors.toSet());

                        // Merge with existing authorities
                        Set<GrantedAuthority> combinedAuthorities = new java.util.HashSet<>(oidcUser.getAuthorities());
                        combinedAuthorities.addAll(mappedAuthorities);

                        return new DefaultOidcUser(combinedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
                    }
                }

                return oidcUser;
            }
        };
    }

    /**
     * Custom authorization manager (optional) to enforce role and 2FA if needed.
     * Not wired by default; can be used for fine-grained access control.
     */
    private AuthorizationManager<RequestAuthorizationContext> customAuthorizationManager() {
        return (authentication, context) -> {
            Authentication auth = authentication.get();

            if (auth == null || !auth.isAuthenticated()) {
                return new AuthorizationDecision(false);
            }

            boolean hasRequiredRole = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equalsIgnoreCase("ROLE_" + requiredRole));

            if (!hasRequiredRole) {
                return new AuthorizationDecision(false);
            }

            if (require2fa && auth.getPrincipal() instanceof OidcUser oidcUser) {
                Object amrClaim = oidcUser.getClaims().get("amr");
                if (amrClaim instanceof List<?>) {
                    List<?> amrList = (List<?>) amrClaim;
                    boolean mfaCompleted = amrList.stream()
                        .map(Object::toString)
                        .anyMatch(method -> method.toLowerCase().contains("mfa")
                                || method.toLowerCase().contains("otp")
                                || method.toLowerCase().contains("2fa"));
                    return new AuthorizationDecision(mfaCompleted);
                }
                return new AuthorizationDecision(false);
            }

            return new AuthorizationDecision(true);
        };
    }
}
