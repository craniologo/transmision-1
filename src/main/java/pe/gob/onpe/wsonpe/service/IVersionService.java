package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;

public interface IVersionService {

  MensajeWsResponse verificarVersion(UserRequest request);

}
