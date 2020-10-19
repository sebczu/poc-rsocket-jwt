package com.sebczu.poc.rsocket.jwt;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;


public class ValidRSocketJwtApplicationTest extends RSocketJwtApplicationTest {

  @Test
  public void shouldConnectWhenValidToken() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenGenerator.generateTokenJWT("test"));

    Mono<Void> res = requester.rsocket()
        .onClose();

    StepVerifier
        .create(res)
        .expectComplete();
  }

  @Test
  public void shouldInvokeSetupConnectionWhenValidToken() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(builder
        .setupMetadata(tokenGenerator.generateTokenJWT("test"), BEARER_MIME_TYPE)
        .setupRoute("setup")
        .setupData("client-id"));

    Mono<Void> res = requester.rsocket()
        .onClose();

    StepVerifier
        .create(res)
        .expectComplete();
  }

}
