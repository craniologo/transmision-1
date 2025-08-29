package pe.gob.onpe.wsonpe.utils;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.gob.onpe.wsonpe.utils.exceptions.AlgorithmException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class WsOnpeUtilsTest {

  @TempDir
  Path tempDir;

  private Path tempFile;
  private File mockFile;

  @BeforeEach
  void setUp() throws IOException {
    mockFile = new File(tempDir.toFile(), "test.7z");

    // Crear un archivo temporal para pruebas
    tempFile = tempDir.resolve("testFile.txt");
    Files.write(tempFile, "Test content for file hash".getBytes(StandardCharsets.UTF_8));

    // Preparar un directorio para pruebas de mesa
    Files.createDirectory(tempDir.resolve("mesa"));

    // Configurar mock para SecurityContextHolder
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn("encryptedUser");
    SecurityContextHolder.setContext(securityContext);

    // Crear un directorio para logs
    new File(".//logs//").mkdirs();
  }

  @Test
  void testPrivateConstructor() throws Exception {
    // Prueba del constructor privado
    java.lang.reflect.Constructor<WsOnpeUtils> constructor = WsOnpeUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    WsOnpeUtils instance = constructor.newInstance();
    assertNotNull(instance);
  }

  @Test
  void testGetSHA() {
    // Prueba de getSHA con entrada válida
    String input = "test string";
    String result = WsOnpeUtils.getSHA(input);
    assertNotNull(result);
    assertEquals(128, result.length()); // SHA-512 debe producir un hash de 128 caracteres en hex
  }

  @Test
  void testGetSHAWithAlgorithmException() {
    // Prueba getSHA con una excepción simulada
    try (MockedStatic<MessageDigest> mockedStatic = mockStatic(MessageDigest.class)) {
      mockedStatic.when(() -> MessageDigest.getInstance(anyString()))
        .thenThrow(new java.security.NoSuchAlgorithmException("Test Exception"));

      assertThrows(AlgorithmException.class, () -> WsOnpeUtils.getSHA("test"));
    }
  }

  @Test
  void testGetMd5() {
    // Prueba de getMd5 con entrada válida
    String input = "test string";
    String result = WsOnpeUtils.getMd5(input);
    assertNotNull(result);
    assertEquals(32, result.length()); // MD5 debe producir un hash de 32 caracteres en hex
  }

  @Test
  void testGetMd5WithAlgorithmException() {
    // Prueba getMd5 con una excepción simulada
    try (MockedStatic<MessageDigest> mockedStatic = mockStatic(MessageDigest.class)) {
      mockedStatic.when(() -> MessageDigest.getInstance(anyString()))
        .thenThrow(new java.security.NoSuchAlgorithmException("Test Exception"));

      assertThrows(AlgorithmException.class, () -> WsOnpeUtils.getMd5("test"));
    }
  }

  @Test
  void testWriteLog() throws IOException {
    // Configurar el directorio de logs
    String user = "testUser";
    String action = "testAction";
    String message = "testMessage";

    WsOnpeUtils.writeLog(user, action, message);

    // Verificar que se ha creado el archivo de log
    String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    File logFile = new File(".//logs//" + user + "_" + today + ".jel");

    // Como writeLog maneja excepciones internamente y no devuelve ningún valor,
    // solo podemos verificar si el archivo existe
    assertTrue(logFile.exists());

    // Leer el contenido del archivo para verificar el formato
    try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
      String line = reader.readLine();
      assertNotNull(line);
      assertTrue(line.contains(action));
      assertTrue(line.contains(message));
    }

    // Limpiar después de la prueba
    logFile.delete();
  }

  @Test
  void testGetCurrentTimestampInteger() {
    // Verificar que getCurrentTimestampInteger devuelve un valor entero positivo
    int timestamp = WsOnpeUtils.getCurrentTimestampInteger();
    assertTrue(timestamp > 0);
  }

  @Test
  void testGetFileHash() throws IOException {
    // Probar getFileHash con un archivo existente
    String filePath = tempFile.toString();
    String hash = WsOnpeUtils.getFileHash(filePath);

    assertNotNull(hash);
    assertEquals(64, hash.length()); // SHA-256 produce un hash de 64 caracteres
  }

  @Test
  void testGetFileHashWithNonExistentFile() {
    // Probar getFileHash con un archivo que no existe
    String nonExistentPath = tempDir.resolve("nonexistent.txt").toString();

    assertThrows(IOException.class, () -> WsOnpeUtils.getFileHash(nonExistentPath));
  }

  @Test
  void testGetRequestUser() {
    // Configurar mock para Crypto.decryptStringAES
    try (MockedStatic<Crypto> mockedCrypto = mockStatic(Crypto.class)) {
      mockedCrypto.when(() -> Crypto.decryptStringAES(anyString()))
        .thenReturn("decryptedUser");

      String user = WsOnpeUtils.getRequestUser();
      assertEquals("decryptedUser", user);
    }
  }

  @Test
  void testMesaDirectoryExistsOrCreate() {
    // Probar cuando el directorio ya existe
    String existingPath = tempDir.resolve("mesa").toString();
    assertTrue(WsOnpeUtils.mesaDirectoryExistsOrCreate(existingPath));

    // Probar cuando el directorio no existe pero se puede crear
    String newPath = tempDir.resolve("newMesa").toString();
    assertTrue(WsOnpeUtils.mesaDirectoryExistsOrCreate(newPath));
    assertTrue(Files.exists(Paths.get(newPath)));

    // Probar cuando hay error al crear el directorio
    String invalidPath = "/:*?\"<>|"; // Caracteres inválidos para nombre de archivo
    assertFalse(WsOnpeUtils.mesaDirectoryExistsOrCreate(invalidPath));
  }

  @Test
  void testGetJsonToObject() {
    // Probar la conversión de JSON a objeto
    String json = "{\"name\":\"Test\",\"value\":123}";
    TestClass result = WsOnpeUtils.getJsonToObject(json, TestClass.class);

    assertNotNull(result);
    assertEquals("Test", result.getName());
    assertEquals(123, result.getValue());
  }

  /**
   * Prueba completa para isValidSevenFileZip con un archivo válido
   */
  @Test
  void testIsValidSevenFileZipWithValidFile() throws IOException {
    // Usar MockedConstruction para controlar la creación del SevenZFile
    try (MockedConstruction<SevenZFile> mocked = Mockito.mockConstruction(
      SevenZFile.class,
      (mock, context) -> {
        // Configurar el mock para que validateSevenZipEntries retorne true
        // No configuramos ninguna entrada, simulando un archivo vacío pero válido
      })) {

      // Esta llamada debería retornar true para un archivo válido
      boolean result = WsOnpeUtils.isValidSevenFileZip(mockFile);

      // Verificar que se retorna true
      assertTrue(result);

      // Verificar que se construyó el SevenZFile
      assertEquals(1, mocked.constructed().size());

      // Verificar que se llamó al método getNextEntry en el SevenZFile construido
      verify(mocked.constructed().get(0)).getNextEntry();
    }

  }

  @Test
  void testIsValidSevenFileZipWithInvalidFile() throws IOException {
    // Crear un archivo de prueba que no sea un archivo 7z válido
    Path invalidFile = tempDir.resolve("invalid.7z");
    Files.write(invalidFile, "This is not a valid 7z file".getBytes(StandardCharsets.UTF_8));

    // Llamar al método con el archivo inválido
    boolean result = WsOnpeUtils.isValidSevenFileZip(invalidFile.toFile());

    // Verificar que se retorna false
    assertFalse(result);
  }

  @Test
  void testValidateSevenZipEntries() throws Exception {
    // Obtener acceso al método privado usando reflexión
    java.lang.reflect.Method validateMethod = WsOnpeUtils.class.getDeclaredMethod("validateSevenZipEntries", SevenZFile.class);
    validateMethod.setAccessible(true);

    // Caso 1: Prueba el caso exitoso - proceso normal sin entradas
    SevenZFile mockEmptySevenZFile = Mockito.mock(SevenZFile.class);
    // getNextEntry() devuelve null inmediatamente (archivo vacío)
    Mockito.when(mockEmptySevenZFile.getNextEntry()).thenReturn(null);

    boolean emptyResult = (boolean) validateMethod.invoke(null, mockEmptySevenZFile);
    assertTrue(emptyResult, "Debería retornar true para un archivo 7z vacío");
    Mockito.verify(mockEmptySevenZFile).close();

    // Caso 2: Prueba con una entrada exitosa
    SevenZFile mockOneEntrySevenZFile = Mockito.mock(SevenZFile.class);
    SevenZArchiveEntry mockEntry = Mockito.mock(SevenZArchiveEntry.class);
    // Primera llamada retorna una entrada, segunda llamada retorna null
    Mockito.when(mockOneEntrySevenZFile.getNextEntry()).thenReturn(mockEntry, (SevenZArchiveEntry)null);
    // Configurar el tamaño de la entrada
    Mockito.when(mockEntry.getSize()).thenReturn(10L);

    boolean oneEntryResult = (boolean) validateMethod.invoke(null, mockOneEntrySevenZFile);
    assertTrue(oneEntryResult, "Debería retornar true para un archivo 7z con una entrada válida");
    // Verificar que read fue llamado con los parámetros correctos
    Mockito.verify(mockOneEntrySevenZFile).read(Mockito.any(byte[].class), Mockito.eq(0), Mockito.eq(10));
    Mockito.verify(mockOneEntrySevenZFile).close();

    // Caso 3: Prueba con excepción en getNextEntry()
    SevenZFile mockExceptionOnGetNextEntry = Mockito.mock(SevenZFile.class);
    Mockito.when(mockExceptionOnGetNextEntry.getNextEntry()).thenThrow(new IOException("Error al obtener la siguiente entrada"));

    boolean exceptionOnGetNextEntryResult = (boolean) validateMethod.invoke(null, mockExceptionOnGetNextEntry);
    assertFalse(exceptionOnGetNextEntryResult, "Debería retornar false cuando hay una excepción en getNextEntry()");

    // Caso 4: Prueba con excepción en read()
    SevenZFile mockExceptionOnRead = Mockito.mock(SevenZFile.class);
    SevenZArchiveEntry mockReadExceptionEntry = Mockito.mock(SevenZArchiveEntry.class);
    Mockito.when(mockExceptionOnRead.getNextEntry()).thenReturn(mockReadExceptionEntry);
    Mockito.when(mockReadExceptionEntry.getSize()).thenReturn(10L);
    Mockito.doThrow(new IOException("Error al leer la entrada")).when(mockExceptionOnRead).read(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());

    boolean exceptionOnReadResult = (boolean) validateMethod.invoke(null, mockExceptionOnRead);
    assertFalse(exceptionOnReadResult, "Debería retornar false cuando hay una excepción en read()");

    // Caso 5: Prueba con excepción en close()
    SevenZFile mockExceptionOnClose = Mockito.mock(SevenZFile.class);
    Mockito.when(mockExceptionOnClose.getNextEntry()).thenReturn(null);
    Mockito.doThrow(new IOException("Error al cerrar el archivo")).when(mockExceptionOnClose).close();

    boolean exceptionOnCloseResult = (boolean) validateMethod.invoke(null, mockExceptionOnClose);
    assertFalse(exceptionOnCloseResult, "Debería retornar false cuando hay una excepción en close()");
  }

  @Test
  void testSaveFilesFromCompressed_IOException() {
    // Crear mocks para los objetos necesarios
    File mockFile1 = Mockito.mock(File.class);

    // Usar MockedConstruction para interceptar la creación de SevenZFile
    // y hacer que lance una IOException
    try (MockedConstruction<SevenZFile> mockedSevenZFile = Mockito.mockConstruction(
      SevenZFile.class,
      (mock, context) -> {
        // Hacer que getNextEntry lance una IOException
        when(mock.getNextEntry()).thenThrow(new IOException("Test exception"));
      })) {

      // Ejecutar el método que estamos probando
      boolean result = WsOnpeUtils.saveFilesFromCompressed(mockFile1, "output/path");

      // Verificar que el resultado es falso (error)
      assertFalse(result);

      // Verificar que se creó un SevenZFile
      assertEquals(1, mockedSevenZFile.constructed().size());
    }
  }

  @Test
  void testSaveEntryToFile_Success() throws Exception {
    // Este es un método privado, necesitaremos reflexión para probarlo
    Method saveEntryToFileMethod = WsOnpeUtils.class.getDeclaredMethod(
      "saveEntryToFile",
      SevenZFile.class,
      SevenZArchiveEntry.class,
      File.class,
      String.class
    );
    saveEntryToFileMethod.setAccessible(true);

    // Crear mocks para los objetos necesarios
    SevenZFile mockSevenZFile = Mockito.mock(SevenZFile.class);
    SevenZArchiveEntry mockEntry = Mockito.mock(SevenZArchiveEntry.class);
    File mockDestFile = Mockito.mock(File.class);
    String path = "/test/path";

    // Configurar el comportamiento de los mocks
    when(mockEntry.getName()).thenReturn("testfile.txt");
    when(mockEntry.getSize()).thenReturn(10L);

    // Usar MockedConstruction para interceptar la creación de FileOutputStream
    try (MockedConstruction<FileOutputStream> mockedOutputStream = Mockito.mockConstruction(
      FileOutputStream.class,
      (mock, context) -> {
        // No es necesario configurar nada aquí
      })) {

      // Invocar el método privado
      saveEntryToFileMethod.invoke(null, mockSevenZFile, mockEntry, mockDestFile, path);

      // Verificar que se creó un FileOutputStream
      assertEquals(1, mockedOutputStream.constructed().size());

      // Verificar que se llamó al método read en el SevenZFile
      verify(mockSevenZFile).read(any(byte[].class), eq(0), eq(10));
    }
  }

  @Test
  void testSaveEntryToFile_IOException() throws Exception {
    // Este es un método privado, necesitaremos reflexión para probarlo
    Method saveEntryToFileMethod = WsOnpeUtils.class.getDeclaredMethod(
      "saveEntryToFile",
      SevenZFile.class,
      SevenZArchiveEntry.class,
      File.class,
      String.class
    );
    saveEntryToFileMethod.setAccessible(true);

    // Crear mocks para los objetos necesarios
    SevenZFile mockSevenZFile = Mockito.mock(SevenZFile.class);
    SevenZArchiveEntry mockEntry = Mockito.mock(SevenZArchiveEntry.class);
    File mockDestFile = Mockito.mock(File.class);
    String path = "/test/path";

    // Configurar el comportamiento de los mocks
    when(mockEntry.getName()).thenReturn("testfile.txt");
    when(mockEntry.getSize()).thenReturn(10L);
    doThrow(new IOException("Test exception")).when(mockSevenZFile).read(any(byte[].class), anyInt(), anyInt());

    // Usar MockedConstruction para interceptar la creación de FileOutputStream
    try (MockedConstruction<FileOutputStream> mockedOutputStream = Mockito.mockConstruction(
      FileOutputStream.class,
      (mock, context) -> {
        // No es necesario configurar nada aquí
      })) {

      // Invocar el método privado - debería manejar la excepción internamente
      saveEntryToFileMethod.invoke(null, mockSevenZFile, mockEntry, mockDestFile, path);

      // Verificar que se intentó llamar al método read en el SevenZFile
      verify(mockSevenZFile).read(any(byte[].class), eq(0), eq(10));
    }
  }

  // Clase auxiliar para pruebas de JSON
  static class TestClass {
    private String name;
    private int value;

    public String getName() {
      return name;
    }

    public int getValue() {
      return value;
    }
  }

}
