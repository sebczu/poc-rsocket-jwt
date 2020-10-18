package com.sebczu.poc.rsocket.jwt.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
@ConfigurationProperties(prefix = "poc.rsocket.jwt")
public class JwtPublicKeyProperty {

  private String publicKey;

  public void setPublicKey(String publicKey) {
    log.info("set public key: " + publicKey);
    this.publicKey = publicKey;
  }

}
