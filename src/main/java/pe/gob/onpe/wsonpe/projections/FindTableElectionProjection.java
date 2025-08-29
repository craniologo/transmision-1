package pe.gob.onpe.wsonpe.projections;

import lombok.Setter;
import lombok.Value;

@Value
@Setter
public class FindTableElectionProjection {
  private String cEleccionPk;
  private String cNombreCortoEleccion;
}
