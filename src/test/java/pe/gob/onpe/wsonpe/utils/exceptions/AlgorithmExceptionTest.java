package pe.gob.onpe.wsonpe.utils.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@Slf4j
class AlgorithmExceptionTest {

  @Test
  void testConstructorWithMessageAndCause() {
    // Arrange
    String errorMessage = "Error de prueba";
    Throwable cause = new RuntimeException("Causa original");

    // Act
    AlgorithmException exception = new AlgorithmException(errorMessage, cause);

    // Assert
    assertEquals(errorMessage, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testConstructorWithCauseOnly() {
    // Arrange
    Throwable cause = new IllegalArgumentException("Causa original");
    String expectedMessage = "Error al procesar el algoritmo criptográfico";

    // Act
    AlgorithmException exception = new AlgorithmException(cause);

    // Assert
    assertEquals(expectedMessage, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
