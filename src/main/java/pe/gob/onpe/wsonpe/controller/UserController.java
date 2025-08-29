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
import pe.gob.onpe.wsonpe.enums.AccionesUsuarioEnum;
import pe.gob.onpe.wsonpe.service.IUserService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

@RestController
@RequestMapping("/usuario")
@Slf4j
public class UserController {


  @Value("${stae.trans.masterkey}")
  private String masterKey;

  private final IUserService userService;

  public UserController(IUserService userService) {
    this.userService = userService;
  }

  @GetMapping()
  public RedirectView getMethod() {
    return new RedirectView("/");
  }

  @PostMapping(consumes = {"application/x-www-form-urlencoded;charset=UTF-8",
    MediaType.APPLICATION_JSON_VALUE})
  public MensajeWsResponse userLogin(@RequestParam(value = "a") String accion,
    @RequestParam(value = "d") String llaveUsuario,
    @RequestParam(value = "f") String mesa,
    @RequestParam(value = "g") String tipoSolucion,
    @RequestParam(value = "e") String usbParte,
    @RequestParam(value = "h") String usbModulo,
    @RequestParam(value = "j") String paginaNro) {

    String requestUser = WsOnpeUtils.getRequestUser();

    UserRequest request = new UserRequest(accion, requestUser, llaveUsuario, mesa,
      tipoSolucion, usbParte, usbModulo, paginaNro);

    if (!request.getLlaveUsuario().equals(masterKey)) {
      return new MensajeWsResponse(1, false, "Key de acceso incorrecta");
    }

    if (request.getAccion().equals(AccionesUsuarioEnum.UPDATE_ESTADO.getCode())) {
      return userService.updateEstado(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.VERIFICAR_PUESTA_CERO.getCode())) {
      return userService.verificarPuestaCero(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.VERIFICAR_PUESTA_CERO_DIAGNOSTICO.getCode())) {
      return userService.verificarPuestaCeroDiagnostico(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.VERIFICAR_PUESTA_CERO_Y_TRANSMITIDO.getCode())) {
      return userService.verificarPuestaCeroYTransmitido(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.VERIFICAR_PUESTA_CERO_Y_TRANSMITIDO2.getCode())) {
      return userService.verificarPuestaCeroYTransmitido2(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.EJECUTAR_PUESTA_CERO.getCode())) {
      return userService.ejecutarPuestaCero(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.EJECUTAR_PUESTA_CERO_DIAGNOSTICO.getCode())) {
      return userService.ejecutarPuestaCeroD(request);
    } else if(request.getAccion().equals(AccionesUsuarioEnum.VERIFICAR_HORA_INGRESO.getCode())) {
      return userService.verificarTimeSufEs(request);
    }else if(request.getAccion().equals(AccionesUsuarioEnum.VSA.getCode())) {
      return userService.verificarSessionActiva(request);
    } else {
      return new MensajeWsResponse(0, false, "Codigo de accion no encontrado.");
    }

  }


}
