package pe.gob.onpe.wsonpe.repository;

import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.onpe.wsonpe.model.TabUsuario;

@Repository
public interface TabUsuarioRepository extends JpaRepository<TabUsuario, Long> {

  @Procedure(name = "TabUsuario.spValidaAccesoTabUsuarioEntity")
  Map<String, Object> spValidaAcceso(@Param("PI_USER") String user,
    @Param("PI_PWD") String password);

  @Procedure(name = "TabUsuario.spActualizaEstado")
  Map<String, Object> spActualizaEstado(@Param("PI_USER") String user,
    @Param("PI_STATUS") Integer status);

  @Procedure(name = "TabUsuario.spVerificaPuestaCeroTrans")
  Map<String, Object> spVerificaPuestaCeroTrans(@Param("PI_USER") String user,
    @Param("PI_MESA") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_TIPO_USB") Integer tipoUsb,
    @Param("PI_N_PAGINA_NRO") Integer paginaNro,
    @Param("PI_N_MODULO") Integer modulo);

  @Procedure(name = "TabUsuario.spValidaTransmisionTabUsuarioEntity")
  Map<String, Object> spValidaTransmision(
    @Param("PI_MESA") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_MODULO") Integer modulo,
    @Param("PI_N_TIPO_TRAMA") Integer tipoTrama,
    @Param("PI_N_PAGINA_NRO") Integer paginaNro
  );

  @Procedure(name = "TabUsuario.spVerificaPuestaCeroTransD")
  Map<String, Object> spVerificaPuestaCeroTransD(
    @Param("PI_USER") String user,
    @Param("PI_MESA") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_TIPO_USB") String tipoUsb,
    @Param("PI_N_MODULO") Integer modulo);

  @Procedure(name = "TabUsuario.spEjecutarPuestaCeroDTabUsuario")
  Map<String, Object> spEjecutarPuestaCeroD(@Param("PI_USER") String user);

  @Procedure(name = "TabUsuario.spValidaEstadoTabUsuario")
  Map<String, Object> spValidaEstado(@Param("PI_USER") String user);

  TabUsuario findByUsuario(String usuario);

}
