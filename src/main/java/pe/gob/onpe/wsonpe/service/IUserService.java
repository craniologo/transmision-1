package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;

public interface IUserService {

  MensajeWsResponse updateEstado(UserRequest request);

  MensajeWsResponse verificarPuestaCero(UserRequest request);

  MensajeWsResponse verificarPuestaCeroDiagnostico(UserRequest request);

  MensajeWsResponse verificarPuestaCeroYTransmitido(UserRequest request);

  MensajeWsResponse verificarPuestaCeroYTransmitido2(UserRequest request);

  MensajeWsResponse ejecutarPuestaCero(UserRequest request);

  MensajeWsResponse ejecutarPuestaCeroD(UserRequest request);

  MensajeWsResponse verificarTimeSufEs(UserRequest request);

  MensajeWsResponse verificarSessionActiva(UserRequest request);

}
