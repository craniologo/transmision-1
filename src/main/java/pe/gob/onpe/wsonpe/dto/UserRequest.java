package pe.gob.onpe.wsonpe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

  private String accion;//usuario
  private String usuario;//clave
  private String llaveUsuario;//tipo de Usuario  SEA,SAJE,VOTO ELECTRONICO,etc
  private String mesa;
  private String tipoSolucion;
  private String usbParte;
  private String usbModulo;
  private String paginaNro;

  public UserRequest(String accion, String usuario, String llaveUsuario) {
    this.accion = accion;
    this.usuario = usuario;
    this.llaveUsuario = llaveUsuario;
  }


}
