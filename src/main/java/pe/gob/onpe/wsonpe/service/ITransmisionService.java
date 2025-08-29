package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

public interface ITransmisionService {
  String getEncryptionKey();
  MensajeWsResponse validateAndConfirmFile(String mesa, int tipoSolucion, int tipoModulo, int tipoTrama,
                                           int idFlujo, String username, String fileName);

  void setCertificatesFolders();
  void moveCertificates(String sourceDirectory);
}
