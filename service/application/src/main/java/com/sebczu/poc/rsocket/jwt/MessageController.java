package com.sebczu.poc.rsocket.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageController {

  private final ConnectorController connectorController;

  @MessageMapping("request.response")
  public Mono<String> requestResponse(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload String message) {
    log.info("message: {}", message);
    return userDetail
        .map(user -> "hello: " + user.getSubject() + " [" + connectorController.getActualConnection() + "] your message: " + message);
  }

  @MessageMapping("fire.and.forget")
  public Mono<Void> fireAndForget(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload String message) {
    log.info("message: {}", message);
    return Mono.empty();
  }

  @MessageMapping("request.stream")
  public Flux<String> requestStream(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload String message) {
    log.info("message: {}", message);
    return Flux.fromStream(message.chars()
        .mapToObj(Character::toString))
        .delayElements(Duration.ofMillis(1000));
  }

  @MessageMapping("request.channel")
  public Flux<String> requestChannel(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload Flux<String> messages) {
    return messages
        .doOnNext(message -> log.info("message: {}", message))
        .map(message -> "hello: " + message)
        .delayElements(Duration.ofMillis(1000));
  }
}
