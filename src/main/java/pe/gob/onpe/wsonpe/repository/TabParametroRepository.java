package pe.gob.onpe.wsonpe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.wsonpe.model.TabParametro;

@Repository
public interface TabParametroRepository extends JpaRepository<TabParametro, Long> {

}
