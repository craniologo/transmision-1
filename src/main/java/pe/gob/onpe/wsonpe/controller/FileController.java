package pe.gob.onpe.wsonpe.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import pe.gob.onpe.wsonpe.constants.WebService;
import pe.gob.onpe.wsonpe.dto.FileRequest;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.service.IFileService;
import pe.gob.onpe.wsonpe.utils.WsOnpeUtils;

@RestController
@RequestMapping("/fileUpload")
@Slf4j
public class FileController {

  @Value("${stae.trans.masterkey}")
  private String masterKey;

  private final IFileService fileService;

  public FileController(IFileService fileService){
    this.fileService = fileService;
  }

  @GetMapping()
  public RedirectView getMethod() {
    return new RedirectView("/");
  }

  @PostMapping
  public MensajeWsResponse storeFile(
    @RequestParam(value = "d") String key,
    @RequestParam(value = "f") String mesa,
    @RequestParam(value = "g") Integer tipoSolucion,
    @RequestParam(value = "h") Integer tipoModulo,
    @RequestParam(value = "i") Integer tipoTrama,
    @RequestParam(value = "j") Integer npaginaNro,
    @RequestParam("file") MultipartFile file
  ) {

    String requestUser = WsOnpeUtils.getRequestUser();

    String logHead = String.format(WebService.LOG_HEAD_FORMAT, mesa, tipoSolucion, tipoModulo, tipoTrama);

    FileRequest request = new FileRequest(requestUser, key, mesa, tipoSolucion, tipoModulo, tipoTrama, npaginaNro, file);

    log.info("{}. FileController: request: {}", logHead, request);

    MensajeWsResponse ans;

    if ( request.getKey() == null || !request.getKey().equals(masterKey)) {
      ans = new MensajeWsResponse(9, false, "Key de acceso incorrecta");
    } else {
      ans = this.fileService.storeFile(request);
    }


    log.info("{}. FileController Response : {}", logHead, ans);
    return ans;
  }
}
