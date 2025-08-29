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
@Table(name = "TAB_USUARIO")
@Data
@NamedStoredProcedureQuery(
  name = "TabUsuario.spValidaAccesoTabUsuarioEntity",
  procedureName = "jel_stae.sp_valida_acceso",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_PWD", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
@NamedStoredProcedureQuery(
  name = "TabUsuario.spActualizaEstado",
  procedureName = "jel_stae.SP_ACT_ESTADO",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_STATUS", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
@NamedStoredProcedureQuery(
  name = "TabUsuario.spVerificaPuestaCeroTrans",
  procedureName = "jel_stae.SP_VERIFICA_PUESTA_CERO_TRANS",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_MESA", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_USB", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_PAGINA_NRO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
@NamedStoredProcedureQuery(
  name = "TabUsuario.spValidaTransmisionTabUsuarioEntity",
  procedureName = "jel_stae.SP_VALIDA_TRASMISION",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_MESA", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_TRAMA", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_PAGINA_NRO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
@NamedStoredProcedureQuery(
  name = "TabUsuario.spVerificaPuestaCeroTransD",
  procedureName = "jel_stae.SP_VERIFICA_PC_TRANS_D",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_MESA", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_SOLUCION", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_TIPO_USB", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_MODULO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_N_PAGINA_NRO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
@NamedStoredProcedureQuery(
  name = "TabUsuario.spEjecutarPuestaCeroDTabUsuario",
  procedureName = "jel_stae.SP_PUESTA_CERO_D",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
@NamedStoredProcedureQuery(
  name = "TabUsuario.spValidaEstadoTabUsuario",
  procedureName = "jel_stae.SP_VALIDA_ESTADO",
  parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "PI_USER", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_PROCEDER", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_RESULTADO", type = Integer.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "PO_MENSAJE", type = String.class)
  })
public class TabUsuario {

  @Id
  @Column(name = "N_USUARIO_PK")
  private Integer nUsuarioPk;

  @Column(name = "C_USUARIO")
  private String usuario;

  @Column(name = "C_CLAVE")
  private String cClave;

  @Column(name = "N_PERFIL")
  private Integer nPerfil;

  @Column(name = "N_FLAG_CENTRO_ACOPIO")
  private Short nFlagCentroAcopio;

  @Column(name = "N_LOGIN")
  private Short nLogin;

  @Column(name = "N_FLAG_PUESTA_CERO")
  private Short nFlagPuestaCero;

  @Column(name = "N_ESTADO")
  private Short nEstado;

  @Column(name = "C_CLAVE_MD5")
  private String cClaveMd5;

  @Column(name = "N_FLAG_PUESTA_CERO_D")
  private Long nFlagPuestaCeroD;

  @Column(name = "N_PROYECTO")
  private Long nProyecto;

  @Column(name = "N_CRUD")
  private Long nCrud;

  @Column(name = "C_ORGANO")
  private String cOrgano;

  @Column(name = "N_ROLES")
  private Long nRoles;

  @Column(name = "D_CADUCIDAD")
  private Timestamp dCaducidad;

  @Column(name = "N_USR_CREACION")
  private Long nUsrCreacion;

  @Column(name = "D_FEC_CREACION")
  private Timestamp dFecCreacion;

  @Column(name = "N_USR_EDICION")
  private Long nUsrEdicion;

  @Column(name = "D_FEC_EDICION")
  private Timestamp dFecEdicion;
}
