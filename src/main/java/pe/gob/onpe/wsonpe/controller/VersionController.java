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
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;
import pe.gob.onpe.wsonpe.enums.AccionesVersionEnum;
import pe.gob.onpe.wsonpe.service.IVersionService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

@RestController
@RequestMapping("/version")
@Slf4j
public class VersionController {

  private IVersionService versionService;

  public VersionController(IVersionService versionService) {
    this.versionService = versionService;
  }


  @Value("${stae.trans.masterkey}")
  private String masterKey;

  @PostMapping(consumes = {"application/x-www-form-urlencoded;charset=UTF-8",
    MediaType.APPLICATION_JSON_VALUE})
  public MensajeWsResponse userLogin(
    @RequestParam(value = "a") String accion,
    @RequestParam(value = "d") String llaveUsuario) {

    String requestUser = WsOnpeUtils.getRequestUser();
    log.info("RequestUser {}", requestUser);

    UserRequest request = new UserRequest(accion, requestUser, llaveUsuario);

    if (!request.getLlaveUsuario().equals(masterKey)) {
      return new MensajeWsResponse(2, false, "Key de acceso incorrecta");
    }

    if (request.getAccion().equals(AccionesVersionEnum.OBTENER_VERSION.getCode())) {
      return versionService.verificarVersion(request);
    } else {
      return new MensajeWsResponse(0, false, "Codigo de accion no encontrado.");
    }

  }

  @GetMapping()
  public RedirectView getMethod() {
    return new RedirectView("/");
  }

}
