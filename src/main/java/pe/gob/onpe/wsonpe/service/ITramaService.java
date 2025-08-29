package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

public interface ITramaService {
  MensajeWsResponse receiveTrama(String usuario, String trama);
}
