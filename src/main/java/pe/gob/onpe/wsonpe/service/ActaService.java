package pe.gob.onpe.wsonpe.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.dto.ActaRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.repository.TabUsuarioRepository;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;
import pe.gob.onpe.wsonpe.constants.WebService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Service
@Slf4j
public class ActaService implements IActaService {

  private static final String TRANSMISION = "transmisión";
	private static final int ERR_INVALID_RESULT = 2;
  private static final int ERR_TRAMA_TRANSMITIDA = WebService.TRAMA_TRANS;
	private static final int ERR_MESA_TRANSMITIDA = WebService.MESA_TRANS;
	private static final int VALID_RESULT = WebService.SUCCESS_RESULT;
	private TabUsuarioRepository usuarioRepository;
	private TabMesaTransmitidaRepository mesaTransmitidaRepository;
  private final ITransmisionService transmisionService;

	public ActaService(TabUsuarioRepository usuarioRepository,
                     TabMesaTransmitidaRepository mesaTransmitidaRepository,
                     ITransmisionService transmisionService) {
		this.usuarioRepository = usuarioRepository;
		this.mesaTransmitidaRepository = mesaTransmitidaRepository;
    this.transmisionService = transmisionService;
  }

	@Override
	public MensajeWsResponse receiveActa(ActaRequest request) {

		MensajeWsResponse response = new MensajeWsResponse(ERR_INVALID_RESULT, false, "");
		String username = request.getUsuario();

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, request.getMesa(), request.getTipoSolucion(), request.getTipoModulo(), request.getTipoTrama());

    int fechaTransmisionIni = WsOnpeUtils.getCurrentTimestampInteger();

		try {
			WsOnpeUtils.writeLog(username, TRANSMISION, "Preparándose a validar");
			response = validarTransmision(request);

			if (!response.getSuccess()) {
				WsOnpeUtils.writeLog(username, TRANSMISION, "Validación no válida para la transmisión");
				return response;
			}

			WsOnpeUtils.writeLog(username, TRANSMISION, "Validación de transmisión");
			log.info("{}. receiveActa: fechaTransmisionIni: {}", logHead, fechaTransmisionIni);
			response = insertarTransmision(request, fechaTransmisionIni);

		} catch (Exception e) {
			WsOnpeUtils.writeLog("catch", "", e.getMessage());
      log.error("{}. Error en receiveActa: {}", logHead, e.getMessage());
			response.setCodigo(ERR_INVALID_RESULT);
			response.setMessage("Error interno del servidor");
			response.setSuccess(false);
		}

		return response;
	}

	private MensajeWsResponse validarTransmision(ActaRequest actaRequest) {

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, actaRequest.getMesa(), actaRequest.getTipoSolucion(), actaRequest.getTipoModulo(), actaRequest.getTipoTrama());

    Map<String, Object> validaTransmisionResponse = usuarioRepository.spValidaTransmision(
      actaRequest.getMesa(), actaRequest.getTipoSolucion(),
				actaRequest.getTipoModulo(), actaRequest.getTipoTrama(), actaRequest.getPaginaNro()
    );

		int result = Integer.parseInt(validaTransmisionResponse.get("PO_RESULTADO").toString());
		boolean success = result == VALID_RESULT;
		String message = "";

		log.info("{}. Usuario: {}, Validar de Transmision: responseSP -> {}", logHead, actaRequest.getUsuario(),
				validaTransmisionResponse);

		if (result != ERR_MESA_TRANSMITIDA && result != ERR_TRAMA_TRANSMITIDA) {
			result = ERR_INVALID_RESULT;
		}
		if (!success) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			message = dateFormat.format(new Date());
		}
		return new MensajeWsResponse(result, success, message);
	}

	private MensajeWsResponse insertarTransmision(ActaRequest actaRequest, int fechaTransmisionIni) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String funcName = "insertarTransmision";

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, actaRequest.getMesa(), actaRequest.getTipoSolucion(), actaRequest.getTipoModulo(), actaRequest.getTipoTrama());

		int fechaTransmisionFin = WsOnpeUtils.getCurrentTimestampInteger();
		int result;
		boolean success;
		String description;
		String message = dateFormat.format(new Date());
		String username = actaRequest.getUsuario();

		log.info("{}. {}: mesa: {}", logHead, funcName, actaRequest);
		log.info("{}. {}: fechaTransmisionIni: {}", logHead, funcName, fechaTransmisionIni);
		log.info("{}. {}: fechaTransmisionFin: {}", logHead, funcName, fechaTransmisionFin);

		try {
			Map<String, Object> insertaTransmisionResponse = mesaTransmitidaRepository.spInsercionTransmision(
					actaRequest.getMesa(), actaRequest.getTipoSolucion(), actaRequest.getTipoModulo(),
					actaRequest.getTipoTrama(), actaRequest.getPaginaNro(), actaRequest.getUsuario(),
					actaRequest.getTrama(), actaRequest.getTrama2(), actaRequest.getFirma(), actaRequest.getMeta(),
					fechaTransmisionIni, fechaTransmisionFin, actaRequest.getPdfDigest());

			log.info("{}. Usuario: {}, Insertar Transmision: responseSP -> {}", logHead, actaRequest.getUsuario(),
					insertaTransmisionResponse);

			description = (String) insertaTransmisionResponse.get("PO_MENSAJE");
			result = Integer.parseInt(insertaTransmisionResponse.get("PO_RESULTADO").toString());
			success = result == VALID_RESULT;

			if (!success) {
				message = description;

        // trama insertada pero falta validar el archivo previamente transmitido
        if (result == WebService.VALIDATE_FILE) {

          MensajeWsResponse fileValidationResponse = this.retryValidateFile(
            actaRequest.getMesa(),
            actaRequest.getTipoSolucion(),
            actaRequest.getTipoModulo(),
            actaRequest.getTipoTrama(),
            actaRequest.getPaginaNro(),
            username,
            description
          );

          success = fileValidationResponse.getSuccess();
          result = fileValidationResponse.getCodigo();
          if (success) message = dateFormat.format(new Date());
          else message = fileValidationResponse.getMessage();

        } else if (result != ERR_MESA_TRANSMITIDA && result != ERR_TRAMA_TRANSMITIDA) {
					result = ERR_INVALID_RESULT;
					WsOnpeUtils.writeLog(username, TRANSMISION, "Error en la inserción a la BD");
				} else if (result == ERR_MESA_TRANSMITIDA) {
					WsOnpeUtils.writeLog(username, TRANSMISION, "Mesa ya transmitida");
				} else if (result == ERR_TRAMA_TRANSMITIDA) {
          WsOnpeUtils.writeLog(username, TRANSMISION, "Trama ya transmitida");
        }
			}

			WsOnpeUtils.writeLog("logTransmision",
					"trama: " + actaRequest.getTrama() + " *****|***** firma:" + actaRequest.getFirma(), "");
			WsOnpeUtils.writeLog(username, TRANSMISION, "Inserción a la BD válida");

		} catch (Exception exception) {
      log.error("{}. Error en {}: {} - {}", logHead, funcName, exception.getMessage(), exception.getStackTrace());
			success = false;
			message = "Error en la inserción";
			result = ERR_INVALID_RESULT;
			WsOnpeUtils.writeLog("catch", TRANSMISION, exception.getMessage());
		}

		return new MensajeWsResponse(result, success, message);
	}

  public MensajeWsResponse retryValidateFile(
    String mesa, int tipoSolucion, int tipoModulo, int tipoTrama, int idFlujo, String username, String fileName) {
    String funcName = "retryValidateFile";

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, tipoModulo, tipoTrama);


    MensajeWsResponse res = this.transmisionService.validateAndConfirmFile(mesa,
      tipoSolucion,
      tipoModulo, tipoTrama, idFlujo, username, fileName);

    log.info("{}. {}: Respuesta del intento de validacion del archivo: {}", logHead, funcName, res);

    if (res.getSuccess()) {
      res.setCodigo(WebService.SUCCESS_RESULT);
      res.setMessage("Datos enviados satisfactoriamente");
    } else {
      res.setCodigo(WebService.VALIDATION_FILE_FAILED);
    }

    return res;

  }

}
