package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.LoginRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

public interface ILoginService {

  MensajeWsResponse login(LoginRequest request);

}
