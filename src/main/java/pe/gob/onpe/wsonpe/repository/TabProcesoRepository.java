package pe.gob.onpe.wsonpe.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.wsonpe.model.TabProceso;

@Repository
public interface TabProcesoRepository extends JpaRepository<TabProceso, String> {

  List<TabProceso> findByEstado(Integer estado);

  @Query("SELECT DISTINCT TO_CHAR(T.fechaProceso, 'DD/MM/YY') " +
         "FROM TabProceso T")
  List<String> obtenerFechaProceso();
}
