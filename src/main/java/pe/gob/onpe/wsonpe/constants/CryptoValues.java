package pe.gob.onpe.wsonpe.constants;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class CryptoValues {

  @Getter
  private static String jwtKey;

  @Getter
  private static String aesEncryptKey;

  @SuppressWarnings("unused")
  private CryptoValues(
    @Value("${stae.trans.secretKeyJwt}") String localJwtToken,
    @Value("${stae.trans.aesEncryptKey}") String localEncryptKey) {
    setKeys(localJwtToken, localEncryptKey);
  }

  private static void setKeys(String jwtTokenValue, String encryptKeyValue) {
    jwtKey = jwtTokenValue;
    aesEncryptKey = encryptKeyValue;
  }

}
