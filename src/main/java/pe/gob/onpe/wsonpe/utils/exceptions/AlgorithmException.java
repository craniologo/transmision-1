package pe.gob.onpe.wsonpe.utils.exceptions;

public class AlgorithmException extends RuntimeException{
  public AlgorithmException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlgorithmException(Throwable cause) {
    super("Error al procesar el algoritmo criptográfico", cause);
  }
}
