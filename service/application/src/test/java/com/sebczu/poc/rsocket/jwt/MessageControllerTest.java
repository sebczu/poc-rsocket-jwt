package com.sebczu.poc.rsocket.jwt;

import com.nimbusds.jose.JOSEException;
import io.rsocket.exceptions.ApplicationErrorException;
import io.rsocket.exceptions.RejectedSetupException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageControllerTest extends RSocketJwtApplicationTest {

  private final static String FNF_CHANNEL = "fire.and.forget";
  private final static String RR_CHANNEL = "request.response";
  private final static String RS_CHANNEL = "request.stream";
  private final static String RC_CHANNEL = "request.channel";

  @Test
  public void whenNoTokenShouldNotConnectToChannel() {
    buildRequester(builder);

    Mono<Void> result = requester
        .route(FNF_CHANNEL)
        .data("test")
        .retrieveMono(Void.class);

    StepVerifier
        .create(result)
        .expectErrorSatisfies(ex -> {
          assertThat(ex)
              .isInstanceOf(RejectedSetupException.class)
              .hasMessageContaining("Access Denied");
        })
        .verify();
  }

  @Test
  public void whenChannelNotExistShouldReturnError() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenFactory.generateTokenJWT("test"));

    Mono<Void> result = requester
        .route("invalid")
        .data("test")
        .retrieveMono(Void.class);

    StepVerifier
        .create(result)
        .expectErrorSatisfies(ex -> {
          assertThat(ex)
              .isInstanceOf(ApplicationErrorException.class)
              .hasMessageContaining("No handler for destination 'invalid'");
        })
        .verify();
  }

  @Test
  public void fireAndForgetTest() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenFactory.generateTokenJWT("test"));

    Mono<Void> result = requester
        .route(FNF_CHANNEL)
        .data("test")
        .retrieveMono(Void.class);

    StepVerifier
        .create(result)
        .verifyComplete();
  }

  @Test
  public void requestResponseTest() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenFactory.generateTokenJWT("test"));

    Mono<String> result = requester
        .route(RR_CHANNEL)
        .data("test")
        .retrieveMono(String.class);

    StepVerifier
        .create(result)
        .expectNextMatches(message -> message.contains("hello") && message.contains("test"))
        .verifyComplete();
  }

  @Test
  public void requestStreamTest() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenFactory.generateTokenJWT("test"));

    Flux<String> result = requester
        .route(RS_CHANNEL)
        .data("test")
        .retrieveFlux(String.class);

    StepVerifier
        .create(result)
        .expectNext("t", "e", "s", "t")
        .verifyComplete();
  }

  @Test
  public void requestChannelTest() throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
    buildRequester(tokenFactory.generateTokenJWT("test"));

    Flux<String> request = Flux.just("1", "2", "3");

    Flux<String> result = requester
        .route(RC_CHANNEL)
        .data(request)
        .retrieveFlux(String.class);

    StepVerifier
        .create(result)
        .expectNext("hello: 1", "hello: 2", "hello: 3")
        .verifyComplete();
  }

}
