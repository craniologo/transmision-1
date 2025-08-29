package pe.gob.onpe.wsonpe.versionservice;


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
import pe.gob.onpe.wsonpe.dao.impl.TabProcesoDaoImpl;
import pe.gob.onpe.wsonpe.dao.impl.TabVersionDaoImpl;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;
import pe.gob.onpe.wsonpe.service.VersionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class VersionServiceTests {

  @Mock
  private TabProcesoDaoImpl mockTabProcesoDao;

  @Mock
  private TabVersionDaoImpl mockTabVersionDao;

  @InjectMocks
  private VersionService versionService;

  private static UserRequest request;
  private static MensajeWsResponse goodResponse;
  private static MensajeWsResponse goodResponseFechaActual;
  private static MensajeWsResponse goodResponseFechaActual10;
  private static MensajeWsResponse goodResponseFechaActualMinusOneDay;
  private static String stringFechaActual;
  private static String stringFechaActualMinusOneDay;

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

    request = new UserRequest();
    request.setAccion("vV");
    request.setUsuario("usuarioTest");
    request.setLlaveUsuario("llave");

    goodResponse = new MensajeWsResponse();
    goodResponse.setSuccess(true);
    goodResponse.setMessage("5f03f8700ab2f1f64e4edf9d23841dfa:JORNADA ELECTORAL:10/10/21:1:0");
    goodResponse.setCodigo(1);

    LocalDate localDate = LocalDate.now();
    stringFechaActual = localDate
      .format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    goodResponseFechaActual = new MensajeWsResponse();
    goodResponseFechaActual.setSuccess(true);
    goodResponseFechaActual.setMessage("5f03f8700ab2f1f64e4edf9d23841dfa:JORNADA ELECTORAL:"+stringFechaActual+":1:1");
    goodResponseFechaActual.setCodigo(1);

    goodResponseFechaActual10 = new MensajeWsResponse();
    goodResponseFechaActual10.setSuccess(true);
    goodResponseFechaActual10.setMessage("5f03f8700ab2f1f64e4edf9d23841dfa:JORNADA ELECTORAL:"+stringFechaActual+":1:0");
    goodResponseFechaActual10.setCodigo(1);

    LocalDate localDateMinusOne = LocalDate.now().minusDays(1);
    stringFechaActualMinusOneDay = localDateMinusOne
      .format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    goodResponseFechaActualMinusOneDay = new MensajeWsResponse();
    goodResponseFechaActualMinusOneDay.setSuccess(true);
    goodResponseFechaActualMinusOneDay.setMessage("5f03f8700ab2f1f64e4edf9d23841dfa:JORNADA ELECTORAL:"+stringFechaActualMinusOneDay+":1:0");
    goodResponseFechaActualMinusOneDay.setCodigo(1);

  }
  @DisplayName("Verificar version action NO OK - Verificar version falla")
  @Test
  void verificarVersionNotOk_VerificarVersionFail() {

    String fecha = "10/10/21";
    MensajeWsResponse response = new MensajeWsResponse(2, false,"");

    Mockito.when(mockTabProcesoDao.obtenerFechaProceso()).thenReturn(fecha);
    Mockito.when(mockTabVersionDao.verificarVersion(fecha)).thenReturn(response);

    Assertions.assertNull(versionService.verificarVersion(request));
  }
}
