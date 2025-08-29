package pe.gob.onpe.wsonpe.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pe.gob.onpe.wsonpe.constants.CryptoValues;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class CryptoTest {

  private static final String TEST_KEY = "1234567890ABCDEF"; // Clave de 16 bytes para pruebas
  private static final String PLAIN_TEXT = "Texto a encriptar para prueba";

  @BeforeEach
  void setUp() {
    // Configurar mock para CryptoValues
    try (MockedStatic<CryptoValues> cryptoValuesMock = Mockito.mockStatic(CryptoValues.class)) {
      cryptoValuesMock.when(CryptoValues::getAesEncryptKey).thenReturn(TEST_KEY);
    }
  }

  @Test
  void testPrivateConstructor() throws Exception {
    // Usar reflexión para acceder al constructor privado
    Constructor<Crypto> constructor = Crypto.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    try {
      // Intentar crear una instancia
      constructor.newInstance();
      fail("Se esperaba que el constructor lanzara una excepción");
    } catch (InvocationTargetException e) {
      // Verificar que la causa sea UnsupportedOperationException
      Throwable cause = e.getCause();
      assertTrue(cause instanceof UnsupportedOperationException,
        "La causa debe ser UnsupportedOperationException, pero fue: " + cause.getClass().getName());

      // Verificar el mensaje de la excepción
      String expectedMessage = "notImplemented() cannot be performed because";
      assertTrue(cause.getMessage().contains(expectedMessage),
        "El mensaje de excepción debe contener: " + expectedMessage);
    }
  }

  @Test
  void testEncryptDecryptString() {
    // Configurar mock para CryptoValues
    try (MockedStatic<CryptoValues> cryptoValuesMock = Mockito.mockStatic(CryptoValues.class)) {
      cryptoValuesMock.when(CryptoValues::getAesEncryptKey).thenReturn(TEST_KEY);

      // Probar encriptación
      String encrypted = Crypto.encryptStringAES(PLAIN_TEXT);
      assertNotEquals(PLAIN_TEXT, encrypted);
      assertFalse(encrypted.isEmpty());

      // Probar desencriptación
      String decrypted = Crypto.decryptStringAES(encrypted);
      assertEquals(PLAIN_TEXT, decrypted);
    }
  }

  @Test
  void testEncryptWithException() {
    // Configurar mock para CryptoValues con una clave inválida
    try (MockedStatic<CryptoValues> cryptoValuesMock = Mockito.mockStatic(CryptoValues.class)) {
      cryptoValuesMock.when(CryptoValues::getAesEncryptKey).thenReturn("invalid_key"); // Clave inválida

      // La encriptación debería fallar pero retornar string vacío sin lanzar excepción
      String encrypted = Crypto.encryptStringAES(PLAIN_TEXT);
      assertTrue(encrypted.isEmpty());
    }
  }

  @Test
  void testDecryptWithException() {
    // Configurar entrada inválida para desencriptación
    String invalidEncrypted = Base64.getEncoder().encodeToString("invalid_data".getBytes(StandardCharsets.UTF_8));

    // La desencriptación debería fallar pero retornar string vacío sin lanzar excepción
    String decrypted = Crypto.decryptStringAES(invalidEncrypted);
    assertTrue(decrypted.isEmpty());
  }

  @Test
  void testDecryptFileSTAE(@TempDir File tempDir) throws Exception {
    // Configurar archivo de prueba con datos encriptados simulados
    File inputFile = new File(tempDir, "encrypted.dat");
    File outputFile = new File(tempDir, "decrypted.txt");

    try (MockedStatic<CryptoValues> cryptoValuesMock = Mockito.mockStatic(CryptoValues.class)) {
      cryptoValuesMock.when(CryptoValues::getAesEncryptKey).thenReturn(TEST_KEY);

      // Crear datos de prueba - primero encriptar un texto
      String encrypted = Crypto.encryptStringAES(PLAIN_TEXT);
      byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

      // Escribir los bytes encriptados al archivo de entrada
      Files.write(inputFile.toPath(), encryptedBytes);

      // Ejecutar el método a probar
      Crypto.decryptFileSTAE(inputFile, outputFile, TEST_KEY);

      // Verificar que el archivo de salida existe y contiene el texto original
      assertTrue(outputFile.exists());
      String decryptedContent = new String(Files.readAllBytes(outputFile.toPath()), StandardCharsets.UTF_8);
      assertEquals(PLAIN_TEXT, decryptedContent);
    }
  }

  @Test
  void testDecryptFileSTAEWithException(@TempDir File tempDir) throws Exception {
    // Configurar archivo de entrada y salida
    File inputFile = new File(tempDir, "invalid.dat");
    File outputFile = new File(tempDir, "output.txt");

    // Crear archivo de entrada con datos inválidos
    Files.write(inputFile.toPath(), "invalid data".getBytes(StandardCharsets.UTF_8));

    // Ejecutar el método - debería manejar la excepción internamente
    Crypto.decryptFileSTAE(inputFile, outputFile, TEST_KEY);

    // Verificar que el archivo de salida no existe o está vacío
    if (outputFile.exists()) {
      byte[] content = Files.readAllBytes(outputFile.toPath());
      assertEquals(0, content.length);
    }
  }

}
