package pe.gob.onpe.wsonpe.actaservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import pe.gob.onpe.wsonpe.dto.ActaRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")


@Slf4j
class ActaRequestTest {

  @Test
  void testSetTrama2WhenTramaIsNull() {
    // Arrange
    ActaRequest actaRequest = new ActaRequest();
    actaRequest.setTrama("");

    // Act
    actaRequest.setTrama2("alguna");

    // Assert
    assertEquals("alguna", actaRequest.getTrama2());
    assertEquals("", actaRequest.getTrama());
  }

  @Test
  void testSetTrama2WhenTramaIsLessThanMax() {
    // Arrange
    ActaRequest actaRequest = new ActaRequest();
    String tramaPequena = "Esta es una trama corta";
    actaRequest.setTrama(tramaPequena);

    // Act
    actaRequest.setTrama2("trama secundaria");

    // Assert
    assertEquals("trama secundaria", actaRequest.getTrama2());
    assertEquals(tramaPequena, actaRequest.getTrama());
  }

  @Test
  void testSetTrama2WhenTramaIsGreaterThanMax() {
    // Arrange
    ActaRequest actaRequest = new ActaRequest();

    // Crear una trama que exceda el máximo (30000 caracteres)
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 31000; i++) {
      sb.append("a");
    }
    String tramaLarga = sb.toString();
    actaRequest.setTrama(tramaLarga);

    // Act
    actaRequest.setTrama2("esto será reemplazado");

    // Assert
    // Verificar que trama2 contiene los caracteres excedentes
    String expectedTrama2 = tramaLarga.substring(30000);
    assertEquals(expectedTrama2, actaRequest.getTrama2());

    // Verificar que trama ahora solo contiene los primeros 30000 caracteres
    String expectedTrama = tramaLarga.substring(0, 30000);
    assertEquals(expectedTrama, actaRequest.getTrama());
    assertEquals(30000, actaRequest.getTrama().length());
    assertEquals(1000, actaRequest.getTrama2().length());
  }

  @Test
  void testSetTrama2WhenTramaIsExactlyMax() {
    // Arrange
    ActaRequest actaRequest = new ActaRequest();

    // Crear una trama de exactamente 30000 caracteres
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 30000; i++) {
      sb.append("x");
    }
    String tramaExacta = sb.toString();
    actaRequest.setTrama(tramaExacta);

    // Act
    actaRequest.setTrama2("valor de trama2");

    // Assert
    assertEquals("valor de trama2", actaRequest.getTrama2());
    assertEquals(tramaExacta, actaRequest.getTrama());
    assertEquals(30000, actaRequest.getTrama().length());
  }
}
