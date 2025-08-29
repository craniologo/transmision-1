package pe.gob.onpe.wsonpe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.SynchroRequest;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;
import pe.gob.onpe.wsonpe.utils.NetClientPost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@ExtendWith(MockitoExtension.class)
class AsyncSynchroServiceTest {

  @Mock
  private TabTareasMesaRepository tramaRepository;

  @InjectMocks
  private AsyncSynchroService asyncSynchroService;

  @Mock
  private NetClientPost mockNetClientPost;

  @BeforeEach
  void setUp() {
    // Configurar propiedades de valor mediante reflexión
    ReflectionTestUtils.setField(asyncSynchroService, "synchroUrl", "http://test-url.com/");
    ReflectionTestUtils.setField(asyncSynchroService, "synchroMasterKey", "testMasterKey");
  }

  @Test
  void doSynchro_WithNoFileInSynchroResponse_ShouldRetryWithForceFlag() {
    // Parámetros de entrada
    String mesa = "001";
    Integer tipoSolucion = 1;
    Integer tipoModulo = 2;
    Integer tipoTrama = 3;

    // Mock para permitir crear nuevas instancias controladas de NetClientPost
    NetClientPost mockClient = Mockito.mock(NetClientPost.class);

    // Crear una respuesta NO exitosa con código NO_FILE_IN_SYNCHRO
    MensajeWsResponse failResponse = new MensajeWsResponse();
    failResponse.setCodigo(WebService.NO_FILE_IN_SYNCHRO);
    failResponse.setSuccess(false);
    failResponse.setMessage("Error: No file in synchro");

    // Configurar la subclase para pruebas
    class TestableAsyncSynchroService extends AsyncSynchroService {
      private final NetClientPost mockNetClient;
      private SynchroRequest lastRequest;

      public TestableAsyncSynchroService(TabTareasMesaRepository repo, NetClientPost mockClient) {
        super(repo);
        this.mockNetClient = mockClient;
      }

      // Sobrescribir el método para crear un cliente - hacemos que devuelva nuestro mock
      @Override
      protected NetClientPost createNetClient(String url) {
        return mockNetClient;
      }

      // Método para verificar los parámetros de la solicitud
      public SynchroRequest getLastRequest() {
        return lastRequest;
      }
    }

    // Crear instancia de prueba
    TestableAsyncSynchroService testService = new TestableAsyncSynchroService(tramaRepository, mockClient);
    ReflectionTestUtils.setField(testService, "synchroUrl", "http://test-url.com/");
    ReflectionTestUtils.setField(testService, "synchroMasterKey", "testMasterKey");

    // Configurar el comportamiento del mock para que devuelva la respuesta de error
    Mockito.when(mockClient.getResult()).thenReturn(failResponse);

    // Capturar los parámetros de solicitud
    Mockito.doAnswer(invocation -> {
      SynchroRequest req = invocation.getArgument(0);
      testService.lastRequest = req;
      return null;
    }).when(mockClient).setRequestParams(Mockito.any(SynchroRequest.class));

    // Ejecutar el método bajo prueba
    testService.doSynchro(mesa, tipoSolucion, tipoModulo, tipoTrama);

    // Verificar que se llamó al método setRequestParams
    Mockito.verify(mockClient, Mockito.atLeastOnce()).setRequestParams(Mockito.any(SynchroRequest.class));

    // Verificar que el último force es 1
    assertEquals(1, testService.getLastRequest().getForce(),
      "La última solicitud debería tener force=1");
  }



}
