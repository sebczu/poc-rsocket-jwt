package com.sebczu.poc.rsocket.jwt.security.exception;

public class JwtPublicKeyException extends RuntimeException {

  public JwtPublicKeyException(Exception e) {
    super("jwt public-key exception", e);
  }

}
