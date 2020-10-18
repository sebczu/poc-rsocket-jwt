package com.sebczu.poc.rsocket.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sebczu.poc.rsocket.jwt.security.JwtPublicKeyProperty;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Stream;

@Slf4j
public class JwtTokenGenerator extends RSocketJwtApplicationTest {

  private final static JWSAlgorithm ALGORITHM = JWSAlgorithm.RS256;
  private final static Base64.Decoder DECODER = Base64.getMimeDecoder();

  @Autowired
  private JwtPublicKeyProperty publicKeyProperty;
  @Autowired
  private JwtPrivateKeyProperty privateKeyProperty;

  @ParameterizedTest
  @MethodSource("input")
  public void generateTokenJWT(String subject) throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException, ParseException {
    String privateJwk = privatePemToJwk(privateKeyProperty.getPrivateKey(), publicKeyProperty.getPublicKey());

    RSAKey rsaPrivate = RSAKey.parse(privateJwk);
    JWSSigner signer = new RSASSASigner(rsaPrivate);

    SignedJWT jwt = new SignedJWT(getHeader(), getClaims(subject, Period.ofYears(100)));
    jwt.sign(signer);
    String tokenJwt = jwt.serialize();
    System.out.println("token:");
    System.out.println(tokenJwt);
  }

  private static Stream<Arguments> input() {
    return Stream.of(
        Arguments.of("user1"),
        Arguments.of("user2")
    );
  }

  public String privatePemToJwk(String privatePem, String publicPem) throws InvalidKeySpecException, NoSuchAlgorithmException {
      RSAPrivateKey rsaPrivateKey = privateKeyPemToRSA(privatePem);
      RSAPublicKey rsaPublicKey = publicKeyPemToRSA(publicPem);

      RSAKey jwk = new RSAKey.Builder(rsaPublicKey)
          .privateKey(rsaPrivateKey)
          .build();
      String privateKey = jwk.toJSONString();

      System.out.println("converted private key[jwk]:");
      System.out.println(privateKey);
      return  privateKey;
  }

  private RSAPublicKey publicKeyPemToRSA(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] decoded = DECODER.decode(getBodyPublicKey(pem));
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
  }

  private RSAPrivateKey privateKeyPemToRSA(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] decoded = DECODER.decode(getBodyPrivateKey(pem));
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
  }

  private String getBodyPublicKey(String publicKey) {
    String publicKeyBody = publicKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
    return publicKeyBody.replace("\n-----END PUBLIC KEY-----", "");
  }

  private String getBodyPrivateKey(String privateKey) {
    String privateKeyBody = privateKey.replace("-----BEGIN PRIVATE KEY-----\n", "");
    return privateKeyBody.replace("\n-----END PRIVATE KEY-----", "");
  }

  private JWSHeader getHeader() {
    return new JWSHeader.Builder(ALGORITHM)
        .build();
  }

  private JWTClaimsSet getClaims(String subject, TemporalAmount duration) {
    return new JWTClaimsSet.Builder()
        .subject(subject)
        .claim("scope", "USER")
        .expirationTime(getExpirationDate(duration))
        .build();
  }

  private Date getExpirationDate(TemporalAmount duration) {
    LocalDateTime expirationDate = LocalDateTime.now().plus(duration);
    return Timestamp.valueOf(expirationDate);
  }
}
