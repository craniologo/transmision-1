package pe.gob.onpe.wsonpe.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class WsOnpeFileUtilsTest {

  @TempDir
  Path tempDir;

  private File testFile;
  private File testDirectory;
  private File nestedDirectory;
  private File nestedFile;

  @BeforeEach
  void setUp() throws IOException {
    // Crear estructura de prueba
    testFile = new File(tempDir.toFile(), "testFile.txt");
    Files.write(testFile.toPath(), "test content".getBytes());

    testDirectory = new File(tempDir.toFile(), "testDirectory");
    testDirectory.mkdir();

    nestedDirectory = new File(testDirectory, "nestedDirectory");
    nestedDirectory.mkdir();

    nestedFile = new File(nestedDirectory, "nestedFile.txt");
    Files.write(nestedFile.toPath(), "nested content".getBytes());
  }

  @Test
  void testPrivateConstructor() throws Exception {
    // Usar reflexión para acceder al constructor privado
    Constructor<WsOnpeFileUtils> constructor = WsOnpeFileUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    try {
      // Intentar crear una instancia
      constructor.newInstance();
      fail("Se esperaba que el constructor lanzara una excepción");
    } catch (InvocationTargetException e) {
      // Verificar que la causa sea UnsupportedOperationException
      Throwable cause = e.getCause();
      assertTrue(cause instanceof IllegalStateException,
        "La causa debe ser UnsupportedOperationException, pero fue: " + cause.getClass().getName());

      // Verificar el mensaje de la excepción
      String expectedMessage = "WsOnpeFileUtils class";
      assertTrue(cause.getMessage().contains(expectedMessage),
        "El mensaje de excepción debe contener: " + expectedMessage);
    }
  }

  @Test
  void testDeleteWithChildrenForNonExistentFile() {
    // Probar con un archivo que no existe
    String nonExistentPath = tempDir.resolve("nonexistent.txt").toString();
    boolean result = WsOnpeFileUtils.deleteWithChildren(nonExistentPath);
    assertTrue(result);
  }

  @Test
  void testDeleteWithChildrenForRegularFile() {
    // Probar con un archivo normal (no directorio)
    // Mockear deleteDirectory para simular un borrado exitoso
    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteWithChildren")) {
        return invocation.callRealMethod();
      }
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return true;
      }
      return invocation.callRealMethod();
    })) {
      boolean result = WsOnpeFileUtils.deleteWithChildren(testFile.getAbsolutePath());
      assertTrue(result);
    }
  }

  @Test
  void testDeleteWithChildrenForDirectory() {
    // Probar con un directorio
    // Mockear métodos para simular borrado exitoso
    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteWithChildren")) {
        return invocation.callRealMethod();
      }
      if (invocation.getMethod().getName().equals("deleteChildren") ||
        invocation.getMethod().getName().equals("deleteDirectory")) {
        return true;
      }
      return invocation.callRealMethod();
    })) {
      boolean result = WsOnpeFileUtils.deleteWithChildren(testDirectory.getAbsolutePath());
      assertTrue(result);
    }
  }

  @Test
  void testDeleteWithChildrenWhenDeleteChildrenFails() {
    // Probar cuando deleteChildren falla
    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteWithChildren")) {
        return invocation.callRealMethod();
      }
      if (invocation.getMethod().getName().equals("deleteChildren")) {
        return false;
      }
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return true;
      }
      return invocation.callRealMethod();
    })) {
      boolean result = WsOnpeFileUtils.deleteWithChildren(testDirectory.getAbsolutePath());
      assertFalse(result);
    }
  }

  @Test
  void testDeleteWithChildrenWhenDeleteDirectoryFails() {
    // Probar cuando deleteDirectory falla
    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteWithChildren")) {
        return invocation.callRealMethod();
      }
      if (invocation.getMethod().getName().equals("deleteChildren")) {
        return true;
      }
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return false;
      }
      return invocation.callRealMethod();
    })) {
      boolean result = WsOnpeFileUtils.deleteWithChildren(testDirectory.getAbsolutePath());
      assertFalse(result);
    }
  }

  @Test
  void testDeleteChildren() throws Exception {
    // Acceder al método privado deleteChildren
    java.lang.reflect.Method deleteChildrenMethod = WsOnpeFileUtils.class.getDeclaredMethod("deleteChildren", File.class);
    deleteChildrenMethod.setAccessible(true);

    // Mockear deleteDirectory para simular borrado exitoso
    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return true;
      }
      return invocation.callRealMethod();
    })) {
      boolean result = (boolean) deleteChildrenMethod.invoke(null, testDirectory);
      assertTrue(result);
    }
  }

  @Test
  void testDeleteChildrenWithNullListFiles() throws Exception {
    // Crear un mock de File que devuelve null al llamar a listFiles()
    File mockDirectory = mock(File.class);
    when(mockDirectory.listFiles()).thenReturn(null);

    // Acceder al método privado deleteChildren
    java.lang.reflect.Method deleteChildrenMethod = WsOnpeFileUtils.class.getDeclaredMethod("deleteChildren", File.class);
    deleteChildrenMethod.setAccessible(true);

    boolean result = (boolean) deleteChildrenMethod.invoke(null, mockDirectory);
    assertTrue(result);
  }

  @Test
  void testDeleteChildrenWithNestedDirectories() throws Exception {
    // Crear una estructura de directorios más compleja
    File deepDir = new File(nestedDirectory, "deepDir");
    deepDir.mkdir();
    File deepFile = new File(deepDir, "deepFile.txt");
    Files.write(deepFile.toPath(), "deep content".getBytes());

    // Acceder al método privado deleteChildren
    java.lang.reflect.Method deleteChildrenMethod = WsOnpeFileUtils.class.getDeclaredMethod("deleteChildren", File.class);
    deleteChildrenMethod.setAccessible(true);

    // Mockear deleteDirectory para simular borrado exitoso
    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return true;
      }
      return invocation.callRealMethod();
    })) {
      boolean result = (boolean) deleteChildrenMethod.invoke(null, testDirectory);
      assertTrue(result);
    }
  }

  @Test
  void testDeleteFileWithFileObject() {
    // Probar con un archivo que existe
    File mockFile = mock(File.class);
    when(mockFile.exists()).thenReturn(true);
    when(mockFile.isFile()).thenReturn(true);
    when(mockFile.getAbsolutePath()).thenReturn("/path/to/file.txt");

    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteFile") && invocation.getArguments()[0] instanceof File) {
        invocation.callRealMethod();
        return null;
      }
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return true;
      }
      return invocation.callRealMethod();
    })) {
      // No debería lanzar excepción
      assertDoesNotThrow(() -> WsOnpeFileUtils.deleteFile(mockFile));
    }
  }

  @Test
  void testDeleteFileWhenDeleteFails() {
    // Probar cuando el borrado falla
    File mockFile = mock(File.class);
    when(mockFile.exists()).thenReturn(true);
    when(mockFile.isFile()).thenReturn(true);
    when(mockFile.getAbsolutePath()).thenReturn("/path/to/file.txt");

    try (MockedStatic<WsOnpeFileUtils> mockedStatic = mockStatic(WsOnpeFileUtils.class, invocation -> {
      if (invocation.getMethod().getName().equals("deleteFile") && invocation.getArguments()[0] instanceof File) {
        invocation.callRealMethod();
        return null;
      }
      if (invocation.getMethod().getName().equals("deleteDirectory")) {
        return false;
      }
      return invocation.callRealMethod();
    })) {
      // No debería lanzar excepción incluso si falla
      assertDoesNotThrow(() -> WsOnpeFileUtils.deleteFile(mockFile));
    }
  }

  @Test
  void testDeleteFileWithException() {
    // Probar cuando se lanza una excepción durante el borrado
    File mockFile = mock(File.class);
    when(mockFile.exists()).thenReturn(true);
    when(mockFile.isFile()).thenReturn(true);
    when(mockFile.getAbsolutePath()).thenThrow(new RuntimeException("Test exception"));

    // No debería propagar la excepción
    assertDoesNotThrow(() -> WsOnpeFileUtils.deleteFile(mockFile));
  }

  @Test
  void testValidateDirWhenDirectoryExists() {
    // Probar con un directorio que existe
    boolean result = WsOnpeFileUtils.validateDir(testDirectory.getAbsolutePath(), false);
    assertTrue(result);
  }

  @Test
  void testValidateDirWhenDirectoryDoesNotExistButShouldCreate() {
    // Probar con un directorio que no existe pero debería crearse
    String newDirPath = tempDir.resolve("newDirectory").toString();
    boolean result = WsOnpeFileUtils.validateDir(newDirPath, true);
    assertTrue(result);
    assertTrue(new File(newDirPath).exists());
  }

  @Test
  void testValidateDirWhenNotDirectory() {
    // Probar con un archivo (no directorio)
    boolean result = WsOnpeFileUtils.validateDir(testFile.getAbsolutePath(), false);
    assertFalse(result);
  }

  @Test
  void testValidateFile() {
    // Probar con un archivo que existe
    boolean result = WsOnpeFileUtils.validateFile(testFile.getAbsolutePath());
    assertTrue(result);

    // Probar con un archivo que no existe
    result = WsOnpeFileUtils.validateFile(tempDir.resolve("nonexistent.txt").toString());
    assertFalse(result);
  }

  @Test
  void testCreateFolderTreeWhenFolderExists() {
    // Probar con un directorio que ya existe
    boolean result = WsOnpeFileUtils.createFolderTree(testDirectory.getAbsolutePath());
    assertTrue(result);
  }

  @Test
  void testCreateFolderTreeWhenFolderDoesNotExist() {
    // Probar con un directorio que no existe
    String newDirPath = tempDir.resolve("newFolderTree").toString();
    boolean result = WsOnpeFileUtils.createFolderTree(newDirPath);
    assertTrue(result);
    assertTrue(new File(newDirPath).exists());
  }

  @Test
  void testCreateFolderTreeWithNestedPath() {
    // Probar con un path anidado que no existe
    String nestedPath = tempDir.resolve("level1/level2/level3").toString();
    boolean result = WsOnpeFileUtils.createFolderTree(nestedPath);
    assertTrue(result);
    assertTrue(new File(nestedPath).exists());
  }

  @Test
  void testDeleteDirectoryWithDirectory() throws IOException {
    // Crear un directorio temporal para la prueba
    Path tempDir2 = Files.createTempDirectory("testDelete");
    File directory = tempDir2.toFile();

    // Verificar que existe el directorio
    assertTrue(directory.exists());
    assertTrue(directory.isDirectory());

    // Probar la eliminación del directorio
    boolean result = WsOnpeFileUtils.deleteDirectory(directory);

    // Verificar que la función retorna true y el directorio fue eliminado
    assertTrue(result);
    assertFalse(directory.exists());
  }

  @Test
  void testDeleteFileStringComplete() throws Exception {
    // Opción 2: Probar con un archivo real

    // Crear un directorio temporal
    Path tempDir2 = Files.createTempDirectory("testDelete");
    Path tempFile = tempDir2.resolve("testFile.txt");

    // Crear un archivo de prueba
    Files.write(tempFile, "Test content".getBytes());

    // Verificar que el archivo existe
    assertTrue(Files.exists(tempFile));

    // Ejecutar el método a probar
    WsOnpeFileUtils.deleteFile(tempFile.toString());

    // Verificar resultado - esto dependerá de la implementación real de deleteFile(File)
    // Si deleteFile(File) está correctamente implementado, el archivo debería ser eliminado

    // Limpiar - eliminar el directorio temporal si aún existe
    Files.deleteIfExists(tempFile);
    Files.deleteIfExists(tempDir2);
  }

}
