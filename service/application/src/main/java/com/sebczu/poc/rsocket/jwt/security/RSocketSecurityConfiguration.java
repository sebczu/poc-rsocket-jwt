package com.sebczu.poc.rsocket.jwt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;

@Configuration
@EnableRSocketSecurity
@RequiredArgsConstructor
public class RSocketSecurityConfiguration {

  private final JwtPublicKeySupplier jwtPublicKeySupplier;

  @Bean
  public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity security) {
    return security.authorizePayload(authorizeSpec ->
        authorizeSpec.setup()
            .permitAll())
        .jwt(jwtSpec -> jwtSpec.authenticationManager(jwtReactiveAuthenticationManager()))
        .build();
  }

  @Bean
  public ReactiveAuthenticationManager jwtReactiveAuthenticationManager() {
    NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
        .withPublicKey(jwtPublicKeySupplier.get())
        .signatureAlgorithm(SignatureAlgorithm.RS256)
        .build();

    JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager = new JwtReactiveAuthenticationManager(decoder);
    jwtReactiveAuthenticationManager.setJwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter()));
    return jwtReactiveAuthenticationManager;
  }

}
