package pe.gob.onpe.wsonpe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name="TAB_PROCESO")
@Data
@NoArgsConstructor
public class TabProceso {

  @Id
  @Column(name = "C_PROCESO_PK",nullable = false)
  private String procesoPk;

  @Column(length = 300, name = "C_NOMBRE_PROCESO", nullable = false)
  private String nombreProceso;

  @Column(length = 500, name = "C_DESC_PROCESO")
  private String descProceso;

  @Column(name = "D_FECHA_PROCESO")
  private Timestamp fechaProceso;

  @Column(name = "C_ARCHIVO_CONFIG_SEA")
  private String archivoConfigSea;

  @Column(length = 8, name = "C_COD_AGRUPOL")
  private String codAgrupol;

  @Column(name = "N_ESTADO", nullable = false)
  private BigDecimal estado;

  @Column(name = "N_MESAS")
  private Long mesas;

  @Column(name = "N_ELECTORES")
  private Long electores;

}
