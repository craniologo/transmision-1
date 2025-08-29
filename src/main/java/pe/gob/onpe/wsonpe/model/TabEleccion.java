package pe.gob.onpe.wsonpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TAB_ELECCION")
public class TabEleccion {


  @Id
  @Column(length = 2, name = "C_ELECCION_PK")
  private String cEleccionPk;

  @Column(name = "C_NOMBRE_ELECCION")
  private String cNombreeleccion;

  @Column(name = "C_NOMBRE_CORTO_ELECCION")
  private String cNombreCortoEleccion;

  @Column(name = "N_ESTADO")
  private Short nEstado;

  @Column(name = "N_ORDEN")
  private Short nOrden;

  @Column(name = "C_PROCESO")
  private String cProceso;

  @Column(name = "C_TOTAL")
  private String cTotal;

  @Column(name = "C_PREFIX")
  private String cPrefix;

  @Column(name = "C_NOMBRE_MENU")
  private String cNombreMenu;

  @Column(name = "C_NOMBRE_PDF")
  private String cNombrePdf;

  @Column(name = "N_SINCRONIZACION")
  private Long nSincronizacion;

}
