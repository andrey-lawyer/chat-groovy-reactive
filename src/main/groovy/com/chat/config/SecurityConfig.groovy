package com.chat.config

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.WebFilter;
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import java.time.Duration

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        http
                .csrf((csrf) -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/public/**").permitAll()
                        .pathMatchers("/api/auth/register").permitAll()
                        .pathMatchers("/**").permitAll()
                        .pathMatchers("/api/ws/**").authenticated()
                        .pathMatchers("/api/user/current-user").authenticated()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyExchange().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/api/auth/login")
                .authenticationEntryPoint((exchange, exception) -> Mono.fromRunnable {
                            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                        })
                        .authenticationSuccessHandler(authenticationSuccessHandler())

                        .authenticationFailureHandler(authenticationFailureHandler()))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutHandler(logoutHandler())
                        .logoutSuccessHandler((exchange, authentication) ->
                                exchange.getExchange().getResponse().setComplete())
                )
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    ServerLogoutHandler logoutHandler() {
        return new SecurityContextServerLogoutHandler();
    }

    @Bean
    WebFilter webSessionConfig() {
        return (exchange, chain) -> exchange.getSession().doOnNext(webSession -> {
            webSession.setMaxIdleTime(Duration.ofMinutes(30));
        }).then(chain.filter(exchange));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        (exchange, authentication) -> {
            exchange.getExchange().getResponse().setStatusCode(HttpStatus.OK)
            return Mono.empty()
        }
    }

    @Bean
    ServerAuthenticationFailureHandler authenticationFailureHandler() {
        (exchange, exception) -> {
            println "Authentication failed: ${exception.message}"
            exchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
            return Mono.empty()
        }
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //Make the below setting as * to allow connection from any hos
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }


}



