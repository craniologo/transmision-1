package pe.gob.onpe.wsonpe.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import pe.gob.onpe.wsonpe.dao.TabProcesoDao;
import pe.gob.onpe.wsonpe.dao.TabVersionDao;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.dto.UserRequest;

@Service
@Slf4j
@AllArgsConstructor
public class VersionService implements IVersionService {

  private final TabProcesoDao tabProcesoDao;
  private final TabVersionDao tabVersionDao;

  @Override
  public MensajeWsResponse verificarVersion(UserRequest request) {

    String fechaProceso;
    String mensajeResponse = "";

    try {
      fechaProceso = tabProcesoDao.obtenerFechaProceso();
      log.info("fecha::: {}", fechaProceso);

      log.info("Usuario: {}, Accion: {}, Validando Fecha Elección. Ok", request.getUsuario(),
        request.getAccion());
      MensajeWsResponse versionResponse = tabVersionDao.verificarVersion(fechaProceso);
      log.info("versionResponse::: {}", versionResponse);

      Integer codigo = versionResponse.getCodigo();
      Boolean success = codigo == 1;
      String mensaje = versionResponse.getMessage();

      String version;
      String descr;
      String fechaDiag;
      String[] partesMensaje = versionResponse.getMessage().split(":");

      if (StringUtils.isNotBlank(mensaje)) {
        version = partesMensaje[0];
        descr = partesMensaje[1];
        fechaDiag = partesMensaje[2];

        LocalDate localDateFechaActual = LocalDate.now();
        LocalDate localDateFechaProceso = LocalDate
          .parse(fechaProceso, DateTimeFormatter.ofPattern("dd/MM/yy"));
        LocalDate localDateFechaDiag = LocalDate
          .parse(fechaDiag, DateTimeFormatter.ofPattern("dd/MM/yy"));

        String tipo = (localDateFechaActual.isEqual(localDateFechaDiag)) ? "1" : "0";
        String active = (localDateFechaActual.isEqual(localDateFechaProceso) || localDateFechaActual
          .isAfter(localDateFechaProceso)) ? "1" : "0";
        mensajeResponse = version + ":" + descr + ":" + fechaProceso + ":" + active + ":" + tipo;
        log.info(
          "Usuario: {}, Accion: {}, Version: {}, Descripcion: {}, Fecha Proceso: {}, Activo: {}, Tipo: {}",
          request.getUsuario(), request.getAccion(), version, descr, fechaProceso, active,
          tipo);
      }

      log.info("Usuario: {}, Accion: {}, Validando Version.", request.getUsuario(),
        request.getAccion());

      if (versionResponse.getSuccess()) {
        log.info("Usuario: {}, Acción: {}, Se obtuvo datos del Instalador.",
          request.getUsuario(),
          request.getAccion());
      } else {
        log.error("Usuario: {}, Acción: {}", request.getUsuario(), request.getAccion());
      }

      return new MensajeWsResponse(codigo, success, mensajeResponse);

    } catch (Exception e) {
      log.error(ExceptionUtils.getMessage(e));
      log.error(ExceptionUtils.getStackTrace(e));
      log.error("Usuario: {}, Error en la obtención de la fecha de elección", request.getUsuario());
      return null;
    }
  }

}
