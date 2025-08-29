package pe.gob.onpe.wsonpe.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import lombok.extern.slf4j.Slf4j;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.ITramaService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

@RestController
@RequestMapping("/trama")
@Slf4j
public class TramaController {

  private ITramaService tramaService;

	@Value("${stae.trans.masterkey}")
	private String masterKey;

  public TramaController(ITramaService tramaService) {
    this.tramaService = tramaService;
  }

	@PostMapping(consumes = { "application/x-www-form-urlencoded;charset=UTF-8", MediaType.APPLICATION_JSON_VALUE })
	public MensajeWsResponse receiveTrama(@RequestParam(value = "k") String trama) {

    String requestUser = WsOnpeUtils.getRequestUser();

		log.info("ReceiveTrama: usuario: {}", requestUser);
		log.info("ReceiveTrama: trama: {}", trama);

    MensajeWsResponse res =  this.tramaService.receiveTrama(requestUser,trama);

    log.info("Response de ReceiveTrama: {} por {}", res, requestUser);
    return res;
	}

	@GetMapping()
	public RedirectView getMethod() {
		return new RedirectView("/");
	}

}
