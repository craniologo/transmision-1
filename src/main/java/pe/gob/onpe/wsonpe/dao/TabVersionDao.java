package pe.gob.onpe.wsonpe.dao;

import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

public interface TabVersionDao {

  MensajeWsResponse verificarVersion(String fecha);

}
