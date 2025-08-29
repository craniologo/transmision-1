package pe.gob.onpe.wsonpe.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.wsonpe.model.TabEleccion;

@Repository
public interface TabEleccionRepository extends JpaRepository<TabEleccion, String> {

  List<TabEleccion> findAll();


}
