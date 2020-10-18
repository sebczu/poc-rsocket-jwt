package com.sebczu.poc.rsocket.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageController {

  private final ConnectorController connectorController;

  @MessageMapping("hello")
  public Mono<String> hello(@AuthenticationPrincipal Mono<Jwt> userDetail, @Payload String message) {
    log.info("message: {}", message);
    return userDetail.map(user -> "hello: " + user.getSubject() + " [" + connectorController.getActualConnection() + "] your message: " + message);
  }

}
