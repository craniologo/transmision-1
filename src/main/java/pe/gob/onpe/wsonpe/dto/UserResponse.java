package pe.gob.onpe.wsonpe.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

  private String a; // descripcion
  private String b; // C_ENC_SEA
  private String c; // C_ENC_FILE
  private Integer d; // N_TX_MESA
  private String e; // C_ENC_VEP
  private String f; // C_ENC_FIRMA
  private String g; // C_ENC_QR
  private String h; // C_HSQL_USR
  private String i; // C_HSQL_PWD
  private List<Elecciones> l; // //data de elecciones
  private String t; // USUARIO PERFIL
  private String x; // TOKEN

  @Value
  @AllArgsConstructor
  public static class Elecciones {

    private String e; // C_ELECCION_PK
    private String n; // C_NOMBRE_CORTO_ELECCION

  }


}
