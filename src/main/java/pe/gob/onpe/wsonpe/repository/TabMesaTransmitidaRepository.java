package pe.gob.onpe.wsonpe.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import pe.gob.onpe.wsonpe.model.TabMesaTransmitida;
import java.util.Map;

public interface TabMesaTransmitidaRepository extends JpaRepository<TabMesaTransmitida, Long> {

  @Procedure(name = "TabMesaTransmitida.spInsercionTransmisionTabMesaTransmitida")
  Map<String, Object> spInsercionTransmision(
    @Param("PI_C_MESA_PK") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_MODULO") Integer tipoModulo,
    @Param("PI_N_TIPO_TRAMA") Integer tipoTrama,
    @Param("PI_N_PAGINA_NRO") Integer paginaNro,
    @Param("PI_C_USUARIO") String usuario,
    @Param("PI_C_DATA_ELECCION") String trama,
    @Param("PI_C_DATA_ELECCION2") String trama2,
    @Param("PI_C_DATA_FIRMA") String firma,
    @Param("PI_C_DATA_META") String meta,
    @Param("PI_D_FECHA_TRANSMISION_INI") int fechaTransmisionIni,
    @Param("PI_D_FECHA_TRANSMISION_FIN") int fechaTransmisionFin,
    @Param("PI_C_PDF_DIGEST") String pdfDigest
  );

  @Procedure(name = "TabMesaTransmitida.spActualizaFileTransmisionTabMesaTransmitida")
  Map<String, Object> spActualizaFileTransmision(
    @Param("PI_C_MESA_PK") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_MODULO") Integer tipoModulo,
    @Param("PI_N_TIPO_TRAMA") Integer tipoTrama,
    @Param("PI_N_PAGINA_NRO") Integer paginaNro,
    @Param("PI_C_PDF_DIGEST") String hash,
    @Param("PI_ARCHIVO_VALIDO") Integer archivoValido,
    @Param("PI_ARCHIVO_NOMBRE") String archivoNombre
  );

  @Procedure(name = "TabMesaTransmitida.spValidaTransmisionArchivo")
  Map<String, Object> spValidaTransmisionArchivo(
    @Param("PI_C_MESA_PK") String mesa,
    @Param("PI_N_TIPO_SOLUCION") Integer tipoSolucion,
    @Param("PI_N_MODULO") Integer tipoModulo,
    @Param("PI_N_TIPO_TRAMA") Integer tipoTrama,
    @Param("PI_N_PAGINA_NRO") Integer tipoFlujo,
    @Param("PI_USUARIO") String username
  );

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query(
    "DELETE FROM TabMesaTransmitida " +
      "WHERE usuario = :usuarioPk " +
      "AND tipoTrama NOT IN (98, 99)"
  ) void eliminarMesasTransmitidasPorUsuario(@Param("usuarioPk") Integer usuarioPk);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query( nativeQuery = true,
    value =
    "DELETE FROM TAB_INTEGRACION " +
      " WHERE C_MESA IN " +
      "   (SELECT DISTINCT C_MESA " +
      "     FROM TAB_MESA_TRANSMITIDA " +
      "     WHERE N_USUARIO = :usuarioPk)"
  ) void eliminarIntegracionPorUsuario(@Param("usuarioPk") Integer usuarioPk);


  @Transactional
  @Modifying(clearAutomatically = true)
  @Query( nativeQuery = true,
    value =
    "DELETE FROM TAB_SINCRONIZACION " +
      " WHERE C_MESA IN " +
      "   (SELECT DISTINCT C_MESA " +
      "     FROM TAB_MESA_TRANSMITIDA " +
      "     WHERE N_USUARIO = :usuarioPk)"
  ) void eliminarSincronizacionPorUsuario(@Param("usuarioPk") Integer usuarioPk);

}
