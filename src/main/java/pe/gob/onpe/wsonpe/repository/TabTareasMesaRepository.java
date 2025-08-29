package pe.gob.onpe.wsonpe.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import pe.gob.onpe.wsonpe.model.TabTareasMesa;
import java.util.Map;

public interface TabTareasMesaRepository extends JpaRepository<TabTareasMesa, String>{
  @Procedure(name = "TabTrama.spInsercionTareasTabTrama")
  Map<String, Object> spInsercionTareas(
    @Param("PI_C_MESA_PK") String mesa,
    @Param("PI_USER") String usuario,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_TIPO_TRAMA") Integer tipoTrama,
    @Param("PI_N_MODULO") Integer tipoModulo,
    @Param("PI_N_IDFLUJO") Integer tipoFlujo
  );

  @Procedure(name = "TabTrama.spVerificaTareasTabTrama")
  Map<String, Object> spVerificaTareas(
    @Param("PI_C_MESA_PK") String mesa,
    @Param("PI_COUNT_TRAMA") Integer strama
  );

  @Procedure(name = "TabTrama.spListaTareasTabTrama")
  Map<String, Object> spListaTareas(
    @Param("PI_C_MESA_PK") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_TIPO_TRAMA") Integer tipoTrama,
    @Param("PI_N_MODULO") Integer tipoModulo,
    @Param("PI_N_IDFLUJO") Integer tipoFlujo,
    @Param("PI_N_TIPO_TAREA") String tipoTarea
  );

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE TabTareasMesa T " +
      "SET T.flag = 1, T.usuario = :usuario " +
      "WHERE T.mesaPk = :mesaPk " +
        "AND T.tipoSolucion = :tipoSolucion " +
        "AND T.tipoTrama = :tipoTrama " +
        "AND T.modulo = :modulo " +
        "AND T.idFlujo = :idFlujo " +
        "AND T.tipoTarea = 'F' " +
        "AND T.flag = 0")
  void confirmarTransmisionArchivo(@Param("usuario") String usuario,
                                   @Param("mesaPk") String mesaPk,
                                   @Param("tipoSolucion") Integer tipoSolucion,
                                   @Param("tipoTrama") Integer tipoTrama,
                                   @Param("modulo") Integer modulo,
                                   @Param("idFlujo") Integer idFlujo);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query(
    "DELETE FROM TabTareasMesa T " +
      "WHERE T.usuario = :usuario")
  void eliminarPorUsuario(@Param("usuario") String usuario);
}
