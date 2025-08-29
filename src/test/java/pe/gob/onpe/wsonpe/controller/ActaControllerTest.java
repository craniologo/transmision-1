package pe.gob.onpe.wsonpe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.ActaRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.IActaService;
import pe.gob.onpe.wsonpe.service.IAsyncSynchroService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class ActaControllerTest {

  @Mock
  private IActaService actaService;

  @Mock
  private IAsyncSynchroService synchroService;

  @InjectMocks
  private ActaController actaController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Configurar valores para las propiedades inyectadas con @Value
    ReflectionTestUtils.setField(actaController, "synchroFlag", 1);
    ReflectionTestUtils.setField(actaController, "masterKey", "clave123");

    // Mock estático para WsOnpeUtils.getRequestUser()
    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");
    }
  }

  @Test
  void testReceiveActaWithValidKeyAndSuccessfulResponse() {
    // Arrange
    String key = "clave123";
    String mesa = "M001";
    String trama = "datosEnTrama";
    String firma = "firmaDigital";
    String meta = "metadatos";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    String pdfDigest = "digestValue";

    MensajeWsResponse successResponse = new MensajeWsResponse(0, true, "Procesado correctamente");
    when(actaService.receiveActa(any(ActaRequest.class))).thenReturn(successResponse);

    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");

      MensajeWsResponse response = actaController.receiveActa(
        key, mesa, trama, firma, meta, tipoSolucion,
        tipoModulo, tipoTrama, npaginaNro, pdfDigest);

      // Assert
      assertNotNull(response);
      assertEquals(0, response.getCodigo());
      assertTrue(response.getSuccess());
    }

    // Verify
    verify(actaService, times(1)).receiveActa(any(ActaRequest.class));
    verify(synchroService, times(1)).doSynchro(mesa, tipoSolucion, tipoModulo, tipoTrama);
  }

  @Test
  void testReceiveActaWithEncryptedFileNoSynchro() {
    // Arrange
    String key = "clave123";
    String mesa = "M001";
    String trama = "datosEnTrama";
    String firma = "firmaDigital";
    String meta = "metadatos";
    Integer tipoSolucion = 1;
    Integer tipoModulo = WebService.ENCRYPTED_FILE; // Módulo encriptado
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    String pdfDigest = "digestValue";

    MensajeWsResponse successResponse = new MensajeWsResponse(0, true, "Procesado correctamente");
    when(actaService.receiveActa(any(ActaRequest.class))).thenReturn(successResponse);

    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");

      // Act
      MensajeWsResponse response = actaController.receiveActa(
        key, mesa, trama, firma, meta, tipoSolucion,
        tipoModulo, tipoTrama, npaginaNro, pdfDigest);

      // Assert
      assertNotNull(response);
      assertEquals(0, response.getCodigo());
      assertTrue(response.getSuccess());
    }
    // Verify - No debe llamar a doSynchro para archivos encriptados
    verify(actaService, times(1)).receiveActa(any(ActaRequest.class));
    verify(synchroService, never()).doSynchro(anyString(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void testReceiveActaWithFailedResponseButSynchroProceed() {
    // Arrange
    String key = "clave123";
    String mesa = "M001";
    String trama = "datosEnTrama";
    String firma = "firmaDigital";
    String meta = "metadatos";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    String pdfDigest = "digestValue";

    // Respuesta fallida pero con código específico que requiere sincronización
    MensajeWsResponse failedResponse = new MensajeWsResponse(WebService.VALIDATION_FILE_FAILED, false, "Error en validación");
    when(actaService.receiveActa(any(ActaRequest.class))).thenReturn(failedResponse);

    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");

      // Act
      MensajeWsResponse response = actaController.receiveActa(
        key, mesa, trama, firma, meta, tipoSolucion,
        tipoModulo, tipoTrama, npaginaNro, pdfDigest);

      // Assert
      assertNotNull(response);
      assertEquals(WebService.VALIDATION_FILE_FAILED, response.getCodigo());
      assertFalse(response.getSuccess());
    }

    // Verify - Debe llamar a doSynchro aún con respuesta fallida por el código específico
    verify(actaService, times(1)).receiveActa(any(ActaRequest.class));
    verify(synchroService, times(1)).doSynchro(mesa, tipoSolucion, tipoModulo, tipoTrama);
  }

  @Test
  void testReceiveActaWithSynchroFlagDisabled() {
    // Arrange - Desactivar la bandera de sincronización
    ReflectionTestUtils.setField(actaController, "synchroFlag", 0);

    String key = "clave123";
    String mesa = "M001";
    String trama = "datosEnTrama";
    String firma = "firmaDigital";
    String meta = "metadatos";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    String pdfDigest = "digestValue";

    MensajeWsResponse successResponse = new MensajeWsResponse(0, true, "Procesado correctamente");
    when(actaService.receiveActa(any(ActaRequest.class))).thenReturn(successResponse);

    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");

      // Act
      MensajeWsResponse response = actaController.receiveActa(
        key, mesa, trama, firma, meta, tipoSolucion,
        tipoModulo, tipoTrama, npaginaNro, pdfDigest);

      // Assert
      assertNotNull(response);
      assertEquals(0, response.getCodigo());
      assertTrue(response.getSuccess());
    }

    // Verify - No debe llamar a doSynchro con la bandera desactivada
    verify(actaService, times(1)).receiveActa(any(ActaRequest.class));
    verify(synchroService, never()).doSynchro(anyString(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void testGetMethod() {
    // Act
    RedirectView result = actaController.getMethod();

    // Assert
    assertNotNull(result);
    assertEquals("/", result.getUrl());
  }

  @Test
  void testReceiveActaWithInvalidKey() {
    // Arrange
    String invalidKey = "claveIncorrecta"; // Clave diferente a la configurada
    String mesa = "M001";
    String trama = "datosEnTrama";
    String firma = "firmaDigital";
    String meta = "metadatos";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    String pdfDigest = "digestValue";

    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");

      // Act
      MensajeWsResponse response = actaController.receiveActa(
        invalidKey, mesa, trama, firma, meta, tipoSolucion,
        tipoModulo, tipoTrama, npaginaNro, pdfDigest);

      // Assert
      assertNotNull(response);
      assertEquals(2, response.getCodigo());
      assertFalse(response.getSuccess());
      assertEquals("Key de acceso incorrecta", response.getMessage());
    }

    // Verify - No debe llamar a ningún servicio con clave inválida
    verify(actaService, never()).receiveActa(any(ActaRequest.class));
    verify(synchroService, never()).doSynchro(anyString(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void testReceiveActaWithNullKey() {
    // Arrange
    String nullKey = null; // Clave nula
    String mesa = "M001";
    String trama = "datosEnTrama";
    String firma = "firmaDigital";
    String meta = "metadatos";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;
    Integer npaginaNro = 1;
    String pdfDigest = "digestValue";

    try (var mocked = mockStatic(WsOnpeUtils.class)) {
      mocked.when(WsOnpeUtils::getRequestUser).thenReturn("testUser");

      // Act
      MensajeWsResponse response = actaController.receiveActa(
        nullKey, mesa, trama, firma, meta, tipoSolucion,
        tipoModulo, tipoTrama, npaginaNro, pdfDigest);

      // Assert
      assertNotNull(response);
      assertEquals(2, response.getCodigo());
      assertFalse(response.getSuccess());
      assertEquals("Key de acceso incorrecta", response.getMessage());
    }

    // Verify - No debe llamar a ningún servicio con clave nula
    verify(actaService, never()).receiveActa(any(ActaRequest.class));
    verify(synchroService, never()).doSynchro(anyString(), anyInt(), anyInt(), anyInt());
  }


}
