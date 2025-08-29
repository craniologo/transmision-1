package pe.gob.onpe.wsonpe.constants;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class WebService {

  @Getter
  private static String tipoEnvioData;

  @SuppressWarnings("unused")
  private WebService(
    @Value("${stae.trans.typeenviodata}") String localTipoEnvioData
  ) {
    setTipoEnvioData(localTipoEnvioData);
  }

  private static void setTipoEnvioData(String value) {
    tipoEnvioData = value;
  }

  public static final int SUCCESS_RESULT = 1;
  public static final int TRAMA_TRANS = 65;
  public static final int MESA_TRANS = 66;
  public static final int VALIDATE_FILE = 67;
  public static final int REGISTRO_TAREAS = 3;
  public static final int LISTA_TAREAS = 21;
  public static final int REGISTRO_D_TAREAS = 5;
  public static final int JSON_NOT_NULL = 22;
  public static final int JSON_NULL = 23;
  public static final int ENCRYPTED_FILE = 103;
  public static final int ACTIVE = 1;
  public static final int VALIDATION_FILE_FAILED = -100;
  public static final int NO_FILE_IN_SYNCHRO = 20;
  public static final int REGISTRO_TRAMAS = 4;

  public static final String LOG_HEAD_FORMAT = "Mesa:%s, S:%s, M:%s, T:%s";

  public static final String FILE_SEPARATOR = System.getProperty("file.separator");

}
