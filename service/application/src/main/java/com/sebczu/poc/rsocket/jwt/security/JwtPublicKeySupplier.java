package com.sebczu.poc.rsocket.jwt.security;

import com.sebczu.poc.rsocket.jwt.security.exception.JwtPublicKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtPublicKeySupplier implements Supplier<RSAPublicKey> {

  private final JwtPublicKeyProperty property;
  private final static Base64.Decoder decoder = Base64.getMimeDecoder();

  @Override
  public RSAPublicKey get() {
    try {
      String publicKey = property.getPublicKey();
      byte[] decoded = decoder.decode(getBodyPublicKey(publicKey));
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    } catch (NoSuchAlgorithmException e) {
      throw new JwtPublicKeyException(e);
    } catch (InvalidKeySpecException e) {
      throw new JwtPublicKeyException(e);
    }
  }

  private String getBodyPublicKey(String publicKey) {
    String publicKeyBody = publicKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
    return publicKeyBody.replace("\n-----END PUBLIC KEY-----", "");
  }
}
