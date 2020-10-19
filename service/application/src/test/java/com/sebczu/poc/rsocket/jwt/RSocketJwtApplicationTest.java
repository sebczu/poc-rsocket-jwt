package com.sebczu.poc.rsocket.jwt;

import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RSocketJwtApplicationTest {

  protected final static MimeType AUTHENTICATION_MIME_TYPE = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
  protected final static MimeType BEARER_MIME_TYPE = new MimeType("message", "x.rsocket.authentication.bearer.v0");

  @Autowired
  protected JwtTokenGenerator tokenGenerator;
  @Autowired
  protected RSocketRequester.Builder builder;
  @LocalRSocketServerPort
  protected Integer port;
  protected RSocketRequester requester;

  protected void buildRequester(RSocketRequester.Builder builder) {
    requester = builder
        .dataMimeType(MediaType.TEXT_PLAIN)
        .connect(WebsocketClientTransport.create(port))
        .block();
  }

  protected void buildRequester(String jwtToken) {
    buildRequester(builder
        .setupMetadata(jwtToken, BEARER_MIME_TYPE));
  }

  @AfterEach
  public void disposeRequester() {
    requester.rsocket().dispose();
  }

}
