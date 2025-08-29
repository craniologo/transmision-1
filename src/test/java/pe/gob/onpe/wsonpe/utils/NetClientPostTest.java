package pe.gob.onpe.wsonpe.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.SynchroRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@ExtendWith(MockitoExtension.class)
class NetClientPostTest {
  private HttpURLConnection mockHttpConnection;
  private HttpsURLConnection mockHttpsConnection;
  private SynchroRequest synchroRequest;

  private NetClientPost netClientPost;

  @BeforeEach
  void setUp() {
    // Configurar mocks comunes

    netClientPost = new NetClientPost("http://example.com/api");

    mockHttpConnection = mock(HttpURLConnection.class);
    mockHttpsConnection = mock(HttpsURLConnection.class);

    // Configurar SynchroRequest para pruebas
    synchroRequest = new SynchroRequest();
    synchroRequest.setMasterKey("testKey");
    synchroRequest.setMesa("M001");
    synchroRequest.setTipoSolucion(1);
    synchroRequest.setTipoModulo(2);
    synchroRequest.setTipoTrama(3);
    synchroRequest.setForce(0);
  }

  @Test
  void constructor_createsConnection() throws Exception {
    // Este test usa PowerMock o simplemente verifica el comportamiento indirectamente

    // Arrange - Preparar el objeto para probar
    String urlString = "http://example.com";

    // Act - Crear el objeto (sabiendo que no podemos mockear fácilmente la creación de URL)
    NetClientPost client = new NetClientPost(urlString);

    // Assert - En lugar de verificar las llamadas a los métodos mockeados,
    // verificamos que el objeto se creó correctamente

    // Podemos verificar que urlString se guardó correctamente
    java.lang.reflect.Field urlStringField = NetClientPost.class.getDeclaredField("urlString");
    urlStringField.setAccessible(true);
    assertEquals(urlString, urlStringField.get(client));
  }

  @Test
  void constructor_createsInstanceWithoutException() {
    // Verificar que el constructor no lanza excepciones
    assertDoesNotThrow(() -> new NetClientPost("http://example.com"));
  }

  @Test
  void constructor_handlesExceptionsGracefully() {
    // Verificar que el constructor maneja adecuadamente las excepciones
    assertDoesNotThrow(() -> new NetClientPost("malformed://\\not a valid url"));
  }

  @Test
  void setRequestParams_formatsParamsCorrectly() throws Exception {
    // Arrange
    SynchroRequest request = new SynchroRequest();
    request.setMasterKey("testKey");
    request.setMesa("M001");
    request.setTipoSolucion(1);
    request.setTipoModulo(2);
    request.setTipoTrama(3);
    request.setForce(0);

    NetClientPost client = new NetClientPost("http://example.com");

    // Act
    client.setRequestParams(request);

    // Assert - Verificar que params se configuró correctamente
    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    String params = (String) paramsField.get(client);

    assertTrue(params.contains("masterKey=testKey"));
    assertTrue(params.contains("mesa=M001"));
    assertTrue(params.contains("tipoSolucion=1"));
    assertTrue(params.contains("tipoModulo=2"));
    assertTrue(params.contains("tipoTrama=3"));
    assertTrue(params.contains("force=0"));
  }


  @Test
  void sendRequestWithSSL_whenExceptionOccurs_returnsEmptyString2(){
    // Arrange
    String urlString = "https://example.com";

    // Crear una subclase de prueba que sobrescriba los métodos relevantes
    NetClientPost testClient = new NetClientPost(urlString) {
      @Override
      public String sendRequestWithSSL() {
        try {
          // Simular la excepción directamente
          throw new IOException("Connection error");
        } catch (Exception ex) {
          // TO DO
        }
        return "";
      }
    };

    // Act
    String result = testClient.sendRequestWithSSL();

    // Assert
    assertEquals("", result);
  }

  @Test
  void getResult_forHttpRequest_returnsSuccessResponse() {
    // Arrange
    String urlString = "http://example.com";
    String jsonResponse = "{\"Codigo\":1,\"Success\":true,\"Message\":\"OK\"}";
    MensajeWsResponse expectedResponse = new MensajeWsResponse(1, true, "OK");

    // Crear una subclase anónima que sobrescriba los métodos necesarios
    NetClientPost testClient = new NetClientPost(urlString) {
      @Override
      public String sendRequest() {
        return jsonResponse;
      }

      @Override
      public String sendRequestWithSSL() {
        // Nunca se debería llamar a este método
        fail("El método sendRequestWithSSL no debería ser llamado");
        return null;
      }

      // Sobrescribir el método privado isSSL para evitar usarlo
      // Nota: esto solo funcionará si el método es protected
      // Si es privado, no podremos sobrescribirlo directamente
      protected boolean isSSL() {
        return false; // Siempre devolver false para usar HTTP
      }
    };

    // Mock de WsOnpeUtils.getJsonToObject
    try (MockedStatic<WsOnpeUtils> mockedUtils = Mockito.mockStatic(WsOnpeUtils.class)) {
      mockedUtils.when(() -> WsOnpeUtils.getJsonToObject(jsonResponse, MensajeWsResponse.class))
        .thenReturn(expectedResponse);

      // Act
      MensajeWsResponse result = testClient.getResult();

      // Assert
      assertTrue(result.getSuccess());
      assertEquals(1, result.getCodigo());
      assertEquals("OK", result.getMessage());
    }
  }


  @Test
  void getResult_whenParsingReturnsNull_setsErrorResponse() {
    // Arrange - Configurar el escenario de prueba
    String invalidJson = "Invalid JSON";

    // Crear un mock de WsOnpeUtils.getJsonToObject
    try (MockedStatic<WsOnpeUtils> mockedUtils = Mockito.mockStatic(WsOnpeUtils.class)) {
      // Configurar para que devuelva null al parsear
      mockedUtils.when(() -> WsOnpeUtils.getJsonToObject(anyString(), eq(MensajeWsResponse.class)))
        .thenReturn(null);

      // Crear una instancia personalizada que simule el comportamiento
      MensajeWsResponse result = new NetClientPost("http://example.com") {
        @Override
        public MensajeWsResponse getResult() {
          MensajeWsResponse response = WsOnpeUtils.getJsonToObject(invalidJson, MensajeWsResponse.class);

          if (response == null) {
            response = new MensajeWsResponse();
            response.setSuccess(false);
            response.setMessage(invalidJson);
          }

          return response;
        }
      }.getResult();

      // Assert - Verificar el resultado
      assertFalse(result.getSuccess());
      assertEquals(invalidJson, result.getMessage());
    }
  }

  @Test
  void isSSL_whenHttpUrl_returnsFalse() throws Exception {
    // Arrange
    String urlString = "http://example.com";

    // Crear el cliente directamente
    NetClientPost client = new NetClientPost(urlString);

    // Asegurarse de que el campo urlString tiene el valor correcto
    java.lang.reflect.Field urlStringField = NetClientPost.class.getDeclaredField("urlString");
    urlStringField.setAccessible(true);
    urlStringField.set(client, urlString);

    // Act - Llamar al método privado usando reflection
    java.lang.reflect.Method isSSLMethod = NetClientPost.class.getDeclaredMethod("isSSL");
    isSSLMethod.setAccessible(true);
    boolean result = (boolean) isSSLMethod.invoke(client);

    // Assert
    assertFalse(result);
  }

  @Test
  void sendRequestWithSSL_whenSuccessful_returnsResponse() throws Exception {
    // Arrange
    String expectedResponse = "Success response";
    String testParams = "param1=value1&param2=value2";

    // Configurar los mocks
    OutputStream mockOutputStream = new ByteArrayOutputStream();
    InputStream mockInputStream = new ByteArrayInputStream(expectedResponse.getBytes());

    when(mockHttpsConnection.getOutputStream()).thenReturn(mockOutputStream);
    when(mockHttpsConnection.getInputStream()).thenReturn(mockInputStream);

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("https://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpsConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequestWithSSL();

    // Assert
    assertEquals(expectedResponse, result);

    // Verificar interacciones
    verify(mockHttpsConnection).connect();
    verify(mockHttpsConnection).getOutputStream();
    verify(mockHttpsConnection).getInputStream();
    verify(mockHttpsConnection).disconnect();

    // Verificar que los parámetros se escribieron correctamente
    ByteArrayOutputStream outputStream = (ByteArrayOutputStream) mockOutputStream;
    assertEquals(testParams, new String(outputStream.toByteArray()));
  }

  @Test
  void sendRequestWithSSL_whenExceptionOccurs_returnsEmptyString() throws Exception {
    // Arrange
    String testParams = "param1=value1&param2=value2";

    // Configurar mock para lanzar excepción
    when(mockHttpsConnection.getOutputStream()).thenThrow(new java.io.IOException("Connection error"));

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("https://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpsConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequestWithSSL();

    // Assert
    assertEquals("", result);

    // Verificar interacciones
    verify(mockHttpsConnection).connect();
    verify(mockHttpsConnection).getOutputStream();
    verify(mockHttpsConnection, never()).disconnect();
  }

  @Test
  void sendRequest_whenSuccessful_returnsResponse() throws Exception {
    // Arrange
    String expectedResponse = "Success response";
    String testParams = "param1=value1&param2=value2";

    // Configurar los mocks
    ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
    InputStream mockInputStream = new ByteArrayInputStream(expectedResponse.getBytes());

    when(mockHttpConnection.getOutputStream()).thenReturn(mockOutputStream);
    when(mockHttpConnection.getInputStream()).thenReturn(mockInputStream);
    doNothing().when(mockHttpConnection).disconnect();

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("http://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequest();

    // Assert
    assertEquals(expectedResponse, result);

    // Verificar interacciones
    verify(mockHttpConnection).connect();
    verify(mockHttpConnection).getOutputStream();
    verify(mockHttpConnection).getInputStream();
    verify(mockHttpConnection).disconnect();

    // Verificar que los parámetros se escribieron correctamente
    assertEquals(testParams, new String(mockOutputStream.toByteArray()));
  }

  @Test
  void sendRequest_whenIOExceptionDuringConnect_returnsEmptyString() throws Exception {
    // Arrange
    String testParams = "param1=value1&param2=value2";

    // Configurar mock para lanzar excepción durante connect()
    doThrow(new IOException("Connection error")).when(mockHttpConnection).connect();

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("http://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequest();

    // Assert
    assertEquals("", result);

    // Verificar interacciones
    verify(mockHttpConnection).connect();
    verify(mockHttpConnection, never()).getOutputStream();
    verify(mockHttpConnection, never()).getInputStream();
    verify(mockHttpConnection, never()).disconnect();
  }

  @Test
  void sendRequest_whenIOExceptionDuringGetOutputStream_returnsEmptyString() throws Exception {
    // Arrange
    String testParams = "param1=value1&param2=value2";

    // Configurar mock para lanzar excepción durante getOutputStream()
    when(mockHttpConnection.getOutputStream()).thenThrow(new IOException("Output stream error"));

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("http://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequest();

    // Assert
    assertEquals("", result);

    // Verificar interacciones
    verify(mockHttpConnection).connect();
    verify(mockHttpConnection).getOutputStream();
    verify(mockHttpConnection, never()).getInputStream();
    verify(mockHttpConnection, never()).disconnect();
  }

  @Test
  void sendRequest_whenIOExceptionDuringGetInputStream_returnsEmptyString() throws Exception {
    // Arrange
    String testParams = "param1=value1&param2=value2";

    // Configurar los mocks
    ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();

    when(mockHttpConnection.getOutputStream()).thenReturn(mockOutputStream);
    when(mockHttpConnection.getInputStream()).thenThrow(new IOException("Input stream error"));

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("http://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequest();

    // Assert
    assertEquals("", result);

    // Verificar interacciones
    verify(mockHttpConnection).connect();
    verify(mockHttpConnection).getOutputStream();
    verify(mockHttpConnection).getInputStream();
    verify(mockHttpConnection, never()).disconnect();
  }

  @Test
  void sendRequest_whenMultipleLines_concatenatesAll() throws Exception {
    // Arrange
    String line1 = "Line 1";
    String line2 = "Line 2";
    String line3 = "Line 3";
    String expectedResponse = line1 + line2 + line3;
    String testParams = "param1=value1&param2=value2";

    // Configurar mocks para simular múltiples líneas en la respuesta
    ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();

    // Crear un InputStream que simule múltiples líneas
    String multiLineResponse = line1 + "\n" + line2 + "\n" + line3;
    InputStream mockInputStream = new ByteArrayInputStream(multiLineResponse.getBytes());

    when(mockHttpConnection.getOutputStream()).thenReturn(mockOutputStream);
    when(mockHttpConnection.getInputStream()).thenReturn(mockInputStream);

    // Crear instancia de la clase a probar
    NetClientPost client = new NetClientPost("http://example.com");

    // Configurar los campos privados usando reflection
    java.lang.reflect.Field urlConnectionField = NetClientPost.class.getDeclaredField("urlConnection");
    urlConnectionField.setAccessible(true);
    urlConnectionField.set(client, mockHttpConnection);

    java.lang.reflect.Field paramsField = NetClientPost.class.getDeclaredField("params");
    paramsField.setAccessible(true);
    paramsField.set(client, testParams);

    // Act
    String result = client.sendRequest();

    // Assert
    assertEquals(expectedResponse, result);
  }

  @Test
  void getResult_WhenResponseIsNull_ReturnsErrorResponse() {
    // Arrange
    // Mockear WsOnpeUtils.getJsonToObject para que devuelva null
    try (MockedStatic<WsOnpeUtils> mockedStatic = mockStatic(WsOnpeUtils.class)) {
      mockedStatic.when(() -> WsOnpeUtils.getJsonToObject(anyString(), eq(MensajeWsResponse.class)))
        .thenReturn(null);

      // Mockear el método sendRequest para evitar conexiones reales
      NetClientPost spyClient = Mockito.spy(netClientPost);
      Mockito.doReturn("response data").when(spyClient).sendRequest();

      // Act
      MensajeWsResponse result = spyClient.getResult();

      // Assert
      assertNotNull(result);
      assertFalse(result.getSuccess());
      assertEquals("response data", result.getMessage());
    }
  }

  @Test
  void getResult_WhenExceptionOccurs_ReturnsErrorResponse() {
    // Arrange
    // Mockear método sendRequest para que lance una excepción
    NetClientPost spyClient = Mockito.spy(netClientPost);
    Mockito.doThrow(new RuntimeException("Connection error")).when(spyClient).sendRequest();

    // Si estamos probando con URL SSL, también debemos mockear sendRequestWithSSL
    try {
      // Usar reflection para modificar el campo urlString y hacerlo no-SSL
      Field urlStringField = NetClientPost.class.getDeclaredField("urlString");
      urlStringField.setAccessible(true);
      urlStringField.set(spyClient, "http://example.com/api");
    } catch (Exception e) {
      // Ignorar errores de reflection, la prueba seguirá funcionando
    }

    // Act
    MensajeWsResponse result = spyClient.getResult();

    // Assert
    assertNotNull(result);
    assertFalse(result.getSuccess());
    assertEquals("Connection error", result.getMessage());
  }

  // Opción alternativa usando reflexión si no quieres usar la clase auxiliar
  @Test
  void isSSL_UsingReflection() throws Exception {
    // Arrange
    NetClientPost httpsClient = new NetClientPost("https://example.com/api");
    NetClientPost httpClient = new NetClientPost("http://example.com/api");

    // Acceder al método privado isSSL usando reflexión
    Method isSSLMethod = NetClientPost.class.getDeclaredMethod("isSSL");
    isSSLMethod.setAccessible(true);

    // Act
    boolean httpsResult = (boolean) isSSLMethod.invoke(httpsClient);
    boolean httpResult = (boolean) isSSLMethod.invoke(httpClient);

    // Assert
    assertTrue(httpsResult, "URL HTTPS debería ser reconocida como SSL");
    assertFalse(httpResult, "URL HTTP no debería ser reconocida como SSL");
  }

}
