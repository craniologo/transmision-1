package pe.gob.onpe.wsonpe.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")


class ZipUtilsTest {

  @TempDir
  Path tempDir;

  private File encryptedFile;
  private String inputDir;
  private String encryptFileName;


  @BeforeEach
  void setUp() throws IOException {
    // Crear directorios y archivos para pruebas
    inputDir = tempDir.resolve("input").toString();
    Files.createDirectory(Paths.get(inputDir));

    encryptFileName = "encrypted.bin";
    encryptedFile = new File(Paths.get(inputDir, encryptFileName).toString());
    Files.write(encryptedFile.toPath(), "encrypted content".getBytes());
  }

  @Test
  void testPrivateConstructor() throws Exception {
    // Usar reflexión para acceder al constructor privado
    Constructor<ZipUtils> constructor = ZipUtils.class.getDeclaredConstructor();
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
      String expectedMessage = "Utility class should not be instantiated";
      assertTrue(cause.getMessage().contains(expectedMessage),
        "El mensaje de excepción debe contener: " + expectedMessage);
    }
  }

  @Test
  void testNameZip() {
    // Verificar formato de nombre del zip
    String zipName = ZipUtils.nameZip();

    // Verificar que tiene el formato correcto (fecha + número aleatorio)
    // El formato esperado es "yyyyMMdd.HHmmss" seguido de un número aleatorio
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
    String dateStr = dateFormat.format(new Date());

    assertTrue(zipName.startsWith(dateStr),
      "El nombre del zip debe empezar con la fecha en formato yyyyMMdd.HHmmss");

    // Verificar que después de la fecha hay un número (la parte aleatoria)
    String randomPart = zipName.substring(dateStr.length());
    assertTrue(Pattern.matches("0\\.[0-9]+", randomPart) ||
        Pattern.matches("[0-9]+\\.[0-9]+", randomPart) ||
        Pattern.matches("[0-9]+", randomPart),
      "Después de la fecha debe haber un número aleatorio");
  }

  @Test
  void testStaticFinalFields() {
    // Verificar que las constantes tienen valores válidos
    assertNotNull(ZipUtils.SEPARATOR);
    assertNotNull(ZipUtils.USER_DIR);
    assertNotNull(ZipUtils.PATH_ROOT_ZIP);

    // Verificar que PATH_ROOT_ZIP es la combinación de USER_DIR y SEPARATOR
    Assertions.assertThat(ZipUtils.USER_DIR + ZipUtils.SEPARATOR).isEqualTo(ZipUtils.PATH_ROOT_ZIP);
  }

}
