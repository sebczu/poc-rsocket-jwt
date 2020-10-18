package com.sebczu.poc.rsocket.jwt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Configuration
@ConfigurationProperties(prefix = "poc.rsocket.jwt")
public class JwtPrivateKeyProperty {

  private String privateKey;

  public void setPrivateKey(String privateKey) {
    log.info("set private key: " + privateKey);
    this.privateKey = privateKey;
  }

}
