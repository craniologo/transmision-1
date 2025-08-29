package pe.gob.onpe.wsonpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "TAB_PARAMETRO")
@Data
public class TabParametro {

  @Id
  @Column(name = "C_TAB_PARAMETRO_PK", length = 10)
  private String tabParametroId;

  @Column(name = "C_CODIGO_TABLA", length = 20)
  private String codigoTabla;

  @Column(name = "C_VALOR", length = 200)
  private String valor;

  @Column(name = "C_ESTADO", length = 1)
  private String estado;

  @Column(name = "N_ORDEN")
  private Integer orden;

}
