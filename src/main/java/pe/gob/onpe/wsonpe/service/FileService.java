package pe.gob.onpe.wsonpe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.FileRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class FileService implements IFileService {

  @Value("${stae.trans.storageFolder}")
  private String storageFolder;

  private final TabMesaTransmitidaRepository mesaTransmitidaRepository;
  private final ITransmisionService transmisionService;


  public FileService(TabMesaTransmitidaRepository mesaTransmitidaRepository,
                     ITransmisionService transmisionService) {
    this.mesaTransmitidaRepository = mesaTransmitidaRepository;
    this.transmisionService = transmisionService;
  }

  @Override
  public MensajeWsResponse storeFile(FileRequest request) {

    log.info("Acción valida tranmision del archivo.");
    String funcName = "storeFile";

    this.transmisionService.setCertificatesFolders();

    String mesa = request.getMesa();
    String username = request.getUsuario();
    Integer tipoSolucion = request.getTipoSolucion();
    Integer tipoModulo = request.getTipoModulo();
    Integer tipoTrama = request.getTipoTrama();
    Integer npaginaNro = request.getNpaginaNro();

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, tipoModulo, tipoTrama);

    String receivedFileName;

    try {
      MultipartFile multipartFile = request.getFile();
      receivedFileName = multipartFile.getOriginalFilename();

      log.info("mesa:{}, tipoSolucion:{}, tipoModulo:{}, tipoTrama:{}, npagina:{}, username:{}",
        mesa, tipoSolucion, tipoModulo, tipoTrama, npaginaNro, username);
      Map<String, Object> validaTransmisionArchivo = mesaTransmitidaRepository
        .spValidaTransmisionArchivo(mesa, tipoSolucion, tipoModulo, tipoTrama, npaginaNro, username);


      Integer codidoValidacion = (Integer) validaTransmisionArchivo.get("PO_RESULTADO");
      boolean successValidacion = codidoValidacion == WebService.SUCCESS_RESULT;
      String descripcionValidacion = (String) validaTransmisionArchivo.get("PO_MENSAJE");

      if (codidoValidacion == 2) {
        log.info(descripcionValidacion);
        return new MensajeWsResponse(codidoValidacion, true, descripcionValidacion);
      } else if (!successValidacion) {
        log.error("{}. {}. Error en la validacion de transmision del archivo: {}", logHead, funcName, descripcionValidacion);
        return new MensajeWsResponse(codidoValidacion, false, descripcionValidacion);
      }

      if (multipartFile == null || multipartFile.isEmpty()) {
        log.warn("{}. {}. El archivo recibido es vacio", logHead, funcName);
        mesaTransmitidaRepository.spActualizaFileTransmision(
          mesa, tipoSolucion, tipoModulo, tipoTrama, npaginaNro, "", 0, receivedFileName
        );
        return new MensajeWsResponse(6, false, "Archivo vacio");
      }

      String mesaPath = Paths.get(storageFolder, mesa) + WebService.FILE_SEPARATOR;
      boolean mesaDirectoryExists = WsOnpeUtils.mesaDirectoryExistsOrCreate(mesaPath);

      if (!mesaDirectoryExists) {
        log.warn("{}. Error al crear o acceder el directorio de la mesa", logHead);
        return new MensajeWsResponse(7, false, "Error al crear o acceder el directorio de la mesa");
      }

      Path path = Paths.get(mesaPath, receivedFileName);
      multipartFile.transferTo(path);
      MensajeWsResponse fileValidation = this.transmisionService.validateAndConfirmFile(
        mesa,
        tipoSolucion,
        tipoModulo,
        tipoTrama,
        npaginaNro,
        username,
        receivedFileName
      );

      log.info("{}. {}: Respuesta de validateAndConfirmFile: {}", logHead, funcName, fileValidation);

      if (fileValidation.getSuccess() && fileValidation.getCodigo() == WebService.VALIDATE_FILE){
        fileValidation.setCodigo(1);
        fileValidation.setMessage("Transmision satisfactoria");
      }
      return fileValidation;

    } catch (Exception e) {
      log.error("{}. Error en la transmisión del archivo: {} - Stacktrace:{}", logHead, e.getMessage(), e.getStackTrace());
      return new MensajeWsResponse(8, false, "Error al subir el archivo al repositorio: " + e.getMessage());
    }
  }

}
