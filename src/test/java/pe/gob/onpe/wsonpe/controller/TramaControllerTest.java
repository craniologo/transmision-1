package pe.gob.onpe.wsonpe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.ITramaService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@ExtendWith(MockitoExtension.class)
class TramaControllerTest {

  @Mock
  private ITramaService tramaService;

  @InjectMocks
  private TramaController tramaController;

  private static final String TRAMA_TEST = "tramaEjemplo123";
  private static final String USER_TEST = "usuarioPrueba";

  @BeforeEach
  void setup() {
    // Inyectar valor para masterKey ya que viene de @Value
    ReflectionTestUtils.setField(tramaController, "masterKey", "claveSecretaPrueba");
  }

  @Test
  void testReceiveTrama() {
    // Arrange
    MensajeWsResponse expectedResponse = new MensajeWsResponse();
    expectedResponse.setCodigo(0); // Usando Integer según la clase actualizada
    expectedResponse.setSuccess(true);
    expectedResponse.setMessage("Trama procesada correctamente");

    // Mockear el método estático WsOnpeUtils.getRequestUser()
    try (MockedStatic<WsOnpeUtils> mockedStatic = Mockito.mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(WsOnpeUtils::getRequestUser).thenReturn(USER_TEST);

      // Configurar el mock del servicio
      when(tramaService.receiveTrama(USER_TEST, TRAMA_TEST)).thenReturn(expectedResponse);

      // Act
      MensajeWsResponse result = tramaController.receiveTrama(TRAMA_TEST);

      // Assert
      assertNotNull(result);
      assertEquals(expectedResponse.getCodigo(), result.getCodigo());
      assertEquals(expectedResponse.getMessage(), result.getMessage());
      assertTrue(result.getSuccess());

      // Verificar que se llamó al método del servicio con los parámetros correctos
      verify(tramaService).receiveTrama(USER_TEST, TRAMA_TEST);
    }
  }

  @Test
  void testReceiveTramaErrorResponse() {
    // Arrange
    MensajeWsResponse errorResponse = new MensajeWsResponse();
    errorResponse.setCodigo(99);
    errorResponse.setSuccess(false);
    errorResponse.setMessage("Error al procesar la trama");

    try (MockedStatic<WsOnpeUtils> mockedStatic = Mockito.mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(WsOnpeUtils::getRequestUser).thenReturn(USER_TEST);

      // Configurar el mock del servicio para devolver un error
      when(tramaService.receiveTrama(USER_TEST, TRAMA_TEST)).thenReturn(errorResponse);

      // Act
      MensajeWsResponse result = tramaController.receiveTrama(TRAMA_TEST);

      // Assert
      assertNotNull(result);
      assertEquals(Integer.valueOf(99), result.getCodigo());
      assertEquals("Error al procesar la trama", result.getMessage());
      assertEquals(false, result.getSuccess());
    }
  }

  @Test
  void testGetMethod() {
    // Act
    RedirectView result = tramaController.getMethod();

    // Assert
    assertNotNull(result);
    assertEquals("/", result.getUrl());
  }

  @Test
  void testControllerConstructor() {
    // Arrange & Act
    TramaController controller = new TramaController(tramaService);

    // Assert
    assertNotNull(controller);
  }

  @Test
  void testControllerInitialization() {
    // Arrange
    ITramaService mockService = Mockito.mock(ITramaService.class);

    // Act
    TramaController controller = new TramaController(mockService);

    // Assert
    assertNotNull(controller);
    // Verificar que el servicio se ha inyectado correctamente usando Reflection
    Object injectedService = ReflectionTestUtils.getField(controller, "tramaService");
    assertEquals(mockService, injectedService);
  }
}
