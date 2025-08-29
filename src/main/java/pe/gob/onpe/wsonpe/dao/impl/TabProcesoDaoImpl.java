package pe.gob.onpe.wsonpe.dao.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.onpe.wsonpe.dao.TabProcesoDao;
import pe.gob.onpe.wsonpe.repository.TabProcesoRepository;

import java.util.List;

@Component
@AllArgsConstructor
public class TabProcesoDaoImpl implements TabProcesoDao {

  private final TabProcesoRepository tabProcesoRepository;

  @Override
  public String obtenerFechaProceso() {
    List<String> fechasProcesos = tabProcesoRepository.obtenerFechaProceso();

    if (!fechasProcesos.isEmpty()) {
      return fechasProcesos.getFirst();
    }

    return null;
  }
}
