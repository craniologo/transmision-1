package pe.gob.onpe.wsonpe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.SynchroRequest;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;
import pe.gob.onpe.wsonpe.utils.NetClientPost;
import pe.gob.onpe.wsonpe.utils.exceptions.InterruptedCustomException;

@Slf4j
@Service
public class AsyncSynchroService implements IAsyncSynchroService {

  @Value("${stae.synchro.url}")
  private String synchroUrl;

  @Value("${stae.synchro.masterKey}")
  private String synchroMasterKey;


  public AsyncSynchroService(TabTareasMesaRepository tramaRepository) {
  }

  @Override
  @Async
  public void doSynchro(String mesa, Integer tipoSolucion, Integer tipoModulo, Integer tipoTrama) {

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, tipoModulo, tipoTrama);
    try {
      Thread.sleep(5000);

      String url = this.synchroUrl + "synchro/trama";
      SynchroRequest synchroRequest = new SynchroRequest(synchroMasterKey, mesa, tipoSolucion, tipoModulo, tipoTrama, 0);

      NetClientPost request;
      MensajeWsResponse response;

      int numAttempts = 3;
      int attempt = 0;

      do {
        if (attempt > 0) Thread.sleep(1000);

        request = createNetClient(url);
        request.setRequestParams(synchroRequest);
        response = request.getResult();

        attempt++;
        log.info("{}. Intento numero {} con respuesta {}", logHead, attempt, response);
      } while (!response.getSuccess() && response.getCodigo() == WebService.NO_FILE_IN_SYNCHRO && attempt < numAttempts);

      if (!response.getSuccess() && response.getCodigo() == WebService.NO_FILE_IN_SYNCHRO) {
        request = new NetClientPost(url);
        synchroRequest.setForce(1);
        request.setRequestParams(synchroRequest);
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new InterruptedCustomException(ex);
    } catch (Exception e) {
      log.error("{}. Error sincronizando la mesa {}: {}", logHead, mesa, e.getMessage());
    }

    log.info("{}. Sincronizacion finalizada de la trama", logHead);
  }

  // Método protegido para facilitar pruebas unitarias
  protected NetClientPost createNetClient(String url) {
    return new NetClientPost(url);
  }
}
