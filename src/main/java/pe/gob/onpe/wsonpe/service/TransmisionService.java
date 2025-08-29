package pe.gob.onpe.wsonpe.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.projections.FindConfigurationProjection;
import pe.gob.onpe.wsonpe.repository.TabConfTxRepository;
import pe.gob.onpe.wsonpe.repository.TabMesaTransmitidaRepository;
import pe.gob.onpe.wsonpe.repository.TabTareasMesaRepository;
import pe.gob.onpe.wsonpe.utils.WsOnpeFileUtils;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;
import pe.gob.onpe.wsonpe.utils.ZipUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TransmisionService implements ITransmisionService {

  @Value("${stae.trans.storageFolder}")
  private String storageFolder;

  @Value("${stae.trans.nombreEleccion}")
  private String electionName;

  @Value("${stae.trans.certsFolder}")
  private String certsFolderRoot;

  @Value("${stae.trans.certificateType}")
  private String certificateType;

  private String certsFolderMesa = "";
  private String certsFolderEF = "";

  private final TabConfTxRepository confTxRepository;
  private final TabMesaTransmitidaRepository mesaTransmitidaRepository;
  private final TabTareasMesaRepository tabTareasMesaRepository;

  public TransmisionService(TabConfTxRepository confTxRepository,
                            TabMesaTransmitidaRepository mesaTransmitidaRepository,
                            TabTareasMesaRepository tabTareasMesaRepository) {
    this.confTxRepository = confTxRepository;
    this.mesaTransmitidaRepository = mesaTransmitidaRepository;
    this.tabTareasMesaRepository = tabTareasMesaRepository;
  }

  @Override
  public String getEncryptionKey() {
    List<FindConfigurationProjection> opConfTx = confTxRepository.findBynEstado(WebService.ACTIVE);

    String key = "";

    if (opConfTx != null && !opConfTx.isEmpty()) {
      FindConfigurationProjection confTx = opConfTx.getLast();
      key = confTx.getCEncFile();
    }

    return key;
  }


  @Override
  @SuppressWarnings("java:S2629")
  public MensajeWsResponse validateAndConfirmFile(String mesa, int tipoSolucion, int tipoModulo, int tipoTrama,
                                                  int idFlujo, String username, String fileName) {

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, tipoModulo, tipoTrama);
    String funcName = "validateAndConfirmFile";
    String encryptionKey = this.getEncryptionKey();
    String mesaPath = Paths.get(storageFolder, mesa) + WebService.FILE_SEPARATOR;
    boolean mesaDirectoryExists = WsOnpeUtils.mesaDirectoryExistsOrCreate(mesaPath);

    if (!mesaDirectoryExists) {
      // NOSONAR o @SuppressWarnings("java:S2629") para la línea específica
      log.warn("{}. Error al crear o acceder el directorio de la mesa", logHead);
      return new MensajeWsResponse(7, false, "Error al crear o acceder el directorio de la mesa");
    }

    Integer codigo;
    boolean success;
    String descripcion;
    String msg;

    try {

      Path path = Paths.get(mesaPath, fileName);
      File transmittedFile = new File(path.toString());
      String fileHash = WsOnpeUtils.getFileHash(transmittedFile.getAbsolutePath());

      if(!transmittedFile.exists()){
        msg = "El archivo no pudo ser encontrado en el sistema";
        // NOSONAR
        log.warn("{}. {}: . {} {}", logHead, funcName, fileName, msg);
        return new MensajeWsResponse(7, false, msg);
      }

      boolean validFile = true;

      if (tipoModulo != WebService.ENCRYPTED_FILE) {
        validFile = WsOnpeUtils.isValidSevenFileZip(transmittedFile);
      }
      // NOSONAR
      log.warn("{}. Archivo {} valido: {}", logHead, fileName, validFile);
      int flagValidZipFile = validFile ? 1 : 0;
      // NOSONAR
      log.info("{}. Llamando al SP spActualizaFileTransmision de actualizacion del archivo.", logHead);

      Map<String, Object> params = new HashMap<>();

      params.put("usuario", username);
      params.put("NpaginaNro", idFlujo);
      params.put("hash", fileHash);
      params.put("filename", fileName);
      params.put("filepath", transmittedFile.getAbsolutePath());
      params.put("archivoValido", flagValidZipFile);
      // NOSONAR
      log.info("{}. {}: {}", logHead, funcName, params);

      // Validación y retorno del estado del Archivo
      Map<String, Object> actualizaTransmisionFileResponse = mesaTransmitidaRepository
        .spActualizaFileTransmision(
            mesa,
            tipoSolucion,
            tipoModulo,
            tipoTrama,
            idFlujo,
            fileHash,
            flagValidZipFile,
            fileName
          );

      codigo = (Integer) actualizaTransmisionFileResponse.get("PO_RESULTADO");
      success = codigo == WebService.SUCCESS_RESULT;
      descripcion = (String) actualizaTransmisionFileResponse.get("PO_MENSAJE");

      // La trama no ha sido aun transmitida y no hay datos para validar
      if (codigo == WebService.VALIDATE_FILE) {
        // NOSONAR
        log.info("{}. {}. Validacion del {} archivo: {}", logHead, funcName, fileName, descripcion);
        return new MensajeWsResponse(codigo, true, descripcion);
      }

      if (!success) {
        if (tipoModulo !=  WebService.ENCRYPTED_FILE) {
          WsOnpeFileUtils.deleteFile(transmittedFile);
        }
        WsOnpeUtils.writeLog(username, "transmisión del archivo", descripcion);
        return new MensajeWsResponse(codigo, false, descripcion);
      }

      // Descomprime y almacena los archivos en el repositorio
      if (tipoModulo != WebService.ENCRYPTED_FILE) {
        boolean filesSaved = WsOnpeUtils.saveFilesFromCompressed(transmittedFile, mesaPath);
        if (filesSaved) WsOnpeFileUtils.deleteFile(transmittedFile);
      } else {
        String decryptedFolderPath = ZipUtils.unZipPassword(
          transmittedFile.getName(), mesaPath, mesaPath, encryptionKey);
        log.info("{}. {}. Archivo {} desencriptado en {}", logHead, funcName, fileName, decryptedFolderPath);
        this.moveCertificates(decryptedFolderPath);
      }

      // Confirma la transmisión del archivo al repositorio
      log.info("{}. {}. Confirmando la transmision del archivo {}", logHead, funcName, fileName);

      // reemplazando SP_CONFIRMA_TRANS_ARCHIVO
      tabTareasMesaRepository.confirmarTransmisionArchivo(username, mesa, tipoSolucion, tipoTrama, tipoModulo, idFlujo);

      descripcion = "Archivo transmitido correctamente";

      log.info("{}. {}: Respuesta de la confirmacion. Codigo: {}, desc: {}", logHead, funcName, codigo, descripcion);

    } catch (Exception e) {
      codigo = 0;
      success = false;
      descripcion = e.getMessage();
    }

    WsOnpeUtils.writeLog(username, "transmisión del archivo", descripcion);
    return new MensajeWsResponse(codigo, success, descripcion);
  }

  @Override
  public void setCertificatesFolders() {

    try {

      boolean resRoot = WsOnpeFileUtils.createFolderTree(certsFolderRoot);
      if (!resRoot) return;

      String certsFolderMesaStr = Paths.get(certsFolderRoot, electionName, certificateType).toString();
      String certsFolderEfStr = Paths.get(certsFolderRoot, electionName, certificateType, "EF").toString();

      boolean resMesa = WsOnpeFileUtils.createFolderTree(certsFolderMesaStr);
      if (resMesa && (certsFolderMesa == null || !certsFolderMesa.equals(certsFolderMesaStr)))
      {
        certsFolderMesa = certsFolderMesaStr;
      }

      boolean resEf = WsOnpeFileUtils.createFolderTree(certsFolderEfStr);
      if (resEf && (certsFolderEF == null || !certsFolderEF.equals(certsFolderEfStr)))
      {
        certsFolderEF = certsFolderEfStr;
      }


    } catch (Exception e) {
      log.error("ERROR en creación de las carpetas de los certificados: {}", e.getMessage());
    }
  }


  @Override
  public void moveCertificates(String sourceDirectory) {
    if (sourceDirectory.isEmpty()) return;

    File sourceDirectoryFile = new File(sourceDirectory);
    if (!sourceDirectoryFile.exists()) return;

    File[] allFiles = sourceDirectoryFile.listFiles();
    if (allFiles == null) return;

    String localCertsFolderMesa = Paths.get(certsFolderRoot, electionName, certificateType).toString();
    String localCertsFolderEF = Paths.get(certsFolderRoot, electionName, certificateType, "EF").toString();

    for (File f: allFiles) {
      if (f.isDirectory()) {
        moveCertificates(f.getAbsolutePath());
        continue;
      }
      processCertificateFile(f, localCertsFolderMesa, localCertsFolderEF);
    }
  }

  public void processCertificateFile(File file, String certsFolderMesa, String certsFolderEF) {
    String filename = file.getName();
    if (!filename.endsWith(".crl") && !filename.endsWith(".crt")) {
      return;
    }

    try {
      String destFolder = determineDestinationFolder(filename, certsFolderMesa, certsFolderEF);

      if (destFolder.isEmpty()) {
        log.warn("La carpeta de destino de los certificados es vacía");
        return;
      }

      File destFile = new File(Paths.get(destFolder, filename).toString());
      FileUtils.copyFile(file, destFile);
    } catch (Exception e) {
      log.error("Error copiando certificado: {} ,{}, {}", e.getMessage(), e.getCause(), e.getStackTrace());
    }
  }

  private String determineDestinationFolder(String filename, String certsFolderMesa, String certsFolderEF) {
    if (filename.startsWith("mesa")) {
      return certsFolderMesa;
    } else if (filename.startsWith("certificado")) {
      return certsFolderEF;
    }
    return "";
  }
}
