package pe.gob.onpe.wsonpe.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.ActaRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.IActaService;
import pe.gob.onpe.wsonpe.service.IAsyncSynchroService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

@RestController
@RequestMapping("/acta")
@Slf4j
public class ActaController {

	private final IActaService actaService;
	private final IAsyncSynchroService synchroService;


	@Value("${stae.synchro.flag}")
	private int synchroFlag;

  @Value("${stae.trans.masterkey}")
  private String masterKey;



	public ActaController(IActaService actaService, IAsyncSynchroService synchroService) {
		this.actaService = actaService;
    this.synchroService = synchroService;
	}

	@PostMapping(consumes = { "application/x-www-form-urlencoded;charset=UTF-8", MediaType.APPLICATION_JSON_VALUE })
	public MensajeWsResponse receiveActa(
			@RequestParam(value = "d") String key, @RequestParam(value = "f") String mesa,
			@RequestParam(value = "k") String trama, @RequestParam(value = "m") String firma,
			@RequestParam(value = "l") String meta, @RequestParam(value = "g") Integer tipoSolucion,
			@RequestParam(value = "h") Integer tipoModulo, @RequestParam(value = "i") Integer tipoTrama,
			@RequestParam(value = "j") Integer npaginaNro, @RequestParam(value = "n") String pdfDigest) {

    String requestUser = WsOnpeUtils.getRequestUser();

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, tipoModulo, tipoTrama);

		ActaRequest request = new ActaRequest(requestUser, key, mesa, trama, "",
        firma, meta, tipoSolucion, tipoModulo, tipoTrama, npaginaNro, pdfDigest);

    log.info("{}. ReceiveActa: request: {}", logHead, request);

		if (request.getKey() == null || !request.getKey().equals(masterKey)) {
			return new MensajeWsResponse(2, false, "Key de acceso incorrecta");
		}

    MensajeWsResponse response = this.actaService.receiveActa(request);
    log.info("{}. ActaController response: {}", logHead, response);

    boolean proceedSynchro = false;

    if (synchroFlag == 1 && tipoModulo != WebService.ENCRYPTED_FILE) {
      proceedSynchro = response.getSuccess();

      if (!proceedSynchro && response.getCodigo() == WebService.VALIDATION_FILE_FAILED) {
        proceedSynchro = true;
      }
    }

    if (proceedSynchro) {
      log.info("{}. Informacion de la trama de la mesa {} fue transmitida correctamente, invocando la sincronizacion", logHead, mesa);
      this.synchroService.doSynchro(mesa, tipoSolucion, tipoModulo, tipoTrama);
    }

		return response;
	}

	@GetMapping()
	public RedirectView getMethod() {
		return new RedirectView("/");
	}

}
