package pe.gob.onpe.wsonpe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.onpe.wsonpe.dto.LoginRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.model.TabEleccion;
import pe.gob.onpe.wsonpe.repository.TabConfTxRepository;
import pe.gob.onpe.wsonpe.repository.TabEleccionRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;
import pe.gob.onpe.wsonpe.utils.Crypto;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock
  private TabUsuarioRepository usuarioRepository;

  @Mock
  private TabConfTxRepository confTxRepository;

  @Mock
  private TabEleccionRepository eleccionRepository;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private LoginService loginService;

  private LoginRequest loginRequest;
  private List<TabEleccion> mockElecciones;

  @BeforeEach
  void setUp() {
    // Configurar valores de propiedades via ReflectionTestUtils
    ReflectionTestUtils.setField(loginService, "jwtLifetime", 60);        // 60 minutos
    ReflectionTestUtils.setField(loginService, "refreshJwtLifetime", 1440); // 24 horas

    // Configurar request
    loginRequest = new LoginRequest();
    loginRequest.setUsuario("testUser");
    loginRequest.setClave("testPassword");
    loginRequest.setRefreshToken(false);

    // Configurar mock de elecciones
    mockElecciones = new ArrayList<>();
    TabEleccion eleccion = new TabEleccion();
    eleccion.setCEleccionPk("E001");
    eleccion.setCNombreCortoEleccion("Elección Test");
    mockElecciones.add(eleccion);
  }

  @Test
  void login_whenCredentialsAreInvalid_returnsErrorResponse() {
    // Arrange
    Map<String, Object> spResponse = new HashMap<>();
    spResponse.put("PO_RESULTADO", 0);
    spResponse.put("PO_MENSAJE", "Credenciales inválidas");

    try (MockedStatic<WsOnpeUtils> utilities = Mockito.mockStatic(WsOnpeUtils.class)) {
      utilities.when(() -> WsOnpeUtils.getSHA(anyString())).thenReturn("hashedPassword");

      when(usuarioRepository.spValidaAcceso(anyString(), anyString())).thenReturn(spResponse);

      // Act
      MensajeWsResponse result = loginService.login(loginRequest);

      // Assert
      assertEquals(0, result.getCodigo());
      assertEquals("Credenciales inválidas", result.getMessage());
      verify(usuarioRepository).spValidaAcceso("testUser", "hashedPassword");
      verifyNoMoreInteractions(confTxRepository);
      verifyNoMoreInteractions(eleccionRepository);
    }
  }

  @Test
  void login_whenJwtGenerationFails_returnsErrorResponse() {
    // Arrange
    Map<String, Object> spResponse = new HashMap<>();
    spResponse.put("PO_RESULTADO", 1);
    spResponse.put("PO_MENSAJE", "Usuario válido");

    try (MockedStatic<WsOnpeUtils> utilities = Mockito.mockStatic(WsOnpeUtils.class);
         MockedStatic<Crypto> cryptoStatic = Mockito.mockStatic(Crypto.class)) {

      utilities.when(() -> WsOnpeUtils.getSHA(anyString())).thenReturn("hashedPassword");
      cryptoStatic.when(() -> Crypto.encryptStringAES(anyString())).thenThrow(new RuntimeException("Encryption error"));

      when(usuarioRepository.spValidaAcceso(anyString(), anyString())).thenReturn(spResponse);

      // Act
      MensajeWsResponse result = loginService.login(loginRequest);

      // Assert
      assertEquals(2, result.getCodigo());
      assertEquals("Error generando el token", result.getMessage());
      verify(usuarioRepository).spValidaAcceso("testUser", "hashedPassword");
      verifyNoMoreInteractions(confTxRepository);
      verifyNoMoreInteractions(eleccionRepository);
    }
  }

  // Método helper para la prueba con ObjectMapper real
  private static class LoginService extends pe.gob.onpe.wsonpe.service.LoginService {
    public LoginService(TabUsuarioRepository usuarioRepository, TabConfTxRepository confTxRepository, TabEleccionRepository eleccionRepository) {
      super(usuarioRepository, confTxRepository, eleccionRepository);
    }

    protected ObjectMapper createObjectMapper() {
      return new ObjectMapper();
    }
  }

}
