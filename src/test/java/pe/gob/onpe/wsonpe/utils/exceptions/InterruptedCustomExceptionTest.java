package pe.gob.onpe.wsonpe.utils.exceptions;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@Slf4j
class InterruptedCustomExceptionTest {

  @Test
  void testConstructorWithMessageAndCause() {
    // Arrange
    String errorMessage = "Error de interrupción personalizado";
    Throwable cause = new RuntimeException("Causa original");

    // Act
    InterruptedCustomException exception = new InterruptedCustomException(errorMessage, cause);

    // Assert
    assertEquals(errorMessage, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testConstructorWithCauseOnly() {
    // Arrange
    Throwable cause = new IllegalArgumentException("Causa original");
    String expectedMessage = "Error e interrupcción en el runtime";

    // Act
    InterruptedCustomException exception = new InterruptedCustomException(cause);

    // Assert
    assertEquals(expectedMessage, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
