package com.sebczu.poc.rsocket.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class ConnectorController {

  private Map<String, RSocketRequester> requesters = new ConcurrentHashMap<>();

  @ConnectMapping("setup")
  public void setup(@AuthenticationPrincipal Mono<Jwt> userDetail, @Headers Map<String,Object> headers, RSocketRequester requester, @Payload String client) {
    showHeaders(headers);

    requester.rsocket()
        .onClose()
        .doFirst(() -> {
          log.info("client: {} connected.", client);
          userDetail.subscribe(user -> {
            log.info("user: {} added.", user.getSubject());
            requesters.put(user.getSubject(), requester);
          });
        })
        .doOnError(error -> {
          log.warn("client: " + client + " error: ", error);
        })
        .doFinally(consumer -> {
          log.info("client: {} disconnected", client);
          userDetail.subscribe(user -> {
            log.info("user: {} deleted.", user.getSubject());
            requesters.remove(user.getSubject());
          });
        })
        .subscribe();
  }

  public int getActualConnection() {
    return requesters.size();
  }

  private void showHeaders(Map<String,Object> headers) {
    headers.forEach((key, value) -> log.info("{} -> {}", key, value));
  }

}
