package pe.gob.onpe.wsonpe.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileRequest {
  private String usuario;
  private String key;
  private String mesa;
  private Integer tipoSolucion;
  private Integer tipoModulo;
  private Integer tipoTrama;
  private Integer npaginaNro;
  private MultipartFile file;
}
