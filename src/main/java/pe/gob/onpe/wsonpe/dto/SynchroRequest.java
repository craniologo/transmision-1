package pe.gob.onpe.wsonpe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SynchroRequest {

  String masterKey;
  String mesa;
  Integer tipoSolucion;
  Integer tipoModulo;
  Integer tipoTrama;
  Integer force;

}
