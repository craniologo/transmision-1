package pe.gob.onpe.wsonpe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
  private String usuario;
  private String clave;
  private String key;
  private boolean refreshToken;
}
