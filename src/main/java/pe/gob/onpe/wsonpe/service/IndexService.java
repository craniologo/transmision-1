package pe.gob.onpe.wsonpe.service;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import pe.gob.onpe.wsonpe.model.TabProceso;
import pe.gob.onpe.wsonpe.projections.FindConfigurationProjection;
import pe.gob.onpe.wsonpe.repository.TabConfTxRepository;
import pe.gob.onpe.wsonpe.repository.TabProcesoRepository;
import pe.gob.onpe.wsonpe.utils.ElectionInformation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@Service
public class IndexService implements IIndexService {

  private final TabProcesoRepository procesoRepository;
  private final TabConfTxRepository confTxRepository;
  private static final int ACTIVE = 1;

  public IndexService(TabProcesoRepository procesoRepository, TabConfTxRepository confTxRepository) {
    this.procesoRepository = procesoRepository;
    this.confTxRepository = confTxRepository;
  }

  public String index(Model model){
    String election = "Elección no encontrada";
    String electionDate = "Fecha no encontrada";
    String version = "";

    ElectionInformation[] allElections;

    List<TabProceso> processRes = procesoRepository.findByEstado(1);
    List<FindConfigurationProjection> opConfTx = confTxRepository.findBynEstado(ACTIVE);

    if (!processRes.isEmpty()) {
      allElections = new ElectionInformation[processRes.size()];

      SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy",
        new Locale("es", "ES"));

      if (opConfTx != null && !opConfTx.isEmpty()) {
          FindConfigurationProjection confTx = opConfTx.get(opConfTx.size()-1);
          version = " - " + confTx.getCDescripcion();
      }

      int ind = 0;
      for (TabProceso process: processRes) {
        election = process.getNombreProceso() + version;
        electionDate = sdf.format(process.getFechaProceso());
        electionDate = electionDate.substring(0, 1).toUpperCase() + electionDate.substring(1);
        allElections[ind] = new ElectionInformation(election, electionDate);
        ind++;
      }

    } else {
      allElections = new ElectionInformation[1];
      allElections[0] =  new ElectionInformation(election, electionDate);
    }

    model.addAttribute("allElections", allElections);
    return "index";
  }
}
