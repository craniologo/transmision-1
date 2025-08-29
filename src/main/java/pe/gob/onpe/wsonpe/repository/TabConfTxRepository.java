package pe.gob.onpe.wsonpe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.wsonpe.model.TabConfTx;
import pe.gob.onpe.wsonpe.projections.FindConfigurationProjection;

import java.util.List;

@Repository
public interface TabConfTxRepository extends JpaRepository<TabConfTx, Long> {

  List<FindConfigurationProjection> findBynEstado(Integer nEstado);

}
