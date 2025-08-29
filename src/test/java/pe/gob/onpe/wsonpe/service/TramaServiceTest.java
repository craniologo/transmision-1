package pe.gob.onpe.wsonpe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.TramaLogRequest;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

class TramaServiceTest {

  @Mock
  private TabTareasMesaRepository tramaRepository;

  @InjectMocks
  private TramaService tramaService;

  private ObjectMapper objectMapper;
  private String testUsuario;
  private TramaLogRequest testTramaRequest;
  private String testTramaJson;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    objectMapper = new ObjectMapper();
    testUsuario = "testUser";

    // Crear una solicitud de trama de prueba con los campos actualizados
    testTramaRequest = new TramaLogRequest()
      .setMesa("MESA001")
      .setTipoSolucion("1")
      .setTipoTrama("2")
      .setModulo("3")
      .setIdFlujo("4")
      .setData("{\"testData\":\"value\"}")
      .setTransmitio(0);

    // Convertir a JSON
    testTramaJson = objectMapper.writeValueAsString(new TramaLogRequest[]{testTramaRequest});
  }

  @Test
  void receiveTramaWithNullTramaShouldReturnError() {
    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, null);

    // Then
    assertEquals(2, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithEmptyTramaShouldReturnError() {
    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, "");

    // Then
    assertEquals(2, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithInvalidJsonShouldHandleException() {
    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, "{invalid json}");

    // Then
    assertEquals(2, response.getCodigo());
    assertTrue(response.getSuccess()); // Nota: el servicio devuelve true incluso con error de JSON
  }

  @Test
  void receiveTramaWithRegistroTareasShouldProcessSuccessfully() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Registro de tareas pendientes");

    Map<String, Object> registraTareasResponse = new HashMap<>();
    registraTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    registraTareasResponse.put("PO_MENSAJE", "Registro exitoso");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spInsercionTareas(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
      .thenReturn(registraTareasResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(WebService.REGISTRO_TAREAS, response.getCodigo());
    assertTrue(response.getSuccess());

    verify(tramaRepository).spVerificaTareas("MESA001",2);
    verify(tramaRepository).spInsercionTareas(
      "MESA001", testUsuario, 1, 2, 3, 4);
  }

  @Test
  void receiveTramaWithRegistroTareasShouldHandleError() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Registro de tareas pendientes");

    Map<String, Object> registraTareasResponse = new HashMap<>();
    registraTareasResponse.put("PO_RESULTADO", 999); // Código de error
    registraTareasResponse.put("PO_MENSAJE", "Error en registro");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spInsercionTareas(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
      .thenReturn(registraTareasResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(999, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithListaTareasShouldProcessMetadataAndFile() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.LISTA_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Lista de tareas pendientes");

    Map<String, Object> metadataResponse = new HashMap<>();
    metadataResponse.put("PO_RESULTADO", WebService.JSON_NOT_NULL);
    metadataResponse.put("PO_MENSAJE", "{\"metadata\":\"test\"}");

    Map<String, Object> fileResponse = new HashMap<>();
    fileResponse.put("PO_RESULTADO", WebService.JSON_NOT_NULL);
    fileResponse.put("PO_MENSAJE", "{\"file\":\"test\"}");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("M")))
      .thenReturn(metadataResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("F")))
      .thenReturn(fileResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(WebService.LISTA_TAREAS, response.getCodigo());
    assertTrue(response.getSuccess());

    verify(tramaRepository).spVerificaTareas("MESA001",2);
    verify(tramaRepository).spListaTareas("MESA001", 1, 2, 3, 4, "M");
    verify(tramaRepository).spListaTareas("MESA001", 1, 2, 3, 4, "F");
  }

  @Test
  void receiveTramaWithListaTareasShouldHandleMetadataError() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.LISTA_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Lista de tareas pendientes");

    Map<String, Object> metadataResponse = new HashMap<>();
    metadataResponse.put("PO_RESULTADO", 999); // Código de error
    metadataResponse.put("PO_MENSAJE", "Error al listar metadata");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("M")))
      .thenReturn(metadataResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(999, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithListaTareasShouldHandleFileError() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.LISTA_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Lista de tareas pendientes");

    Map<String, Object> metadataResponse = new HashMap<>();
    metadataResponse.put("PO_RESULTADO", WebService.JSON_NOT_NULL);
    metadataResponse.put("PO_MENSAJE", "{\"metadata\":\"test\"}");

    Map<String, Object> fileResponse = new HashMap<>();
    fileResponse.put("PO_RESULTADO", 999); // Código de error
    fileResponse.put("PO_MENSAJE", "Error al listar archivo");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("M")))
      .thenReturn(metadataResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("F")))
      .thenReturn(fileResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(999, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithListaTareasShouldHandleEmptyLists() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.LISTA_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Lista de tareas pendientes");

    Map<String, Object> metadataResponse = new HashMap<>();
    metadataResponse.put("PO_RESULTADO", WebService.JSON_NULL);
    metadataResponse.put("PO_MENSAJE", "");

    Map<String, Object> fileResponse = new HashMap<>();
    fileResponse.put("PO_RESULTADO", WebService.JSON_NULL);
    fileResponse.put("PO_MENSAJE", "");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("M")))
      .thenReturn(metadataResponse);
    when(tramaRepository.spListaTareas(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), eq("F")))
      .thenReturn(fileResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(WebService.LISTA_TAREAS, response.getCodigo());
    assertTrue(response.getSuccess());
  }

  @Test
  void receiveTramaWithRegistroDTareasShouldReturnSuccess() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_D_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Registro duplicado de tareas");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(WebService.REGISTRO_D_TAREAS, response.getCodigo());
    assertTrue(response.getSuccess());
  }

  @Test
  void receiveTramaWithMesaTransShouldReturnSuccess() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.MESA_TRANS);
    verificaTareasResponse.put("PO_MENSAJE", "Mesa ya transmitida");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(WebService.MESA_TRANS, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithOtherErrorShouldReturnError() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", 999); // Código de error
    verificaTareasResponse.put("PO_MENSAJE", "Error desconocido");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    assertEquals(999, response.getCodigo());
    assertFalse(response.getSuccess());
  }

  @Test
  void receiveTramaWithMultipleItemsShouldProcessAllItems() throws Exception {
    // Given
    TramaLogRequest request1 = new TramaLogRequest()
      .setMesa("MESA001")
      .setTipoSolucion("1")
      .setTipoTrama("2")
      .setModulo("3")
      .setIdFlujo("4")
      .setData("{\"testData\":\"value1\"}")
      .setTransmitio(0);

    TramaLogRequest request2 = new TramaLogRequest()
      .setMesa("MESA001")
      .setTipoSolucion("5")
      .setTipoTrama("6")
      .setModulo("7")
      .setIdFlujo("8")
      .setData("{\"testData\":\"value2\"}")
      .setTransmitio(0);

    String multiTramaJson = objectMapper.writeValueAsString(new TramaLogRequest[]{request1, request2});

    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Registro de tareas pendientes");

    Map<String, Object> registraTareasResponse = new HashMap<>();
    registraTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    registraTareasResponse.put("PO_MENSAJE", "Registro exitoso");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spInsercionTareas(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
      .thenReturn(registraTareasResponse);

    // When
    MensajeWsResponse response = tramaService.receiveTrama(testUsuario, multiTramaJson);

    // Then
    assertEquals(WebService.REGISTRO_TAREAS, response.getCodigo());
    assertTrue(response.getSuccess());

    // Verificar que se procesaron las dos solicitudes
    verify(tramaRepository).spInsercionTareas("MESA001", testUsuario, 1, 2, 3, 4);
    verify(tramaRepository).spInsercionTareas("MESA001", testUsuario, 5, 6, 7, 8);
  }

  @Test
  void receiveTramaWithStringValuesShouldConvertToIntegers() {
    // Given
    Map<String, Object> verificaTareasResponse = new HashMap<>();
    verificaTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    verificaTareasResponse.put("PO_MENSAJE", "Registro de tareas pendientes");

    Map<String, Object> registraTareasResponse = new HashMap<>();
    registraTareasResponse.put("PO_RESULTADO", WebService.REGISTRO_TAREAS);
    registraTareasResponse.put("PO_MENSAJE", "Registro exitoso");

    when(tramaRepository.spVerificaTareas(anyString(),anyInt())).thenReturn(verificaTareasResponse);
    when(tramaRepository.spInsercionTareas(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
      .thenReturn(registraTareasResponse);

    // When
    tramaService.receiveTrama(testUsuario, testTramaJson);

    // Then
    // Verifica que los strings se conviertan correctamente a enteros para la llamada al procedimiento
    verify(tramaRepository).spInsercionTareas(
      "MESA001", // mesa (String)
      testUsuario, // usuario (String)
      1, // tipoSolucion convertido a Integer
      2, // tipoTrama convertido a Integer
      3, // modulo convertido a Integer
      4  // idFlujo convertido a Integer
    );
  }
}
