package pe.gob.onpe.wsonpe.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.dao.TabProcesoDao;
import pe.gob.onpe.wsonpe.dao.TabVersionDao;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;
import pe.gob.onpe.wsonpe.model.TabUsuario;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class UserService implements IUserService {

  private TabUsuarioRepository usuarioRepository;

  private static final String PO_RESULTADO = "PO_RESULTADO";
  private static final String PO_MENSAJE = "PO_MENSAJE";
  private static final String LOG_CALLING_PROCEDURE = "Calling stored procedure.";
  private final TabMesaTransmitidaRepository tabMesaTransmitidaRepository;
  private final TabProcesoDao tabProcesoDao;
  private final TabVersionDao tabVersionDao;
  private final TabTareasMesaRepository tabTareasMesaRepository;

  @Override
  public MensajeWsResponse updateEstado(UserRequest request) {

    log.info("Acción update estado.");
    log.info("Llamando stored procedure.");

    Map<String, Object> actualizaEstadoResponse = usuarioRepository.spActualizaEstado(request.getUsuario(), 1);

    Integer codigo = (Integer) actualizaEstadoResponse.get(PO_RESULTADO);
    boolean success = codigo == 1;
    String descripcion = (String) actualizaEstadoResponse.get(PO_MENSAJE);

		if (!success) {
			log.error("Usuario: {}, Accion: {}, Logout Usuario Error {}", request.getUsuario(), request.getAccion(),
					descripcion);
			return new MensajeWsResponse(codigo, false, descripcion);
		}

		return new MensajeWsResponse(codigo, true, descripcion);

	}

	@Override
	public MensajeWsResponse verificarPuestaCero(UserRequest request) {
		log.info("Acción verificar puesta cero.");
		log.info("Llamando stored procedure.");

		String fecha = tabProcesoDao.obtenerFechaProceso();
		log.info("{}, {}, Fecha proceso: {}", request.getUsuario(), request.getAccion(), fecha);

    MensajeWsResponse versionResponse = tabVersionDao.verificarVersion(fecha);
		log.info("Verificar versión response : {}", versionResponse);

    TabUsuario usuario = usuarioRepository.findByUsuario(request.getUsuario());
    int codigo;
    String mensaje;
    boolean success = usuario != null;

		if (success) {
      Short flagPuestaCero = usuario.getNFlagPuestaCero();
      codigo = 1;
      mensaje = flagPuestaCero != null ? String.valueOf(flagPuestaCero) : "";
			log.info("Usuario: {}, Acción: {}, Puesta Cero Realizada Satisfactoriamente.", request.getUsuario(),
					request.getAccion());
		} else {
      codigo = 2;
      mensaje = "Ocurrió un error durante la verificación de puesta a cero";
			log.error("Usuario: {}, Acción: {}, Ocurrió un error al ejecutar la puesta cero.", request.getUsuario(),
					request.getAccion());
		}

		return new MensajeWsResponse(codigo, success, mensaje);
	}

	@Override
	public MensajeWsResponse verificarPuestaCeroDiagnostico(UserRequest request) {

    TabUsuario usuario = usuarioRepository.findByUsuario(request.getUsuario());
    int codigo = 2;
    String mensaje = "Ocurrió un error durante la verificación de puesta a cero";
    boolean success = usuario != null;

    if (success) {
      codigo = 1;
      Short flagPuestaCero = usuario.getNFlagPuestaCero();
      mensaje = flagPuestaCero != null ? String.valueOf(flagPuestaCero) : "";
    }

		log.info("Usuario: {}, Acción: {}, Verificación de puesta cero {}", request.getUsuario(), request.getAccion(),
      mensaje);

		return new MensajeWsResponse(codigo, success, mensaje);
	}

	@Override
	public MensajeWsResponse verificarPuestaCeroYTransmitido(UserRequest request) {

		Integer tipoSolucion = Integer.valueOf(request.getTipoSolucion());
		Integer usbModulo = request.getUsbModulo().equals(StringUtils.EMPTY) ? 0
				: Integer.parseInt(request.getUsbModulo());
		Integer usbParte = request.getUsbParte().equals(StringUtils.EMPTY) ? 0
      : Integer.parseInt(request.getUsbParte());
		Integer paginaNro = request.getPaginaNro().equals(StringUtils.EMPTY) ? 0
				: Integer.parseInt(request.getPaginaNro());

		Map<String, Object> spVerificaPuestaCeroTransMap = usuarioRepository.spVerificaPuestaCeroTrans(
				request.getUsuario(), request.getMesa(), tipoSolucion, usbParte, usbModulo, paginaNro);

		Integer codigo = (Integer) spVerificaPuestaCeroTransMap.get(PO_RESULTADO);
		Boolean success = codigo == 1;
		String descripcion = (String) spVerificaPuestaCeroTransMap.get(PO_MENSAJE);

		log.info("Usuario: {}, Accion: {}, Verificación puesta cero y transmitido: {}", request.getUsuario(),
				request.getAccion(), descripcion);

		return new MensajeWsResponse(codigo, success, descripcion);
	}

	@Override
	public MensajeWsResponse verificarPuestaCeroYTransmitido2(UserRequest request) {

		Integer tipoSolucion = Integer.valueOf(request.getTipoSolucion());
		Integer usbModulo = Integer.valueOf(request.getUsbModulo());

		Map<String, Object> spVerificaPuestaCeroTransDMap = usuarioRepository.spVerificaPuestaCeroTransD(
				request.getUsuario(), request.getMesa(), tipoSolucion, request.getUsbParte(), usbModulo);

		Integer codigo = (Integer) spVerificaPuestaCeroTransDMap.get(PO_RESULTADO);
		Boolean success = codigo == 1;
		String descripcion = (String) spVerificaPuestaCeroTransDMap.get(PO_MENSAJE);

		log.info("Usuario: {}, Accion: {}, Verificación puesta cero y transmitido: {}", request.getUsuario(),
				request.getAccion(), descripcion);

		return new MensajeWsResponse(codigo, success, descripcion);
	}

	@Override
	public MensajeWsResponse ejecutarPuestaCero(UserRequest request) {

		log.info("Ejecutar Puesta Cero.");
		log.info(LOG_CALLING_PROCEDURE);

    int codigo = 1;
    String descripcion = "Puesta Cero Satisfactoria";

    // reemplaza SP_PUESTA_CERO
    boolean respPuestaCero = puestaCero(request.getUsuario());

		if (respPuestaCero) {
			log.info("Usuario: {}, Acción: {}, Ejecución de puesta a cero satisfactoria.",
        request.getUsuario(), request.getAccion());
		} else {
      descripcion = "Error en ejecutar la Puesta Cero";
      codigo = 2;
			log.error("Usuario: {}, Acción: {}, Ejecución de puesta a cero fallida: {}",
        request.getUsuario(), request.getAccion(), descripcion);
		}

		return new MensajeWsResponse(codigo, respPuestaCero, descripcion);
	}


	@Override
	public MensajeWsResponse ejecutarPuestaCeroD(UserRequest request) {

		log.info("Ejecutar Puesta Cero Diagnostico.");
		log.info(LOG_CALLING_PROCEDURE);
		Map<String, Object> ejecutarPuestaCeroDMap = usuarioRepository.spEjecutarPuestaCeroD(request.getUsuario());

		Integer codigo = (Integer) ejecutarPuestaCeroDMap.get(PO_RESULTADO);
		boolean success = codigo == 1;
		String descripcion = (String) ejecutarPuestaCeroDMap.get(PO_MENSAJE);

		if (success) {
			log.info("Usuario: {}, Accion: {}, Ejecución de puesta a cero Satifactoria.", request.getUsuario(),
					request.getAccion());
		} else {
			log.error("Usuario: {}, Accion: {}, Ejecucion de puesta a cero Fallida: {}.", request.getUsuario(),
					request.getAccion(), descripcion);
		}

		return new MensajeWsResponse(codigo, success, descripcion);

	}

	@Override
	public MensajeWsResponse verificarTimeSufEs(UserRequest request) {

		log.info("Verificar Hora de Ingreso Escrutinio Mayor a las 4:00pm Usuario.");

		Integer codigo = 1;
		boolean success;
		String descripcion = "";

		LocalDateTime locaDate = LocalDateTime.now();
		int hora = locaDate.getHour();

    success = hora >= 4;

		if (success) {
			log.info("Usuario: {}, Acción: {}, Ingreso de Esrutino Valido mayor igual que las 4pm.",
					request.getUsuario(), request.getAccion());
		} else {
			log.error("Usuario: {}, Acción: {}, Ingreso de Esrutino Valido mayor igual que las 4pm.",
					request.getUsuario(), request.getAccion());
		}

		return new MensajeWsResponse(codigo, success, descripcion);

	}

	@Override
	public MensajeWsResponse verificarSessionActiva(UserRequest request) {

		log.info("Verificar Sesión Activa del Usuario.");
		log.info(LOG_CALLING_PROCEDURE);
		Map<String, Object> validaEstadoMap = usuarioRepository.spValidaEstado(request.getUsuario());

		Integer codigo = (Integer) validaEstadoMap.get(PO_RESULTADO);
		boolean success = codigo == 1;
		String descripcion = (String) validaEstadoMap.get(PO_MENSAJE);

		if (success) {
			log.info("Usuario: {}, Acción: {}, Usuario Válido.", request.getUsuario(), request.getAccion());
		} else {
			log.error("Usuario: {}, Acción: {}, El usuario no está conectado.", request.getUsuario(),
					request.getAccion());
		}

		return new MensajeWsResponse(codigo, success, descripcion);

	}

  private boolean puestaCero(String usuario){
    boolean response = true;

    try {
      TabUsuario tabUsuario = usuarioRepository.findByUsuario(usuario);
      tabMesaTransmitidaRepository.eliminarIntegracionPorUsuario(tabUsuario.getNUsuarioPk());
      tabMesaTransmitidaRepository.eliminarSincronizacionPorUsuario(tabUsuario.getNUsuarioPk());
      tabMesaTransmitidaRepository.eliminarMesasTransmitidasPorUsuario(tabUsuario.getNUsuarioPk());
      tabTareasMesaRepository.eliminarPorUsuario(usuario);
      tabUsuario.setNFlagPuestaCero((short) 1);
      usuarioRepository.saveAndFlush(tabUsuario);
    } catch (Exception e) {
      log.warn("Error en la puesta a cero del usuario {} - {}", usuario, e.getMessage());
      response = false;
    }

    return response;
  }

}
