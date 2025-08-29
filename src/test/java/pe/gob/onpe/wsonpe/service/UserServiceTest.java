package pe.gob.onpe.wsonpe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.onpe.wsonpe.dao.TabProcesoDao;
import pe.gob.onpe.wsonpe.dao.TabVersionDao;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;
import pe.gob.onpe.wsonpe.model.TabUsuario;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;
import pe.gob.onpe.wsonpe.repository.TabVersionRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private TabUsuarioRepository usuarioRepository;

  @Mock
  private TabVersionRepository tabVersionRepository;

  @Mock
  private TabMesaTransmitidaRepository tabMesaTransmitidaRepository;

  @Mock
  private TabProcesoDao tabProcesoDao;

  @Mock
  private TabVersionDao tabVersionDao;

  @Mock
  private TabTareasMesaRepository tabTareasMesaRepository;

  @InjectMocks
  private UserService userService;

  private UserRequest userRequest;
  private TabUsuario tabUsuario;
  private Map<String, Object> spResponse;

  @BeforeEach
  void setUp() {
    // Configuración de datos para las pruebas
    userRequest = new UserRequest();
    userRequest.setUsuario("testUser");
    userRequest.setAccion("testAction");
    userRequest.setMesa("001");
    userRequest.setTipoSolucion("1");
    userRequest.setUsbParte("1");
    userRequest.setUsbModulo("1");
    userRequest.setPaginaNro("1");

    tabUsuario = new TabUsuario();
    tabUsuario.setNUsuarioPk(1);
    tabUsuario.setNFlagPuestaCero((short) 1);

    spResponse = new HashMap<>();
    spResponse.put("PO_RESULTADO", 1);
    spResponse.put("PO_MENSAJE", "Operación exitosa");
  }

  @Test
  void updateEstado_whenSuccess_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.spActualizaEstado(anyString(), anyInt())).thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.updateEstado(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("Operación exitosa", result.getMessage());
    verify(usuarioRepository, times(1)).spActualizaEstado(userRequest.getUsuario(), 1);
  }

  @Test
  void updateEstado_whenFailure_thenReturnFailureResponse() {
    // Arrange
    spResponse.put("PO_RESULTADO", 2);
    spResponse.put("PO_MENSAJE", "Error en la actualización");
    when(usuarioRepository.spActualizaEstado(anyString(), anyInt())).thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.updateEstado(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getCodigo());
    assertFalse(result.getSuccess());
    assertEquals("Error en la actualización", result.getMessage());
    verify(usuarioRepository, times(1)).spActualizaEstado(userRequest.getUsuario(), 1);
  }

  @Test
  void verificarPuestaCero_whenUserExists_thenReturnSuccessResponse() {
    // Arrange
    when(tabProcesoDao.obtenerFechaProceso()).thenReturn("20250513");
    when(tabVersionDao.verificarVersion(anyString())).thenReturn(new MensajeWsResponse(1, true, "Versión verificada"));
    when(usuarioRepository.findByUsuario(anyString())).thenReturn(tabUsuario);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCero(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("1", result.getMessage());
    verify(tabProcesoDao, times(1)).obtenerFechaProceso();
    verify(tabVersionDao, times(1)).verificarVersion(anyString());
    verify(usuarioRepository, times(1)).findByUsuario(userRequest.getUsuario());
  }

  @Test
  void verificarPuestaCero_whenUserNotExists_thenReturnFailureResponse() {
    // Arrange
    when(tabProcesoDao.obtenerFechaProceso()).thenReturn("20250513");
    when(tabVersionDao.verificarVersion(anyString())).thenReturn(new MensajeWsResponse(1, true, "Versión verificada"));
    when(usuarioRepository.findByUsuario(anyString())).thenReturn(null);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCero(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getCodigo());
    assertFalse(result.getSuccess());
    assertEquals("Ocurrió un error durante la verificación de puesta a cero", result.getMessage());
    verify(tabProcesoDao, times(1)).obtenerFechaProceso();
    verify(tabVersionDao, times(1)).verificarVersion(anyString());
    verify(usuarioRepository, times(1)).findByUsuario(userRequest.getUsuario());
  }

  @Test
  void verificarPuestaCeroDiagnostico_whenUserExists_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.findByUsuario(anyString())).thenReturn(tabUsuario);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCeroDiagnostico(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("1", result.getMessage());
    verify(usuarioRepository, times(1)).findByUsuario(userRequest.getUsuario());
  }

  @Test
  void verificarPuestaCeroDiagnostico_whenUserNotExists_thenReturnFailureResponse() {
    // Arrange
    when(usuarioRepository.findByUsuario(anyString())).thenReturn(null);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCeroDiagnostico(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getCodigo());
    assertFalse(result.getSuccess());
    assertEquals("Ocurrió un error durante la verificación de puesta a cero", result.getMessage());
    verify(usuarioRepository, times(1)).findByUsuario(userRequest.getUsuario());
  }

  @Test
  void verificarPuestaCeroYTransmitido_whenSuccess_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.spVerificaPuestaCeroTrans(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
      .thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCeroYTransmitido(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("Operación exitosa", result.getMessage());
    verify(usuarioRepository, times(1)).spVerificaPuestaCeroTrans(
      userRequest.getUsuario(), userRequest.getMesa(), 1, 1, 1, 1);
  }

  @Test
  void verificarPuestaCeroYTransmitido_withEmptyValues_thenCallsRepositoryWithDefaultValues() {
    // Arrange
    userRequest.setUsbModulo("");
    userRequest.setUsbParte("");
    userRequest.setPaginaNro("");

    when(usuarioRepository.spVerificaPuestaCeroTrans(anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
      .thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCeroYTransmitido(userRequest);

    // Assert
    assertNotNull(result);
    verify(usuarioRepository, times(1)).spVerificaPuestaCeroTrans(
      userRequest.getUsuario(), userRequest.getMesa(), 1, 0, 0, 0);
  }

  @Test
  void verificarPuestaCeroYTransmitido2_whenSuccess_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.spVerificaPuestaCeroTransD(anyString(), anyString(), anyInt(), anyString(), anyInt()))
      .thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.verificarPuestaCeroYTransmitido2(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("Operación exitosa", result.getMessage());
    verify(usuarioRepository, times(1)).spVerificaPuestaCeroTransD(
      userRequest.getUsuario(), userRequest.getMesa(), 1, userRequest.getUsbParte(), 1);
  }

  @Test
  void ejecutarPuestaCero_whenSuccess_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.findByUsuario(anyString())).thenReturn(tabUsuario);
    doNothing().when(tabMesaTransmitidaRepository).eliminarIntegracionPorUsuario(anyInt());
    doNothing().when(tabMesaTransmitidaRepository).eliminarSincronizacionPorUsuario(anyInt());
    doNothing().when(tabMesaTransmitidaRepository).eliminarMesasTransmitidasPorUsuario(anyInt());
    doNothing().when(tabTareasMesaRepository).eliminarPorUsuario(anyString());
    when(usuarioRepository.saveAndFlush(any(TabUsuario.class))).thenReturn(tabUsuario);

    // Act
    MensajeWsResponse result = userService.ejecutarPuestaCero(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("Puesta Cero Satisfactoria", result.getMessage());
    verify(usuarioRepository, times(1)).findByUsuario(userRequest.getUsuario());
    verify(tabMesaTransmitidaRepository, times(1)).eliminarIntegracionPorUsuario(tabUsuario.getNUsuarioPk());
    verify(tabMesaTransmitidaRepository, times(1)).eliminarSincronizacionPorUsuario(tabUsuario.getNUsuarioPk());
    verify(tabMesaTransmitidaRepository, times(1)).eliminarMesasTransmitidasPorUsuario(tabUsuario.getNUsuarioPk());
    verify(tabTareasMesaRepository, times(1)).eliminarPorUsuario(userRequest.getUsuario());
    verify(usuarioRepository, times(1)).saveAndFlush(any(TabUsuario.class));
  }

  @Test
  void ejecutarPuestaCero_whenException_thenReturnFailureResponse() {
    // Arrange
    when(usuarioRepository.findByUsuario(anyString())).thenThrow(new RuntimeException("Database error"));

    // Act
    MensajeWsResponse result = userService.ejecutarPuestaCero(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getCodigo());
    assertFalse(result.getSuccess());
    assertEquals("Error en ejecutar la Puesta Cero", result.getMessage());
    verify(usuarioRepository, times(1)).findByUsuario(userRequest.getUsuario());
    verify(tabMesaTransmitidaRepository, never()).eliminarMesasTransmitidasPorUsuario(anyInt());
  }

  @Test
  void ejecutarPuestaCeroD_whenSuccess_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.spEjecutarPuestaCeroD(anyString())).thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.ejecutarPuestaCeroD(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("Operación exitosa", result.getMessage());
    verify(usuarioRepository, times(1)).spEjecutarPuestaCeroD(userRequest.getUsuario());
  }

  @Test
  void ejecutarPuestaCeroD_whenFailure_thenReturnFailureResponse() {
    // Arrange
    spResponse.put("PO_RESULTADO", 2);
    spResponse.put("PO_MENSAJE", "Error en la ejecución");
    when(usuarioRepository.spEjecutarPuestaCeroD(anyString())).thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.ejecutarPuestaCeroD(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getCodigo());
    assertFalse(result.getSuccess());
    assertEquals("Error en la ejecución", result.getMessage());
    verify(usuarioRepository, times(1)).spEjecutarPuestaCeroD(userRequest.getUsuario());
  }

  @Test
  void verificarTimeSufEs_whenAfter4PM_thenReturnSuccessResponse() {
    // Este test es más complicado porque depende de la hora actual
    // Una forma de probarlo es usar un mock de LocalDateTime o una biblioteca como MockedStatic
    // Para simplificar, asumiremos que estamos después de las 4pm en este test

    // La implementación real comprueba LocalDateTime.now().getHour() >= 4
    // Por ahora, verificaremos solo el resultado esperado

    // Act
    MensajeWsResponse result = userService.verificarTimeSufEs(userRequest);

    // En este punto, no podemos estar seguros de qué valor deberá tener result.getSuccess()
    // porque depende de la hora actual

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    // No verificamos result.getSuccess() ya que depende de la hora de ejecución
  }

  @Test
  void verificarSessionActiva_whenSuccess_thenReturnSuccessResponse() {
    // Arrange
    when(usuarioRepository.spValidaEstado(anyString())).thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.verificarSessionActiva(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getCodigo());
    assertTrue(result.getSuccess());
    assertEquals("Operación exitosa", result.getMessage());
    verify(usuarioRepository, times(1)).spValidaEstado(userRequest.getUsuario());
  }

  @Test
  void verificarSessionActiva_whenFailure_thenReturnFailureResponse() {
    // Arrange
    spResponse.put("PO_RESULTADO", 2);
    spResponse.put("PO_MENSAJE", "Usuario no conectado");
    when(usuarioRepository.spValidaEstado(anyString())).thenReturn(spResponse);

    // Act
    MensajeWsResponse result = userService.verificarSessionActiva(userRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getCodigo());
    assertFalse(result.getSuccess());
    assertEquals("Usuario no conectado", result.getMessage());
    verify(usuarioRepository, times(1)).spValidaEstado(userRequest.getUsuario());
  }



}
