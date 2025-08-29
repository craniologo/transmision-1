package pe.gob.onpe.wsonpe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.wsonpe.model.TabVersion;

import java.util.List;

@Repository
public interface TabVersionRepository extends JpaRepository<TabVersion, Long> {

  @Query("" +
    " SELECT DISTINCT TRIM(BOTH COALESCE(t.cVVersionTrans)), t.cDescripcion " +
    " FROM TabVersion t " +
    " WHERE t.nEstado = 1 " )
  List<Object[]> obtenerVersion();

}
