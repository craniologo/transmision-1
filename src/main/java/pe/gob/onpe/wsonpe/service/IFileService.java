package pe.gob.onpe.wsonpe.service;

import pe.gob.onpe.wsonpe.dto.FileRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;

public interface IFileService {

  MensajeWsResponse storeFile(FileRequest request);
}
