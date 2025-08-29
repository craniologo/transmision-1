package pe.gob.onpe.wsonpe.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.gob.onpe.wsonpe.constants.CryptoValues;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class Crypto {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";

  public static final int GCM_TAG_LENGTH = 16;
  public static final int GCM_IV_LENGTH = 12;

  private static final Logger LOGGER = LoggerFactory.getLogger(Crypto.class);


  private Crypto() {
    throw new UnsupportedOperationException("notImplemented() cannot be performed because ...");
  }

  public static void decryptFileSTAE(File inputFile, File outputFile, String encKey){
    try {
      byte[] inputBytes = Files.readAllBytes(inputFile.toPath());

      SecretKeySpec key = new SecretKeySpec(encKey.getBytes(), ALGORITHM);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      byte[] initVector = Arrays.copyOfRange(inputBytes, 0, GCM_IV_LENGTH);
      GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, initVector);
      cipher.init(Cipher.DECRYPT_MODE, key, spec);
      byte[] plaintext = cipher.doFinal(inputBytes, GCM_IV_LENGTH, inputBytes.length - GCM_IV_LENGTH);

      try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
        outputStream.write(plaintext);
      }

    }catch(Exception e){
      LOGGER.error(e.getMessage(), e.getCause());
    }
  }

  public static String decryptStringAES(String encryptedText){
    String key = CryptoValues.getAesEncryptKey();
    String decryptedText = "";

    byte[] inputBytes = Base64.getDecoder().decode(encryptedText.getBytes(StandardCharsets.UTF_8));

    try {
      SecretKeySpec aesKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      byte[] initVector = Arrays.copyOfRange(inputBytes, 0, GCM_IV_LENGTH);
      GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, initVector);

      cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
      byte[] plaintext = cipher.doFinal(inputBytes, GCM_IV_LENGTH, inputBytes.length - GCM_IV_LENGTH);
      decryptedText = new String(plaintext, StandardCharsets.UTF_8);

    } catch (Exception e){
      log.error("Error desencriptando el texto: {}, {}", e.getMessage(), e.getStackTrace());
    }

    return decryptedText;
  }

  public static String encryptStringAES(String plainText){
    String key = CryptoValues.getAesEncryptKey();
    byte[] inputBytes = plainText.getBytes(StandardCharsets.UTF_8);
    String encryptedText = "";

    try {

      Key aesKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      byte[] initVector = new byte[GCM_IV_LENGTH];
      GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, initVector);

      cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
      byte[] cipherText = new byte[initVector.length + cipher.getOutputSize(inputBytes.length)];
      System.arraycopy(initVector, 0, cipherText, 0, initVector.length);

      cipher.doFinal(inputBytes, 0, inputBytes.length, cipherText, initVector.length);
      encryptedText = Base64.getEncoder().encodeToString(cipherText);

    } catch (Exception e){
      log.error("Error encriptando el texto: {}", e.getMessage());
    }

    return encryptedText;
  }

}
