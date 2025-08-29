package pe.gob.onpe.wsonpe.dao.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.onpe.wsonpe.dao.TabVersionDao;
import pe.gob.onpe.wsonpe.dto.MensajeWsResponse;
import pe.gob.onpe.wsonpe.repository.TabVersionRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@AllArgsConstructor
public class TabVersionDaoImpl implements TabVersionDao {

  private final TabVersionRepository tabVersionRepository;

  @Override
  public MensajeWsResponse verificarVersion(String fecha) {
    MensajeWsResponse response = new MensajeWsResponse(2,false, "");
    String mensaje = "";

    List<Object[]> versionRes = tabVersionRepository.obtenerVersion();

    if (versionRes.isEmpty()) {
      return response;
    }

    Object[] datos = versionRes.get(0);

    Object version = datos[0];
    Object descr = datos[1];

    if (version == null) {
      return response;
    }

    LocalDate localDateFechaProceso = LocalDate
      .parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yy")).minusDays(1L);

    String fechaProceso = localDateFechaProceso.format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    mensaje = version + ":" + descr + ":" +  fechaProceso;

    response.setSuccess(true);
    response.setCodigo(1);
    response.setMessage(mensaje);

    return response;
  }
}
