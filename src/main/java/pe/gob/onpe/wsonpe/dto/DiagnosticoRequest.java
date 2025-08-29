package pe.gob.onpe.wsonpe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class DiagnosticoRequest {
  private String usuario;
  private String key;
  private String  idEquipo;
  private String trama;
  private Integer tipoSolucion;
  private Integer tipoModulo;
  private Integer tipoTrama;
  private Integer paginaNro;
}
