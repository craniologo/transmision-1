package pe.gob.onpe.wsonpe.utils.exceptions;

public class InterruptedCustomException extends RuntimeException{
  public InterruptedCustomException(String message, Throwable cause) {
    super(message, cause);
  }

  public InterruptedCustomException(Throwable cause) {
    super("Error e interrupcción en el runtime", cause);
  }
}
