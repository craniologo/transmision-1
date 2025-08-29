package pe.gob.onpe.wsonpe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.constants.CryptoValues;
import pe.gob.onpe.wsonpe.dto.LoginRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserResponse;

import pe.gob.onpe.wsonpe.model.TabEleccion;
import pe.gob.onpe.wsonpe.projections.FindConfigurationProjection;
import pe.gob.onpe.wsonpe.repository.TabConfTxRepository;
import pe.gob.onpe.wsonpe.repository.TabEleccionRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;
import pe.gob.onpe.wsonpe.utils.Crypto;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoginService implements ILoginService {

	private final TabUsuarioRepository usuarioRepository;
	private final TabConfTxRepository confTxRepository;
	private final TabEleccionRepository eleccionRepository;

	private static final int ACTIVE = 1;

  @Value("${stae.trans.jwtLifetime}")
  private int jwtLifetime;

  @Value("${stae.trans.refreshJwtLifetime}")
  private int refreshJwtLifetime;


  public LoginService(TabUsuarioRepository usuarioRepository, TabConfTxRepository confTxRepository,
			TabEleccionRepository eleccionRepository) {
		this.usuarioRepository = usuarioRepository;
		this.confTxRepository = confTxRepository;
		this.eleccionRepository = eleccionRepository;
	}

	@Override
	public MensajeWsResponse login(LoginRequest request) {

		log.info("login: Acción de login");

		log.info("login: Llamando al SP");
		Map<String, Object> validaAccesoResponse = usuarioRepository.spValidaAcceso(request.getUsuario(),
				WsOnpeUtils.getSHA(request.getClave()));

		Integer codigo = (Integer) validaAccesoResponse.get("PO_RESULTADO");
		boolean success = codigo == 1;
		String descripcion = (String) validaAccesoResponse.get("PO_MENSAJE");

		if (!success) {
			log.error("login: {}, Error de inicio de sesión {}", request.getUsuario(), descripcion);
			return new MensajeWsResponse(codigo, false, descripcion);
		}

		String token;

		try {
			token = getJwtToken(request.getUsuario(), request.isRefreshToken());
		} catch (Exception e) {
			log.error("login: Error generando el token. {}", e.getMessage());
			return new MensajeWsResponse(2, false, "Error generando el token");
		}

		return dataAuthentication(request.getUsuario(), token, descripcion);
	}

	private MensajeWsResponse dataAuthentication(String usuario, String token, String usuarioPerfil) {

		List<FindConfigurationProjection> opConfTx = confTxRepository.findBynEstado(ACTIVE);
		List<TabEleccion> tabEleccionList = eleccionRepository.findAll();
		List<UserResponse.Elecciones> eleccionesList = new ArrayList<>();

		if (opConfTx == null || opConfTx.isEmpty()) {
			log.warn("dataAuthentication: No se encontraron registros de configuración");
			return new MensajeWsResponse(2, false, "No se encontraron registros de configuración");
		}

		FindConfigurationProjection confTx = opConfTx.getLast();

		if (!tabEleccionList.isEmpty()) {
			tabEleccionList.forEach(e -> eleccionesList
					.add(new UserResponse.Elecciones(e.getCEleccionPk(), e.getCNombreCortoEleccion())));
		}

		ObjectMapper objectMapper = new ObjectMapper();
		UserResponse userResponse = new UserResponse(confTx.getCDescripcion(), confTx.getCEncSea(),
				confTx.getCEncFile(), confTx.getNTxMesa(), confTx.getCEncVep(), confTx.getCEncFirma(),
				confTx.getCEncQr(), confTx.getChsqlUsr(), confTx.getChsqlPwd(), eleccionesList, usuarioPerfil, token);
		String data;

		try {
			data = objectMapper.writeValueAsString(userResponse);
		} catch (JsonProcessingException e) {
			log.error("dataAuthentication: Error parseando json respuesta.");
			log.error(ExceptionUtils.getMessage(e));
			return new MensajeWsResponse(0, false, "");
		}

		log.info("{}, Inicio de sesión exitosa", usuario);

		return new MensajeWsResponse(1, true, data);

	}

	private String getJwtToken(String username, boolean refreshToken) {
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

		long min = 60 * 1000L;

		long lifetime = refreshToken ? refreshJwtLifetime * min : jwtLifetime * min;

    Map<String, Object> claim = new HashMap<>();
    claim.put("authorities", grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
		String subject = Crypto.encryptStringAES(username);

    String token =  Jwts
                .builder()
                .setClaims(claim)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + lifetime))
                .signWith(SignatureAlgorithm.HS512, CryptoValues.getJwtKey().getBytes())
                .compact();

		return "Bearer " + token;
	}
}
