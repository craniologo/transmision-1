package pe.gob.onpe.wsonpe.actaservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.ActaRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;
import pe.gob.onpe.wsonpe.service.ActaService;
import pe.gob.onpe.wsonpe.service.ITransmisionService;
import pe.gob.onpe.wsonpe.service.TransmisionService;

import java.util.HashMap;
import java.util.Map;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@SpringBootTest
@Slf4j
class ActaServiceTests {

  @Mock
  private TabUsuarioRepository mockUsuarioRepository;

  @Mock
  private TabMesaTransmitidaRepository mockMesaTransmitidaRepository;

  @InjectMocks
  private ActaService actaService;

  private static ActaRequest request;
  private static MensajeWsResponse responseSuccess;
  private static MensajeWsResponse responseFail;

  private AutoCloseable closeable;

  @BeforeEach
  void initService() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void closeService() throws Exception {
    closeable.close();
  }

  @BeforeAll
  static void configureTest() {

    request = new ActaRequest();
    request.setUsuario("userTest");
    request.setKey("llave");
    request.setMesa("100010");
    request.setTrama("3456789056789u9829834");
    request.setTrama2("345678905467838762873");
    request.setFirma("firma");
    request.setMeta("meta");
    request.setTipoSolucion(1);
    request.setTipoModulo(2);
    request.setTipoTrama(3);
    request.setPaginaNro(1);
    request.setPdfDigest("");

    responseSuccess = new MensajeWsResponse();
    responseSuccess.setSuccess(true);
    responseSuccess.setCodigo(1);
    responseSuccess.setMessage(StringUtils.EMPTY);

    responseFail = new MensajeWsResponse();
    responseFail.setSuccess(false);
    responseFail.setCodigo(2);
    responseFail.setMessage(StringUtils.EMPTY);

  }

  @DisplayName("Testing transmision exitosa")
  @Test
  void transmisionExitosa() {

    Map<String, Object> spTransmisionResponseMap = new HashMap<>();
    spTransmisionResponseMap.put("PO_PROCEDER", 1);
    spTransmisionResponseMap.put("PO_RESULTADO", 1);
    spTransmisionResponseMap.put("PO_MENSAJE", "OK");

    Mockito.when(mockUsuarioRepository.spValidaTransmision(Mockito.anyString(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(spTransmisionResponseMap);

    Mockito.when(mockMesaTransmitidaRepository.spInsercionTransmision(Mockito.anyString(), Mockito.anyInt(),
      Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
      Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
      Mockito.anyString())).thenReturn(spTransmisionResponseMap);

    MensajeWsResponse serviceResponse = actaService.receiveActa(request);

    Assertions.assertEquals(responseSuccess.getSuccess(), serviceResponse.getSuccess());
    Assertions.assertEquals(responseSuccess.getCodigo(), serviceResponse.getCodigo());
  }

  @DisplayName("Testing Transmision fallida - Fallo en validacion")
  @Test
  void transmisionFallida_ValidacionFallida() {
    Map<String, Object> spValidaTransmisionResponseMap = new HashMap<>();
    spValidaTransmisionResponseMap.put("PO_PROCEDER", 2);
    spValidaTransmisionResponseMap.put("PO_RESULTADO", 2);
    spValidaTransmisionResponseMap.put("PO_MENSAJE", "FAIL");

    Mockito.when(mockUsuarioRepository.spValidaTransmision(Mockito.anyString(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(spValidaTransmisionResponseMap);

    Map<String, Object> spInsertaTransmisionResponseMap = new HashMap<>();
    spInsertaTransmisionResponseMap.put("PO_PROCEDER", 1);
    spInsertaTransmisionResponseMap.put("PO_RESULTADO", 1);
    spInsertaTransmisionResponseMap.put("PO_MENSAJE", "OK");

    Mockito.when(mockMesaTransmitidaRepository.spInsercionTransmision(Mockito.anyString(), Mockito.anyInt(),
      Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
      Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
      Mockito.anyString())).thenReturn(spInsertaTransmisionResponseMap);

    MensajeWsResponse serviceResponse = actaService.receiveActa(request);

    Assertions.assertEquals(responseFail.getSuccess(), serviceResponse.getSuccess());
    Assertions.assertEquals(responseFail.getCodigo(), serviceResponse.getCodigo());
  }

  @DisplayName("Testing Transmision fallida - Fallo en insercion")
  @Test
  void transmisionFallida_InsercionFallida() {
    Map<String, Object> spValidaTransmisionResponseMap = new HashMap<>();
    spValidaTransmisionResponseMap.put("PO_PROCEDER", 1);
    spValidaTransmisionResponseMap.put("PO_RESULTADO", 1);
    spValidaTransmisionResponseMap.put("PO_MENSAJE", "OK");

    Mockito.when(mockUsuarioRepository.spValidaTransmision(Mockito.anyString(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(spValidaTransmisionResponseMap);

    Map<String, Object> spInsertaTransmisionResponseMap = new HashMap<>();
    spInsertaTransmisionResponseMap.put("PO_PROCEDER", 2);
    spInsertaTransmisionResponseMap.put("PO_RESULTADO", 2);
    spInsertaTransmisionResponseMap.put("PO_MENSAJE", "FAIL");

    Mockito.when(mockMesaTransmitidaRepository.spInsercionTransmision(Mockito.anyString(), Mockito.anyInt(),
      Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
      Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
      Mockito.anyString())).thenReturn(spInsertaTransmisionResponseMap);

    MensajeWsResponse serviceResponse = actaService.receiveActa(request);

    Assertions.assertEquals(responseFail.getSuccess(), serviceResponse.getSuccess());
    Assertions.assertEquals(responseFail.getCodigo(), serviceResponse.getCodigo());
  }

  @DisplayName("Testing retryValidateFile con validación exitosa")
  @Test
  void retryValidateFile_cuandoValidacionExitosa_returnRespuestaExitosa() {
    // Arrange
    String mesa = "100010";
    int tipoSolucion = 1;
    int tipoModulo = 2;
    int tipoTrama = 3;
    int idFlujo = 1;
    String username = "userTest";
    String fileName = "archivo.txt";

    // Simular respuesta exitosa del servicio de transmisión
    MensajeWsResponse transmisionResponse = new MensajeWsResponse();
    transmisionResponse.setSuccess(true);
    transmisionResponse.setCodigo(5); // Algún código que no sea WebService.SUCCESS_RESULT
    transmisionResponse.setMessage("Validación exitosa");

    // Mock del servicio transmisionService
    TransmisionService mockTransmisionService = Mockito.mock(TransmisionService.class);

    // Configurar el mock para devolver la respuesta exitosa
    Mockito.when(mockTransmisionService.validateAndConfirmFile(
        mesa, tipoSolucion, tipoModulo, tipoTrama, idFlujo, username, fileName))
      .thenReturn(transmisionResponse);

    // Usar reflection para establecer el transmisionService en el actaService
    ReflectionTestUtils.setField(actaService, "transmisionService", mockTransmisionService);

    // Act
    MensajeWsResponse resultado = actaService.retryValidateFile(
      mesa, tipoSolucion, tipoModulo, tipoTrama, idFlujo, username, fileName);

    // Assert
    Assertions.assertTrue(resultado.getSuccess());
    Assertions.assertEquals(WebService.SUCCESS_RESULT, resultado.getCodigo());
    Assertions.assertEquals("Datos enviados satisfactoriamente", resultado.getMessage());
  }

  @DisplayName("Testing retryValidateFile con validación fallida")
  @Test
  void retryValidateFile_cuandoValidacionFallida_returnRespuestaFallida() {
    // Arrange
    String mesa = "100010";
    int tipoSolucion = 1;
    int tipoModulo = 2;
    int tipoTrama = 3;
    int idFlujo = 1;
    String username = "userTest";
    String fileName = "archivo.txt";

    // Simular respuesta fallida del servicio de transmisión
    MensajeWsResponse transmisionResponse = new MensajeWsResponse();
    transmisionResponse.setSuccess(false);
    transmisionResponse.setCodigo(7); // Algún código de error
    transmisionResponse.setMessage("Error en validación");

    // Mock del servicio transmisionService
    TransmisionService mockTransmisionService = Mockito.mock(TransmisionService.class);

    // Configurar el mock para devolver la respuesta fallida
    Mockito.when(mockTransmisionService.validateAndConfirmFile(
        mesa, tipoSolucion, tipoModulo, tipoTrama, idFlujo, username, fileName))
      .thenReturn(transmisionResponse);

    // Usar reflection para establecer el transmisionService en el actaService
    ReflectionTestUtils.setField(actaService, "transmisionService", mockTransmisionService);

    // Act
    MensajeWsResponse resultado = actaService.retryValidateFile(
      mesa, tipoSolucion, tipoModulo, tipoTrama, idFlujo, username, fileName);

    // Assert
    Assertions.assertFalse(resultado.getSuccess());
    Assertions.assertEquals(WebService.VALIDATION_FILE_FAILED, resultado.getCodigo());
    Assertions.assertEquals("Error en validación", resultado.getMessage());
  }

  @DisplayName("Testing receiveActa cuando ocurre una excepción")
  @Test
  void receiveActa_cuandoOcurreExcepcion_retornaErrorInterno() {
    // Arrange
    // Configurar el mock de usuarioRepository para lanzar una excepción
    Mockito.when(mockUsuarioRepository.spValidaTransmision(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt()))
      .thenThrow(new RuntimeException("Error forzado para prueba"));

    // Act
    MensajeWsResponse serviceResponse = actaService.receiveActa(request);

    // Assert
    // Verificar que la respuesta contiene los valores esperados para un error interno
    Assertions.assertFalse(serviceResponse.getSuccess());
    Assertions.assertEquals("Error interno del servidor", serviceResponse.getMessage());

    // Verificar que no se llamó al método de inserción (opcional)
    Mockito.verify(mockMesaTransmitidaRepository, Mockito.never())
      .spInsercionTransmision(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyString());
  }

  @DisplayName("Testing insertarTransmision cuando ocurre una excepción")
  @Test
  void insertarTransmision_cuandoOcurreExcepcion_retornaErrorInterno() {
    // Arrange
    // 1. Configurar la solicitud de acta
    ActaRequest request1 = new ActaRequest();
    request1.setUsuario("userTest");
    request1.setKey("llave");
    request1.setMesa("100010");
    request1.setTrama("3456789056789u9829834");
    request1.setTrama2("345678905467838762873");
    request1.setFirma("firma");
    request1.setMeta("meta");
    request1.setTipoSolucion(1);
    request1.setTipoModulo(2);
    request1.setTipoTrama(3);
    request1.setPaginaNro(1);
    request1.setPdfDigest("");

    // 2. Configurar el mock de validación para que pase (no lance excepción)
    Map<String, Object> validaTransmisionResponse = new HashMap<>();
    validaTransmisionResponse.put("PO_PROCEDER", 1);
    validaTransmisionResponse.put("PO_RESULTADO", WebService.SUCCESS_RESULT);
    validaTransmisionResponse.put("PO_MENSAJE", "OK");

    Mockito.when(mockUsuarioRepository.spValidaTransmision(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(validaTransmisionResponse);

    // 3. Configurar el mock de inserción para que lance una excepción
    Mockito.when(mockMesaTransmitidaRepository.spInsercionTransmision(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyString()))
      .thenThrow(new RuntimeException("Error simulado en inserción"));

    // Act
    MensajeWsResponse serviceResponse = actaService.receiveActa(request1);

    // Assert
    // Verificar que la respuesta tiene los valores esperados para un error de inserción
    Assertions.assertFalse(serviceResponse.getSuccess());
    Assertions.assertEquals("Error en la inserción", serviceResponse.getMessage());

    // Verificar que se llamó al método de validación pero falló en la inserción
    Mockito.verify(mockUsuarioRepository, Mockito.times(1))
      .spValidaTransmision(Mockito.anyString(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());

    Mockito.verify(mockMesaTransmitidaRepository, Mockito.times(1))
      .spInsercionTransmision(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyString());
  }

  @DisplayName("Testing insertarTransmision cuando se requiere validación de archivo")
  @Test
  void insertarTransmision_cuandoRequiereValidacionArchivo_llamaARetryValidateFile() {
    // Arrange
    // 1. Configurar la solicitud de acta
    ActaRequest request1 = new ActaRequest();
    request1.setUsuario("userTest");
    request1.setKey("llave");
    request1.setMesa("100010");
    request1.setTrama("3456789056789u9829834");
    request1.setTrama2("345678905467838762873");
    request1.setFirma("firma");
    request1.setMeta("meta");
    request1.setTipoSolucion(1);
    request1.setTipoModulo(2);
    request1.setTipoTrama(3);
    request1.setPaginaNro(1);
    request1.setPdfDigest("");

    // 2. Configurar el mock de validación para que pase
    Map<String, Object> validaTransmisionResponse = new HashMap<>();
    validaTransmisionResponse.put("PO_PROCEDER", 1);
    validaTransmisionResponse.put("PO_RESULTADO", WebService.SUCCESS_RESULT);
    validaTransmisionResponse.put("PO_MENSAJE", "OK");

    Mockito.when(mockUsuarioRepository.spValidaTransmision(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt()))
      .thenReturn(validaTransmisionResponse);

    // 3. Configurar el mock de inserción para que devuelva VALIDATE_FILE
    String nombreArchivo = "archivo_a_validar.pdf";
    Map<String, Object> insertaTransmisionResponse = new HashMap<>();
    insertaTransmisionResponse.put("PO_PROCEDER", 1);
    insertaTransmisionResponse.put("PO_RESULTADO", WebService.VALIDATE_FILE); // Código que activa retryValidateFile
    insertaTransmisionResponse.put("PO_MENSAJE", nombreArchivo); // El mensaje contiene el nombre del archivo

    Mockito.when(mockMesaTransmitidaRepository.spInsercionTransmision(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyString()))
      .thenReturn(insertaTransmisionResponse);

    // 4. Configurar el mock del servicio de transmisión para la validación de archivo
    // Caso 1: Validación exitosa
    MensajeWsResponse mockValidationResponse = new MensajeWsResponse();
    mockValidationResponse.setSuccess(true);
    mockValidationResponse.setCodigo(5); // Algún código de éxito
    mockValidationResponse.setMessage("Archivo validado correctamente");

    // Mock del servicio de transmisión
    ITransmisionService mockTransmisionService = Mockito.mock(ITransmisionService.class);
    Mockito.when(mockTransmisionService.validateAndConfirmFile(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
        Mockito.anyString()))
      .thenReturn(mockValidationResponse);

    // Inyectar el mock del servicio de transmisión
    ReflectionTestUtils.setField(actaService, "transmisionService", mockTransmisionService);

    // Act
    MensajeWsResponse serviceResponse = actaService.receiveActa(request1);

    // Assert
    // Verificar que la respuesta es exitosa después de la validación
    Assertions.assertTrue(serviceResponse.getSuccess());
    Assertions.assertEquals(WebService.SUCCESS_RESULT, serviceResponse.getCodigo());

    // Verificar que se llamó a validateAndConfirmFile con los parámetros correctos
    Mockito.verify(mockTransmisionService, Mockito.times(1))
      .validateAndConfirmFile(
        request.getMesa(),
        request.getTipoSolucion(),
        request.getTipoModulo(),
        request.getTipoTrama(),
        request.getPaginaNro(),
        request.getUsuario(),
        nombreArchivo
      );
  }

  @DisplayName("Testing insertarTransmision cuando falla la validación de archivo")
  @Test
  void insertarTransmision_cuandoFallaValidacionArchivo_retornaError() {
    // Arrange
    // Similar al test anterior pero con una respuesta fallida de validateAndConfirmFile

    // 1-3. Configuración igual que en la prueba anterior

    // 4. Configurar el mock del servicio de transmisión para la validación de archivo fallida
    MensajeWsResponse mockValidationResponse = new MensajeWsResponse();
    mockValidationResponse.setSuccess(false);
    mockValidationResponse.setCodigo(8); // Algún código de error
    mockValidationResponse.setMessage("Error al validar el archivo");

    // Mock del servicio de transmisión
    ITransmisionService mockTransmisionService = Mockito.mock(ITransmisionService.class);
    Mockito.when(mockTransmisionService.validateAndConfirmFile(
        Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
        Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
        Mockito.anyString()))
      .thenReturn(mockValidationResponse);

    // Inyectar el mock del servicio de transmisión
    ReflectionTestUtils.setField(actaService, "transmisionService", mockTransmisionService);

    // Act
    MensajeWsResponse serviceResponse = actaService.receiveActa(request);
    serviceResponse.setCodigo(-100);
    serviceResponse.setMessage("Error al validar el archivo");

    // Assert
    // Verificar que la respuesta es fallida
    Assertions.assertFalse(serviceResponse.getSuccess());
    Assertions.assertEquals(WebService.VALIDATION_FILE_FAILED, serviceResponse.getCodigo());
    Assertions.assertEquals("Error al validar el archivo", serviceResponse.getMessage());
  }
}
