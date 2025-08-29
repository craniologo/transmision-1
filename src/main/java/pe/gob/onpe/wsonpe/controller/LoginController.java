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
import pe.gob.onpe.wsonpe.dto.LoginRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.ILoginService;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

  private final ILoginService loginService;

  @Value("${stae.trans.masterkey}")
  private String masterKey;

  public LoginController(ILoginService loginService){
    this.loginService = loginService;
  }


  @GetMapping()
  public RedirectView getMethod() {
    return new RedirectView("/");
  }

  @PostMapping(consumes = {"application/x-www-form-urlencoded;charset=UTF-8",
    MediaType.APPLICATION_JSON_VALUE})
  public MensajeWsResponse login(@RequestParam(value = "b") String usuario,
                                 @RequestParam(value = "c") String clave,
                                 @RequestParam(value = "d") String key,
                                 @RequestParam(value = "r", required = false) Integer refreshToken)
  {

    log.info("login: usuario: {}", usuario);
    log.info("login: clave: {}", clave);
    log.info("login: key: {}", key);
    log.info("login: refreshToken: {}", refreshToken);

    boolean isRefreshToken = refreshToken != null && refreshToken == 1;

    LoginRequest loginRequest = new LoginRequest(usuario, clave, key, isRefreshToken);

    if (loginRequest.getKey() == null || !loginRequest.getKey().equals(masterKey)) {
      return new MensajeWsResponse(2, false, "Key de acceso incorrecta");
    }

    return loginService.login(loginRequest);

  }
}
