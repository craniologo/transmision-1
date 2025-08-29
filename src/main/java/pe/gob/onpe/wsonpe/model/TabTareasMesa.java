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
@Table(name = "TAB_TAREAS_MESA")
@Data
@NamedStoredProcedureQuery(name = "TabTrama.spInsercionTareasTabTrama",
  procedureName = "jel_stae.SP_INS_TAREAS_P", parameters = {
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_MESA_PK", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TRAMA", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_IDFLUJO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class) })
@NamedStoredProcedureQuery(name = "TabTrama.spVerificaTareasTabTrama",
  procedureName = "jel_stae.SP_VERIFICA_TAREAS_P", parameters = {
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_MESA_PK", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_COUNT_TRAMA", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class) })
@NamedStoredProcedureQuery(name = "TabTrama.spListaTareasTabTrama",
  procedureName = "jel_stae.SP_LIS_TAREAS_P", parameters = {
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_C_MESA_PK", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TRAMA", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_IDFLUJO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TAREA", type = String.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
  @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class) })

public class TabTareasMesa {
  @Id
  @Column(name = "C_MESA")
  private String mesaPk;

  @Column(name = "N_TAREA_MESA")
  private Integer tareaNro;

  @Column(name = "N_TIPO_SOLUCION")
  private Integer tipoSolucion;

  @Column(name = "N_TIPO_TRAMA")
  private Integer tipoTrama;

  @Column(name = "N_MODULO")
  private Integer modulo;

  @Column(name = "N_IDFLUJO")
  private Integer idFlujo;

  @Column(name = "C_TIPO_TAREA")
  private String tipoTarea;

  @Column(name = "N_FLAG")
  private Integer flag;

  @Column(name = "C_USUARIO")
  private String usuario;

  @Column(name = "D_FECHA")
  private Timestamp fecha;
}
