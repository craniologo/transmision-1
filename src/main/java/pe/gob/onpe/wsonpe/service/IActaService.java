package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.ActaRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

public interface IActaService {

  MensajeWsResponse receiveActa(ActaRequest request);

}
