package com.sebczu.poc.rsocket.jwt;

import com.nimbusds.jose.JOSEException;
import io.rsocket.exceptions.RejectedSetupException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidRSocketJwtApplicationTest extends RSocketJwtApplicationTest {

  @Test
  public void whenInvalidTokenStructureShouldRejectSetup() {
    buildRequester("invalid");

    verifyReject("An error occurred while attempting to decode the Jwt: Invalid JWT serialization: Missing dot delimiter(s)");
  }

  @Test
  public void whenInvalidTokenShouldRejectSetup() {
    buildRequester("header.payload,signature");

    verifyReject("An error occurred while attempting to decode the Jwt: Invalid unsecured/JWS/JWE header: Invalid JSON");
  }

  @Test
  public void whenInvalidSetupMimeTypeShouldRejectSetup() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(builder
        .setupMetadata(tokenGenerator.generateTokenJWT("test"), AUTHENTICATION_MIME_TYPE));

    verifyReject("Access Denied");
  }

  @Test
  public void whenTokenExpiredShouldRejectSetup() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenGenerator.generateTokenJWT(Duration.ofMinutes(-10)));

    verifyReject("Jwt expired");
  }

  private void verifyReject(String expectedMessage) {
    Mono<Void> result = requester.rsocket()
        .onClose();

    StepVerifier
        .create(result)
        .expectErrorSatisfies(ex -> {
          assertThat(ex)
              .isInstanceOf(RejectedSetupException.class)
              .hasMessageContaining(expectedMessage);
        })
        .verify();
  }

}
