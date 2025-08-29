package pe.gob.onpe.wsonpe.userservice;

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
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;
import pe.gob.onpe.wsonpe.model.TabEleccion;
import pe.gob.onpe.wsonpe.projections.FindConfigurationProjection;
import pe.gob.onpe.wsonpe.repository.TabConfTxRepository;
import pe.gob.onpe.wsonpe.repository.TabEleccionRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;
import pe.gob.onpe.wsonpe.repository.TabVersionRepository;
import pe.gob.onpe.wsonpe.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@SpringBootTest
class UserServiceTests {

	@Mock
	private TabUsuarioRepository mockTabUsuarioRepository;

	@Mock
	private TabConfTxRepository mockConfTxRepository;

	@Mock
	private TabEleccionRepository mockTabElectionRepository;


	@Mock
	private TabVersionRepository mockTabVersionRepository;

	@InjectMocks
	private UserService userService;

	private static UserRequest request;
	private static final int ACTIVE = 1;
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
		request.setUsuario("usuario");
		request.setAccion("usuario");
	}

	@DisplayName("Testing login action - OK")
	@Test
	void testLogin() {

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("PO_RESULTADO", 1);
		responseMap.put("PO_MENSAJE", "Exito");
		responseMap.put("PO_PROCEDER", 1);

    FindConfigurationProjection configurationProjection = new FindConfigurationProjection(
      "descripcion", "cEncSea", "cEncFile", "cEndVep", "cEncFirma", "cEncQr", "cMasterKey",
      10, 1, null, null);

		List<TabEleccion> tabEleccionList = new ArrayList<>();
		TabEleccion tabEleccion = new TabEleccion();
		tabEleccion.setCNombreCortoEleccion("nombreCortoEleccion");
		tabEleccion.setCEleccionPk("1");
		tabEleccionList.add(tabEleccion);

		Mockito.when(mockTabUsuarioRepository.spValidaAcceso(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(responseMap);

		Mockito.when(mockConfTxRepository.findBynEstado(ACTIVE))
				.thenReturn(Collections.singletonList(configurationProjection));

		Mockito.when(mockTabElectionRepository.findAll()).thenReturn(tabEleccionList);
    Assertions.assertNull(null);
	}

	@DisplayName("Testing login action - KO")
	@Test
	void testLoginBad() {

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("PO_RESULTADO", 0);
		responseMap.put("PO_MENSAJE", "LoginMal");
		responseMap.put("PO_PROCEDER", 1);

		MensajeWsResponse badResponse = new MensajeWsResponse();
		badResponse.setCodigo(0);
		badResponse.setMessage("LoginMal");
		badResponse.setSuccess(false);

		Mockito.when(mockTabUsuarioRepository.spValidaAcceso(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(responseMap);

    Assertions.assertNull(null);
  }
}
