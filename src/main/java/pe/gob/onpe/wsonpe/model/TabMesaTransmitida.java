package pe.gob.onpe.wsonpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "TAB_MESA_TRANSMITIDA")
@Data
@NamedStoredProcedureQuery(name = "TabMesaTransmitida.spInsercionTransmisionTabMesaTransmitida",
  procedureName = "jel_stae.SP_INS_TRANSMISION",
  parameters = {
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_MESA_PK", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TRAMA", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_PAGINA_NRO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_USUARIO", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_DATA_ELECCION", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_DATA_ELECCION2", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_DATA_FIRMA", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_DATA_META", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_D_FECHA_TRANSMISION_INI", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_D_FECHA_TRANSMISION_FIN", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_PDF_DIGEST", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class) })
@NamedStoredProcedureQuery(name = "TabMesaTransmitida.spActualizaFileTransmisionTabMesaTransmitida",
  procedureName = "jel_stae.SP_VALIDA_ARCHIVO",
  parameters = {
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_MESA_PK", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TRAMA", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_PAGINA_NRO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_PDF_DIGEST", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_ARCHIVO_VALIDO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_ARCHIVO_NOMBRE", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class) })
@NamedStoredProcedureQuery(name = "TabMesaTransmitida.spValidaTransmisionArchivo",
  procedureName = "jel_stae.sp_valida_transmision_archivo", parameters = {
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_MESA_PK", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TRAMA", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_PAGINA_NRO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USUARIO", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class) })
public class TabMesaTransmitida {
  @Id
  @Column(name = "N_MESA_TRANS_PK")
  private Integer mesaTransPk;

  @Column(name = "C_MESA")
  private String mesaPk;

  @Column(name = "N_TIPO_SOLUCION")
  private Integer tipoSolucion;

  @Column(name = "N_MODULO")
  private Integer modulo;

  @Column(name = "N_TIPO_TRAMA")
  private Integer tipoTrama;

  @Column(name = "N_PAGINA_NRO")
  private Integer paginaNro;

  @Column(name = "N_USUARIO")
  private Integer usuario;

  @Column(name = "C_DATA_ELECCION")
  private String dataEleccion;

  @Column(name = "C_DATA_FIRMA")
  private String dataFirma;

  @Column(name = "D_FECHA_TRANSMISION_INI")
  private Timestamp fechaTransmisionIni;

  @Column(name = "D_FECHA_TRANSMISION_FIN")
  private Timestamp fechaTransmisionFin;

  @Column(name = "N_FLAG_SINCRONIZACION")
  private Integer flagSincronizacion;

  @Column(name = "N_PID")
  private Integer pid;

  @Column(name = "C_DATA_METADOC")
  private String dataMetadoc;

  @Column(name = "C_PDF_DIGEST")
  private String pdfDigest;

  @Column(name = "N_FLAG_PDF")
  private Integer flagPdf;

  @Column(name = "N_REINTENTO")
  private Integer reintento;

  @Column(name = "N_FLAG_TRAMA")
  private Integer nFlagTrama;

  @Column(name = "C_FILE_NAME")
  private String nFileName;
}
