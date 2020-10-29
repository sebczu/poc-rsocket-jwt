package com.sebczu.poc.rsocket.jwt;

import com.sebczu.poc.rsocket.jwt.domain.SimpleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageJsonController {

  private final ConnectorController connectorController;

  @MessageMapping("request.response.json")
  public Mono<String> requestResponse(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload SimpleMessage message) {
    log.info("message: {}", message);
    return userDetail
        .map(user -> "hello: " + user.getSubject() + " [" + connectorController.getActualConnection() + "] your message: " + message);
  }

  @MessageMapping("fire.and.forget.json")
  public Mono<Void> fireAndForget(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload SimpleMessage message) {
    log.info("message: {}", message);
    return Mono.empty();
  }

  @MessageMapping("request.stream.json")
  public Flux<SimpleMessage> requestStream(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload SimpleMessage message) {
    log.info("message: {}", message);
    return Flux.fromStream(message.getMessage().chars()
        .mapToObj(c -> new SimpleMessage(Character.toString(c))))
        .delayElements(Duration.ofMillis(1000));
  }

  @MessageMapping("request.channel.json")
  public Flux<SimpleMessage> requestChannel(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload Flux<SimpleMessage> messages) {
    return messages
        .doOnNext(message -> log.info("message: {}", message))
        .map(message -> new SimpleMessage("hello: " + message.getMessage()))
        .delayElements(Duration.ofMillis(1000));
  }
}
